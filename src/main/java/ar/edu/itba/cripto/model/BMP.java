package ar.edu.itba.cripto.model;

import ar.edu.itba.cripto.steganography.SteganographyMethod;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    public static final int BLUE = 0xFF;
    public static final int GREEN = 0xFF00;
    public static final int RED = 0xFF0000;

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

        rowSize = getRowSize(width);
        pixelData = new byte[rowSize * height];

        int dataOffset = readInt(header, 10);
        fis.skip(dataOffset - 54);
        fis.read(pixelData);
        fis.close();
    }

    public void writeBMP(String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        byte[] header = new byte[HEADER_SIZE];
        header[0] = 'B';
        header[1] = 'M';
        writeInt(header, 2, FILE_HEADER_SIZE);
        writeInt(header, 10, HEADER_SIZE);
        writeInt(header, 14, 40); // taken from wikipedia
        writeInt(header, 18, width);
        writeInt(header, 22, height);
        writeShort(header, 26, 1); // planes
        writeInt(header, 28, BITS_PER_PIXEL);
        writeInt(header, 30, COMPRESSION); // 0, no compression
        writeInt(header, 34, pixelData.length); // raw length (includes padding)

        fos.write(header);
        fos.write(pixelData);
        fos.close();
    }

    // aux para obtener datos de los headers
    private int readInt(byte[] buffer, int offset) {
        return ByteBuffer.wrap(buffer, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private int readShort(byte[] buffer, int offset) {
        return ByteBuffer.wrap(buffer, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private void writeInt(byte[] buffer, int offset, int value) {
        ByteBuffer.wrap(buffer, offset, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(value);
    }

    private void writeShort(byte[] buffer, int offset, int value) {
        ByteBuffer.wrap(buffer, offset, 2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) value);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixelData() {
        byte[] copy = new byte[pixelData.length];
        System.arraycopy(pixelData, 0, copy, 0, pixelData.length);
        return copy;
    }

    public void setPixelData(byte[] pixelData) {
        if(pixelData.length != this.pixelData.length) {
            throw new IllegalArgumentException("Invalid pixel data length");
        }
        System.arraycopy(pixelData, 0, this.pixelData, 0, pixelData.length);
    }

    private int getIndex(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return (height - y - 1) * rowSize + x * BYTES_PER_PIXEL;
    }

    public int getRGB(int x, int y) {
        int pixelIndex = getIndex(x,y);

        int blue = pixelData[pixelIndex] & 0xFF;
        int green = pixelData[pixelIndex + 1] & 0xFF;
        int red = pixelData[pixelIndex + 2] & 0xFF;

        return (red << 16) | (green << 8) | blue;
    }

    public void setRGB(int x, int y, int rgb) {
        int pixelIndex = getIndex(x,y);

        pixelData[pixelIndex] = (byte) (rgb & 0xFF);
        pixelData[pixelIndex + 1] = (byte) ((rgb >> 8) & 0xFF);
        pixelData[pixelIndex + 2] = (byte) ((rgb >> 16) & 0xFF);
    }

    public BMP scale(int scale) {
        if(scale < 1) {
            throw new IllegalArgumentException("Scale must be greater than 0");
        }
        BMP bmp = new BMP(width * scale, height * scale);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = getRGB(x, y);
                int x_mul = x*scale;
                int y_mul = y*scale;
                for(int y_scaled = 0; y_scaled < scale; y_scaled++) {
                    for (int x_scaled = 0; x_scaled < scale; x_scaled++) {
                        bmp.setRGB(x_mul + x_scaled, y_mul + y_scaled, pixel);
                    }
                }
            }
        }

        return bmp;
    }

    public static void main(String[] args) throws IOException {
        BMP bmp = new BMP("ladoLSB1.bmp");

        byte[] data = SteganographyMethod.LSB1.extract(bmp);
        int length = ((data[0]) & 0xFF) << 24 |
                ((data[1]) & 0xFF) << 16 |
                ((data[2]) & 0xFF) <<  8 |
                ((data[3]) & 0xFF);
        StringBuilder fileNameBuilder = new StringBuilder().append("out");
        for (int i = 4 + length; i < data.length - 1; i++) {
            fileNameBuilder.append((char)data[i]);
        }
        byte[] fileBytes = new byte[length];
        System.arraycopy(data, 4, fileBytes, 0, length);
        FileOutputStream fos = new FileOutputStream(fileNameBuilder.toString());
        fos.write(fileBytes);
        fos.close();
    }
}
