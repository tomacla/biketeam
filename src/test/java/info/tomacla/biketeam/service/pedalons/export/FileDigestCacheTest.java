package info.tomacla.biketeam.service.pedalons.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FileDigestCacheTest {

    @TempDir
    Path dir;

    private static class CountingCache extends FileDigestCache {
        final AtomicInteger reads = new AtomicInteger();

        CountingCache(int maxEntries) {
            super(maxEntries);
        }

        @Override
        String digest(Path file) {
            reads.incrementAndGet();
            return super.digest(file);
        }
    }

    private Path file(String name, String content) throws Exception {
        return Files.writeString(dir.resolve(name), content);
    }

    @Test
    public void testOneEntryPerFileReplacedWhenTheFileChanges() throws Exception {
        CountingCache cache = new CountingCache(10);
        Path gpx = file("a.gpx", "one");

        String first = cache.md5(gpx, 3, 1000);
        assertEquals(first, cache.md5(gpx, 3, 1000));
        assertEquals(1, cache.reads.get(), "unchanged file read once");

        Files.writeString(gpx, "two!");
        String second = cache.md5(gpx, 4, 2000);
        assertNotEquals(first, second);
        assertEquals(FileDigestCache.compute(gpx), second);
        assertEquals(2, cache.reads.get());
        assertEquals(1, cache.size(), "the stale entry is replaced, not kept beside the new one");
    }

    @Test
    public void testTheCacheIsBoundedLeastRecentlyUsedFirst() throws Exception {
        CountingCache cache = new CountingCache(2);
        Path a = file("a", "a");
        Path b = file("b", "b");
        Path c = file("c", "c");

        cache.md5(a, 1, 1);
        cache.md5(b, 1, 1);
        cache.md5(a, 1, 1); // a devient le plus recent
        cache.md5(c, 1, 1); // evince b
        assertEquals(2, cache.size());
        assertEquals(3, cache.reads.get());

        cache.md5(a, 1, 1);
        assertEquals(3, cache.reads.get(), "a kept");
        cache.md5(b, 1, 1);
        assertEquals(4, cache.reads.get(), "b evicted, read again");
        assertEquals(2, cache.size());
    }

    @Test
    public void testTheDigestIsComputedOutsideTheLock() throws Exception {
        CountDownLatch computing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Path slow = file("slow", "slow");
        Path cached = file("cached", "cached");
        FileDigestCache cache = new FileDigestCache(10) {
            @Override
            String digest(Path file) {
                if (file.equals(slow)) {
                    computing.countDown();
                    try {
                        release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return super.digest(file);
            }
        };
        String expected = cache.md5(cached, 6, 1);

        CompletableFuture<String> pending = CompletableFuture.supplyAsync(() -> cache.md5(slow, 4, 1));
        try {
            assertTrue(computing.await(10, TimeUnit.SECONDS));
            // un fichier en cache reste servi pendant qu'un autre est lu
            String served = CompletableFuture.supplyAsync(() -> cache.md5(cached, 6, 1)).get(5, TimeUnit.SECONDS);
            assertEquals(expected, served);
        } finally {
            release.countDown();
        }
        assertEquals(FileDigestCache.compute(slow), pending.get(10, TimeUnit.SECONDS));
    }

}
