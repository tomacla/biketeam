package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.util.CacheFolderProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class CacheFolderProviderImpl implements CacheFolderProvider {

    @Value("${gpx.data.cache:cache}")
    private File cacheFolder = new File("cache");

    @Override
    public File getCacheFolder() {
        return cacheFolder;
    }
}
