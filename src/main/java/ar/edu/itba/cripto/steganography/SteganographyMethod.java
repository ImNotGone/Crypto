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

            BMP stegoImage = SteganographyMethod.LSB1.embed(message, image);
            byte[] stegoImagePixelData = stegoImage.getPixelData();

            /*
            0000 0000
            0000 0010
            0000 0100
            0000 0110
             */
            byte[] possiblePatterns = {0x0, 0x2, 0x4, 0x6};
            Map<Byte, Integer> patternCount = new HashMap<>();
            for (byte originalPixelDatum : originalPixelData) {
                for (byte possiblePattern : possiblePatterns) {
                    if ((originalPixelDatum & 0x6) == possiblePattern) {
                        patternCount.put(
                                possiblePattern, patternCount.getOrDefault(possiblePattern, 0) + 1);
                    }
                }
                if (patternCount.size() == possiblePatterns.length) {
                    break;
                }
            }

            // Check if all patterns have at least 2 pixels
            for (byte pattern : possiblePatterns) {
                if (patternCount.getOrDefault(pattern, 0) < 2) {
                    patternCount.remove(pattern);
                }
            }

            Set<Byte> patternsToVerify = patternCount.keySet();
            Map<Byte, Integer> changedCount = new HashMap<>();
            Map<Byte, Integer> unchangedCount = new HashMap<>();
            for (int i = 0; i < stegoImagePixelData.length; i++) {
                byte stegoPixelDatum = stegoImagePixelData[i];
                byte originalPixelDatum = originalPixelData[i];

                byte currentPattern = (byte) (originalPixelDatum & 0x6);

                if (patternsToVerify.contains(currentPattern)) {
                    byte originalLeastSignificantBit = (byte) (originalPixelDatum & 0x1);
                    byte stegoLeastSignificantBit = (byte) (stegoPixelDatum & 0x1);

                    if (originalLeastSignificantBit != stegoLeastSignificantBit) {
                        changedCount.put(
                                currentPattern, changedCount.getOrDefault(currentPattern, 0) + 1);
                    } else {
                        unchangedCount.put(
                                currentPattern, unchangedCount.getOrDefault(currentPattern, 0) + 1);
                    }
                }
            }

            Set<Byte> patternsToChange = new HashSet<>();
            for (byte pattern : patternsToVerify) {
                if (changedCount.get(pattern) <= unchangedCount.get(pattern)) {
                    patternsToChange.add(pattern);
                }
            }

            // Change the least significant bit of the pixels with the patterns to change
            int messageIndex = 0;
            int bitIndex = 0;
            for (byte stegoImagePixelDatum : stegoImagePixelData) {
                // Si ya itere toodo el byte del mensaje paso al sig.
                if (bitIndex == 8) {
                    bitIndex = 0;
                    messageIndex++;
                }

                // Termine el mensaje, salgo
                if (messageIndex == message.length) {
                    break;
                }

                byte currentPattern = (byte) (stegoImagePixelDatum & 0x6);

                if (patternsToChange.contains(currentPattern)) {
                    // Invert current bit
                    message[messageIndex] = (byte) (message[messageIndex] ^ (1 << (7 - bitIndex)));
                }
                bitIndex++;
            }

            // Embed the inversion pattern
            byte[] inversionPattern = new byte[possiblePatterns.length];
            for (int i = 0; i < possiblePatterns.length; i++) {
                byte pattern = possiblePatterns[i];
                if (patternsToChange.contains(pattern)) {
                    inversionPattern[i] = 1;
                } else {
                    inversionPattern[i] = 0;
                }
            }

            byte[] newMessage = new byte[message.length + 4];
            System.arraycopy(inversionPattern, 0, newMessage, 0, 4);
            System.arraycopy(message, 0, newMessage, 4, message.length);

            return SteganographyMethod.LSB1.embed(newMessage, image);
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
                    j++;
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
