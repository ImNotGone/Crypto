package ar.edu.itba.cripto.model;

import java.io.FileInputStream;
import java.io.IOException;

public class BMP {
    private int width;
    private int height;
    private int rowSize;
    private byte[] pixelData;

    // only working with 24 bit BMPs
    // Source: https://en.wikipedia.org/wiki/BMP_file_format#Example_1
    private static final int BYTES_PER_PIXEL = 3;
    private static final int FILE_HEADER_SIZE = 14;
    private static final int BMP_INFO_HEADER_SIZE = 40;
    private static final int HEADER_SIZE = FILE_HEADER_SIZE + BMP_INFO_HEADER_SIZE;
    private static final int BITS_PER_PIXEL = BYTES_PER_PIXEL * 8;
    private static final int COMPRESSION = 0;

    public BMP(String filePath) throws IOException {
        loadBMP(filePath);
    }

    public BMP(int width, int height) {
        this.width = width;
        this.height = height;
        this.rowSize = getRowSize(width);
        this.pixelData = new byte[height * rowSize];
    }

    private int getRowSize(int width) {
        return (width * BYTES_PER_PIXEL + 3) & ~3;
    }

    private void loadBMP(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);

        // BMP Header
        byte[] header = new byte[HEADER_SIZE];
        if(fis.read(header, 0, HEADER_SIZE) != HEADER_SIZE) {
            throw new IOException("Invalid BMP file");
        }

        // Check for BM marker
        if (header[0] != 'B' || header[1] != 'M') {
            throw new IOException("Not a valid BMP file");
        }

        this.width = readInt(header, 18);
        this.height = readInt(header, 22);
        int bitsPerPixel = readShort(header, 28);
        int compression = readInt(header, 30);

        // Solo soportamos bmp con 24 bits por pixel sin compresion
        if (bitsPerPixel != BITS_PER_PIXEL || compression != COMPRESSION) {
            throw new IOException("Only 24-bit BMP files without compression are supported");
        }

        // multiplico x BYTES_PER_PIXEL y agrego BYTES_PER_PIXEL
        // luego hago & con 11...1100
        // para alinear al siguiente multiplo de 4
        rowSize = getRowSize(width);
        pixelData = new byte[rowSize * height];

        int dataOffset = readInt(header, 10);
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
