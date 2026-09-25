package info.tomacla.biketeam.service.pedalons.export;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Empreinte MD5 des fichiers exportes, en cache memoire.
 * <p>
 * Une entree par fichier (cle : chemin absolu), valable pour une taille et une date de
 * modification : un fichier modifie est relu et son entree remplacee. Relire les 2 600 GPX d'une
 * grosse equipe a chaque instantane couterait plusieurs secondes. Pedalons compare "taille:md5" a
 * l'empreinte du passage precedent pour ne pas retelecharger un GPX inchange.
 * <p>
 * Le cache est borne (LRU, {@link #MAX_ENTRIES} entrees) et le MD5 est calcule hors du verrou :
 * seules la lecture et l'ecriture de la table sont synchronisees. Deux fils peuvent calculer le
 * meme fichier en meme temps ; le resultat est identique, le dernier ecrit gagne.
 */
@Component
public class FileDigestCache {

    /** Plusieurs grosses equipes (quelques milliers de fichiers chacune), a ~200 octets l'entree. */
    static final int MAX_ENTRIES = 20_000;

    private record Digest(long size, long modified, String md5) {
    }

    private final Map<String, Digest> cache;

    public FileDigestCache() {
        this(MAX_ENTRIES);
    }

    FileDigestCache(int maxEntries) {
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Digest> eldest) {
                return size() > maxEntries;
            }
        });
    }

    public String md5(Path file, long size, long modifiedMillis) {
        final String key = file.toAbsolutePath().toString();
        final Digest cached = cache.get(key);
        if (cached != null && cached.size() == size && cached.modified() == modifiedMillis) {
            return cached.md5();
        }
        // I/O hors verrou
        final String md5 = digest(file);
        cache.put(key, new Digest(size, modifiedMillis, md5));
        return md5;
    }

    /** Point d'extension des tests. */
    String digest(Path file) {
        return compute(file);
    }

    public static String compute(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to digest " + Objects.toString(file), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    int size() {
        return cache.size();
    }

}
