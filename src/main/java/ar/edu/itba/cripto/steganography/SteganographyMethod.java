package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.model.BMP;

import java.util.*;
import java.util.function.Predicate;

public enum SteganographyMethod {
    LSB1 {
        @Override
        public BMP embed(byte[] message, BMP image) {
            return lsbEmbed(message, image, 1);
        }

        @Override
        public byte[] extract(BMP image, boolean containsExtension) {
            return lsbExtract(image, containsExtension, 1);
        }
    },
    LSB4 {
        @Override
        public BMP embed(byte[] message, BMP image) {
            return lsbEmbed(message, image, 4);
        }

        @Override
        public byte[] extract(BMP image, boolean containsExtension) {
            return lsbExtract(image, containsExtension, 4);
        }
    },
    LSBI {
        @Override
        public BMP embed(byte[] message, BMP image) {
            byte[] originalPixelData = image.getPixelData();

            // 4 bytes needed for storing the inversion pattern
            int bytesNeeded = message.length * 8 + 4;

            if (originalPixelData.length < bytesNeeded) {
                throw new RuntimeException("BMP file is not long enough");
            }

            /*
            0000 0000
            0000 0010
            0000 0100
            0000 0110
             */
            byte[] patterns = {0x0, 0x2, 0x4, 0x6};
            Map<Byte, Long> patternAppearances = new HashMap<>();
            Map<Byte, Long> patternInversions = new HashMap<>();

            int messageIndex = 0;
            int bitIndex = 0;

            int i;
            for (i = 4; i < originalPixelData.length; i++) {

                // Skip red byte
                if (i % 3 == 2) {
                    continue;
                }

                // Si ya itere toodo el byte del mensaje paso al sig.
                if (bitIndex == 8) {
                    bitIndex = 0;
                    messageIndex++;
                }

                // Termine el mensaje, salgo
                if (messageIndex == message.length) {
                    break;
                }

                // Obtengo el bit del mensaje
                byte currentByte = message[messageIndex];
                int bitToEmbed = (currentByte >> (7 - bitIndex)) & 1;

                byte imageByte = originalPixelData[i];

                // Cuento apariciones de los patrones
                byte pattern = (byte) (imageByte & 0x6);
                patternAppearances.merge(pattern, 1L, Long::sum);

                // Modifico el bit menos significativo de la imagen con el del mensaje
                byte modifiedImageByte = (byte) ((imageByte & 0xFE) | bitToEmbed);
                originalPixelData[i] = modifiedImageByte;

                // Guardo la inversion
                if (imageByte != modifiedImageByte) {
                    patternInversions.merge(pattern, 1L, Long::sum);
                }

                bitIndex++;
            }

            int lastByte = i;

            byte[] patternBytes = new byte[4];

            // Veo si hay que invertir
            for (int j = 0; j < patterns.length; j++) {
                byte pattern = patterns[j];
                long patternAppearancesCount = patternAppearances.getOrDefault(pattern, 0L);
                long patternInversionsCount = patternInversions.getOrDefault(pattern, 0L);

                if (patternAppearancesCount == 0) {
                    continue;
                }

                // Si hay mas de la mitad de inversiones, invierto
                if (patternInversionsCount > patternAppearancesCount / 2) {

                    for (int k = 0; k < lastByte; k++) {
                        byte imageByte = originalPixelData[k];
                        byte patternByte = (byte) (imageByte & 0x6);

                        if (patternByte == pattern) {
                            originalPixelData[k] = (byte) (imageByte ^ 0x1);
                        }
                    }

                    patternBytes[j] = 0x1;
                } else {
                    patternBytes[j] = 0x0;
                }
            }

            // Agrego los patrones al principio
            for (int j = 0; j < 4; j++) {
                originalPixelData[j] = (byte) ((originalPixelData[j] & 0xFE) | patternBytes[j]);
            }

            image.setPixelData(originalPixelData);
            return image;
        }

        @Override
        public byte[] extract(BMP image, boolean containsExtension) {
            byte[] pixelData = image.getPixelData();
            List<Byte> hiddenData = new ArrayList<>();

            Predicate<Integer> cutCondition = (length -> hiddenData.size() >= 4 + length);
            if (containsExtension) {
                cutCondition =
                        cutCondition.and(length -> hiddenData.get(hiddenData.size() - 1) == '\0');
            }

            int byteValue = 0;
            int bitIndex = 0;
            int hiddenDataLength = 0;

            Map<Byte, Boolean> patternInverted = new HashMap<>();
            byte[] patterns = {0x0, 0x2, 0x4, 0x6};

            for (int i = 0; i < 4; i++) {
                boolean inverted = (pixelData[i] & 0x1) == 0x1;
                patternInverted.put(patterns[i], inverted);
            }

            for (int j = 4; j < pixelData.length; j++) {

                // Skipeo el rojo
                if (j % 3 == 2) {
                    continue;
                }

                byte pixelDatum = pixelData[j];

                byte pattern = (byte) (pixelDatum & 0x6);
                boolean inverted = patternInverted.get(pattern);

                // Si esta invertido, invierto el bit
                byteValue = (byteValue << 1) | ((pixelDatum & 0x1) ^ (inverted ? 0x1 : 0x0));
                bitIndex++;

                if (bitIndex == 8) {
                    hiddenData.add((byte) byteValue);
                    bitIndex = 0;
                    byteValue = 0;

                    // extraigo el length
                    if (hiddenData.size() == 4) {
                        hiddenDataLength =
                                ((hiddenData.get(0)) & 0xFF) << 24
                                        | ((hiddenData.get(1)) & 0xFF) << 16
                                        | ((hiddenData.get(2)) & 0xFF) << 8
                                        | ((hiddenData.get(3)) & 0xFF);
                        if (hiddenDataLength <= 0) {
                            throw new RuntimeException("No hidden data found");
                        }
                    } else if (cutCondition.test(hiddenDataLength)) {
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
    };

    private static BMP lsbEmbed(byte[] message, BMP image, int bitsPerByte) {
        byte[] pixelData = image.getPixelData();

        // mask: pone en 0 los bits menos significativos
        int mask = (0xFF << (bitsPerByte)) & 0xFF;
        int bytesNeeded = message.length * (8 / bitsPerByte);

        if (pixelData.length < bytesNeeded) {
            throw new RuntimeException("BMP file is not long enough");
        }

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

            // Obtengo los bitsPerByte del byte
            byte currentByte = message[messageIndex];
            int bitsToEmbed =
                    (currentByte >> (8 - (bitsPerByte + bitIndex))) & ((1 << bitsPerByte) - 1);

            // Modifico el bit menos significativo de la imagen con el del mensaje
            byte imageByte = pixelData[i];
            byte modifiedImageByte = (byte) ((imageByte & mask) | bitsToEmbed);
            pixelData[i] = modifiedImageByte;

            bitIndex += bitsPerByte;
        }

        image.setPixelData(pixelData);

        return image;
    }

    private static byte[] lsbExtract(BMP image, boolean containsExtension, int bitsPerByte) {
        byte[] pixelData = image.getPixelData();
        List<Byte> hiddenData = new ArrayList<>();
        int byteValue = 0;
        int bitIndex = 0;
        int hiddenDataLength = 0;

        Predicate<Integer> cutCondition = (length -> hiddenData.size() >= 4 + length);
        if (containsExtension) {
            cutCondition =
                    cutCondition.and(length -> hiddenData.get(hiddenData.size() - 1) == '\0');
        }

        for (byte pixelDatum : pixelData) {
            byteValue = (byteValue << bitsPerByte) | (pixelDatum & ((1 << bitsPerByte) - 1));
            bitIndex += bitsPerByte;

            if (bitIndex == 8) {
                hiddenData.add((byte) byteValue);
                bitIndex = 0;
                byteValue = 0;

                // extraigo el length
                if (hiddenData.size() == 4) {
                    hiddenDataLength =
                            ((hiddenData.get(0)) & 0xFF) << 24
                                    | ((hiddenData.get(1)) & 0xFF) << 16
                                    | ((hiddenData.get(2)) & 0xFF) << 8
                                    | ((hiddenData.get(3)) & 0xFF);
                    if (hiddenDataLength <= 0) {
                        throw new RuntimeException("No hidden data found");
                    }
                } else if (cutCondition.test(hiddenDataLength)) {
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

    public abstract BMP embed(byte[] message, BMP image);

    public abstract byte[] extract(BMP image, boolean containsExtension);
}
