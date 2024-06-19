package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.model.BMP;

import java.util.ArrayList;
import java.util.List;

public enum SteganographyMethod {
    LSB1{
        @Override
        public BMP embed(byte[] message, BMP image) {

            byte[] pixelData = image.getPixelData();

            int lsb1Mask = 0xFE;

            // i: itera los bytes de pixel data
            // messageIndex: itera los bytes del mensaje
            // bitIndex: itera el byte actual
            int messageIndex = 0;
            int bitIndex = 0;

            for (int i = 0; i < pixelData.length; i++) {

                // Si ya itere toodo el byte del mensaje paso al sig.
                if (bitIndex == 8) {
                    bitIndex = 0;
                    messageIndex++;
                }

                // Termine el mensaje, salgo
                if (messageIndex == message.length) {
                    break;
                }

                // Obtengo el bit actual del mensaje
                byte currentByte = message[messageIndex];
                int currentBit = (currentByte >> (7 - bitIndex)) & 1;

                // Modifico el bit menos significativo de la imagen con el del mensaje
                byte imageByte = pixelData[i];
                byte modifiedImageByte = (byte) ((imageByte & lsb1Mask) | currentBit);
                pixelData[i] = modifiedImageByte;

                bitIndex++;
            }

            // Si no pude embeber toodo el mensaje, error
            if (messageIndex < message.length) {
                throw new RuntimeException("BMP file is not long enough");
            }

            image.setPixelData(pixelData);

            return image;
        }

        @Override
        public byte[] extract(BMP image) {
            byte[] pixelData = image.getPixelData();
            List<Byte> hiddenData = new ArrayList<>();
            int byteValue = 0;
            int bitIndex = 0;
            int hiddenDataLength = 0;

            for (byte pixelDatum : pixelData) {
                byteValue = (byteValue << 1) | (pixelDatum & 0x1);
                bitIndex++;

                if (bitIndex == 8) {
                    hiddenData.add((byte)byteValue);
                    bitIndex = 0;
                    byteValue = 0;

                    // extraigo el length
                    if (hiddenData.size() == 4) {
                        hiddenDataLength =  ((hiddenData.get(0)) & 0xFF) << 24 |
                                            ((hiddenData.get(1)) & 0xFF) << 16 |
                                            ((hiddenData.get(2)) & 0xFF) <<  8 |
                                            ((hiddenData.get(3)) & 0xFF);
                        if(hiddenDataLength <= 0) {
                            throw new RuntimeException("No hidden data found");
                        }
                    } else if(hiddenData.size() >= 4 + hiddenDataLength + 1 && hiddenData.get(hiddenData.size() - 1) == 0) {
                        break;
                    }
                }
            }
            byte[] result = new byte[hiddenData.size()];
            for (int i = 0; i < hiddenData.size(); i++) {
                result[i] = hiddenData.get(i);
            }
            return result;
        }
    },
    LSB4{
        @Override
        public BMP embed(byte[] message, BMP image) {
            byte[] pixelData = image.getPixelData();
            int messageLength = message.length;
            int byteIndex = 0;
            int bitIndex = 0;

            for (int i = 0; i < pixelData.length && byteIndex < messageLength; i++) {

                int bitsToEmbed = (message[byteIndex] >> (4 - bitIndex * 4)) & 0xF;
                pixelData[i] = (byte) ((pixelData[i] & 0xF0) | bitsToEmbed);
                bitIndex++;

                if (bitIndex == 2) {
                    bitIndex = 0;
                    byteIndex++;
                }
            }

            image.setPixelData(pixelData);
            return image;
        }

        @Override
        public byte[] extract(BMP image) {
            byte[] pixelData = image.getPixelData();
            List<Byte> hiddenData = new ArrayList<>();
            int byteValue = 0;
            int bitIndex = 0;
            int hiddenDataLength = 0;

            for (byte pixelDatum : pixelData) {
                byteValue = (byteValue << 4) | (pixelDatum & 0xF);
                bitIndex+=4;

                if (bitIndex == 8) {
                    hiddenData.add((byte)byteValue);
                    bitIndex = 0;
                    byteValue = 0;

                    // extraigo el length
                    if (hiddenData.size() == 4) {
                        hiddenDataLength =  ((hiddenData.get(0)) & 0xFF) << 24 |
                                ((hiddenData.get(1)) & 0xFF) << 16 |
                                ((hiddenData.get(2)) & 0xFF) <<  8 |
                                ((hiddenData.get(3)) & 0xFF);
                        if(hiddenDataLength <= 0) {
                            throw new RuntimeException("No hidden data found");
                        }
                    } else if(hiddenData.size() >= 4 + hiddenDataLength + 1 && hiddenData.get(hiddenData.size() - 1) == 0) {
                        break;
                    }
                }
            }
            byte[] result = new byte[hiddenData.size()];
            for (int i = 0; i < hiddenData.size(); i++) {
                result[i] = hiddenData.get(i);
            }
            return result;
        }
    },
    LSBI{
        @Override
        public BMP embed(byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public abstract BMP embed(byte[] message, BMP image);
    public abstract byte[] extract(BMP image);
}