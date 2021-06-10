package info.tomacla.biketeam.common;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Attachment {

    public static Base64 base64Encode = new Base64();

    public String content;
    public String name;
    public String mimeType;

    public Attachment(String content, String name, String mimeType) {
        this.content = content;
        this.name = name;
        this.mimeType = mimeType;
    }

    public static Attachment create(File file, String name, String mimeType) {
        try (InputStream finput = new FileInputStream(file)) {
            byte[] imageBytes = new byte[(int) file.length()];
            finput.read(imageBytes, 0, imageBytes.length);
            return new Attachment(base64Encode.encodeAsString(imageBytes), name, mimeType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Attachment create(String string, String name, String mimeType) {
        return new Attachment(base64Encode.encodeAsString(string.getBytes()),
                name, mimeType);
    }

}