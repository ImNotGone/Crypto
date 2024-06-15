package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.cryptography.Cryptography;
import ar.edu.itba.cripto.model.BMP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Embed {

    private byte[] input;
    private byte[] fileExtension;

    private BMP cover;
    private String outputPath;

    private SteganographyMethod steganographyMethod;
    private Cryptography cryptography;

    public void setInput(String inputFilePath) throws IOException {
        this.input = Files.readAllBytes(new File(inputFilePath).toPath());

        // Get the file extension: .extension\0
        String[] parts = inputFilePath.split("\\.");
        String extension = parts[parts.length - 1];

        this.fileExtension = ("." + extension + "\0").getBytes();
    }

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

        byte[] message = buildMessage();

        BMP stegoImage = steganographyMethod.embed(message, cover);

        stegoImage.writeBMP(outputPath);
    }

    private byte[] buildMessage() {
        // 4 bytes for the length of the file
        int messageLength = input.length;
        int extensionLength = fileExtension.length;

        // message: filelength + file + extension
        byte[] message = new byte[4 + messageLength + extensionLength];

        // Copy the file length
        byte[] messageLengthBytes = intToByteArray(messageLength);
        System.arraycopy(messageLengthBytes, 0, message, 0, 4);

        // Copy the file
        System.arraycopy(input, 0, message, 4, messageLength);

        // Copy the extension
        System.arraycopy(fileExtension, 0, message, 4 + messageLength, extensionLength);

        if (cryptography == null) {
            return message;
        }

        // Encrypt the message

        byte[] encryptedMessage = cryptography.encrypt(message);

        int encryptedMessageLength = encryptedMessage.length;
        byte[] encryptedLengthBytes = intToByteArray(encryptedMessageLength);

        // encryptedMessage: length + encrypted message
        byte[] encryptedMessageWithLength = new byte[4 + encryptedMessageLength];

        // Copy the encrypted file length
        System.arraycopy(encryptedLengthBytes, 0, encryptedMessageWithLength, 0, 4);

        // Copy the encrypted file
        System.arraycopy(
                encryptedMessage, 0, encryptedMessageWithLength, 4, encryptedMessageLength);

        return encryptedMessageWithLength;
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value
        };
    }
}
