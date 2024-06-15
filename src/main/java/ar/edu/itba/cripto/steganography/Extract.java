package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.cryptography.Cryptography;
import ar.edu.itba.cripto.model.BMP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Extract {

    private BMP cover;
    private String outputPath;

    private SteganographyMethod steganographyMethod;
    private Cryptography cryptography;

    public void setCover(String coverFilePath) throws IOException {
        this.cover = new BMP(coverFilePath);
    }

    public void setOutput(String outputFilePath) {
        this.outputPath = outputFilePath;
    }

    public void setSteganographyMethod(String steganographyMethod) {
        this.steganographyMethod = SteganographyMethod.valueOf(steganographyMethod);
    }

    public void setCryptography(Cryptography cryptography) {
        this.cryptography = cryptography;
    }

    public void execute() throws IOException {
        byte[] hiddenData = steganographyMethod.extract(cover);

        if (cryptography != null) {

            // Skip first 4 bytes (length of encrypted data)
            byte[] encryptedLengthBytes = new byte[4];
            System.arraycopy(hiddenData, 0, encryptedLengthBytes, 0, 4);
            int encryptedLength = byteArrayToInt(hiddenData);

            byte[] hiddenDataWithoutLength = new byte[hiddenData.length - 4];
            System.arraycopy(
                    hiddenData, 4, hiddenDataWithoutLength, 0, hiddenDataWithoutLength.length);

            hiddenData = cryptography.decrypt(hiddenDataWithoutLength);
        }

        // Skip first 4 bytes (length of data)
        byte[] messageLengthBytes = new byte[4];
        System.arraycopy(hiddenData, 0, messageLengthBytes, 0, 4);
        int messageLength = byteArrayToInt(messageLengthBytes);

        byte[] message = new byte[messageLength];
        System.arraycopy(hiddenData, 4, message, 0, messageLength);

        // Get file extension
        byte[] extension = new byte[hiddenData.length - messageLength - 4];
        System.arraycopy(hiddenData, messageLength + 4, extension, 0, extension.length);

        // Ignore the last 0 byte
        String extensionString = new String(extension, 0, extension.length - 1);

        File outputFile = new File(outputPath + extensionString);

        Files.write(outputFile.toPath(), message);
    }

    private int byteArrayToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24
                | (bytes[1] & 0xFF) << 16
                | (bytes[2] & 0xFF) << 8
                | (bytes[3] & 0xFF);
    }
}
