package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ThumbnailService {

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
