package info.tomacla.biketeam.service.file;

import info.tomacla.biketeam.common.file.FileExtension;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ThumbnailService {

    @Autowired
    private FileService fileService;

    public Path resizeImage(Path originalImage, int targetWidth, FileExtension format) {
        try {
            final byte[] bytes = this.resizeImage(Files.readAllBytes(originalImage), targetWidth, format);
            final Path output = fileService.getTempFile("thumb", format.getExtension());
            Files.write(output, bytes);
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Error while generating image", e);
        }
    }

    public byte[] resizeImage(byte[] originalImage, int targetWidth, FileExtension format) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(originalImage))
                    .size(targetWidth, targetWidth)
                    .outputFormat(format.getImageIOType())
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while generating image", e);
        }
    }

}
