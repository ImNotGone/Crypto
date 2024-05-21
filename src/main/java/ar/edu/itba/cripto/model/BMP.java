package ar.edu.itba.cripto.model;

import java.io.FileInputStream;
import java.io.IOException;

public class BMP {
    private int width;
    private int rowSize;
    private int height;
    private byte[] pixelData;

    // only working with 24 bit BMPs
    private final int BYTES_PER_PIXEL = 3;

    // BMP Header fields
    private int fileSize;
    private int dataOffset;
    private int headerSize;
    private int bitsPerPixel;
    private int compression;

    public BMP(String filePath) throws IOException {
        loadBMP(filePath);
    }

    // Source: https://en.wikipedia.org/wiki/BMP_file_format#Example_1
    private void loadBMP(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);

        // BMP Header
        byte[] header = new byte[54];
        if(fis.read(header, 0, 54) != 54) {
            throw new IOException("Invalid BMP file");
        };

        // Check for BM marker
        if (header[0] != 'B' || header[1] != 'M') {
            throw new IOException("Not a valid BMP file");
        }

        fileSize = readInt(header, 2);
        dataOffset = readInt(header, 10);
        headerSize = readInt(header, 14);
        width = readInt(header, 18);
        height = readInt(header, 22);
        bitsPerPixel = readShort(header, 28);
        compression = readInt(header, 30);

        // Solo soportamos bmp con 24 bits por pixel
        if (bitsPerPixel != 24) {
            throw new IOException("Only 24-bit BMP files are supported");
        }

        // Solo soportamos bmp sin compresion
        if(compression != 0) {
            throw new IOException("Only BMP files without compression are supported");
        }

        // multiplico x BYTES_PER_PIXEL y agrego BYTES_PER_PIXEL
        // luego hago & con 11...1100
        // para alinear al siguiente multiplo de 4
        rowSize = (width * BYTES_PER_PIXEL + BYTES_PER_PIXEL) & ~3;
        pixelData = new byte[rowSize * height];

        fis.skip(dataOffset - 54);
        fis.read(pixelData);

        fis.close();
    }

    // aux para obtener datos de los headers
    private int readInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xFF)) |
                ((buffer[offset + 1] & 0xFF) << 8) |
                ((buffer[offset + 2] & 0xFF) << 16) |
                ((buffer[offset + 3] & 0xFF) << 24);
    }

    // aux para obtener datos de los headers
    private int readShort(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xFF)) |
                ((buffer[offset + 1] & 0xFF) << 8);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixelData() {
        return pixelData;
    }

    // Method to get the RGB value of a specific pixel
    public int getRGB(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        int pixelIndex = (height - y - 1) * rowSize + x * BYTES_PER_PIXEL;

        int blue = pixelData[pixelIndex] & 0xFF;
        int green = pixelData[pixelIndex + 1] & 0xFF;
        int red = pixelData[pixelIndex + 2] & 0xFF;

        return (red << 16) | (green << 8) | blue;
    }
}
