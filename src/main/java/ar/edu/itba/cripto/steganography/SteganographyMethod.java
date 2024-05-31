package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.model.BMP;

import java.util.ArrayList;
import java.util.List;

public enum SteganographyMethod {
    LSB1{
        @Override
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
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
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    },
    LSBI{
        @Override
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public abstract BMP embed(Byte[] message, BMP image);
    public abstract byte[] extract(BMP image);
}