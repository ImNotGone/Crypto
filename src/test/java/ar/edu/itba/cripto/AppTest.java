package ar.edu.itba.cripto;

import ar.edu.itba.cripto.cryptography.Cryptography;
import ar.edu.itba.cripto.cryptography.CryptographyAlgorithm;
import ar.edu.itba.cripto.cryptography.CryptographyMode;
import ar.edu.itba.cripto.steganography.Embed;
import ar.edu.itba.cripto.steganography.Extract;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public class AppTest {

    private static final String COVER_FILE_PATH = "src/test/resources/cover.bmp";
    private static final String MESSAGE_FILE_PATH = "src/test/resources/message.java";
    private static final String PASSWORD = "stegobmp";

    private static final String[] SUPPORTED_STEGANOGRAPHY_METHODS = {"LSB1", "LSB4"};

    @TempDir public File tempDir;

    private static Stream<Arguments> provideParameterCombinations() {
        return Stream.of(SUPPORTED_STEGANOGRAPHY_METHODS)
                .flatMap(
                        steganographyMethod ->
                                Stream.of(CryptographyAlgorithm.values())
                                        .flatMap(
                                                cryptographyAlgorithm ->
                                                        Stream.of(CryptographyMode.values())
                                                                .map(
                                                                        cryptographyMode ->
                                                                                Arguments.of(
                                                                                        steganographyMethod,
                                                                                        cryptographyAlgorithm,
                                                                                        cryptographyMode))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LSB1", "LSB4"})
    public void appTestNoCrypto(String steganographyMethod) throws IOException {

        String hiddenFilePath = tempDir.getAbsolutePath() + "/hidden.bmp";
        String outputFilePath = tempDir.getAbsolutePath() + "/output";

        // Embed
        Embed embed = new Embed();
        embed.setInput(MESSAGE_FILE_PATH);
        embed.setCover(COVER_FILE_PATH);
        embed.setOutput(hiddenFilePath);
        embed.setSteganographyMethod(steganographyMethod);
        embed.execute();

        // Extract
        Extract extract = new Extract();
        extract.setCover(hiddenFilePath);
        extract.setOutput(outputFilePath);
        extract.setSteganographyMethod(steganographyMethod);
        extract.execute();

        // Compare
        boolean areEqual =
                Files.mismatch(
                                new File(MESSAGE_FILE_PATH).toPath(),
                                new File(outputFilePath + ".java").toPath())
                        == -1;
        Assertions.assertTrue(areEqual);
    }

    @ParameterizedTest
    @MethodSource("provideParameterCombinations")
    public void appTestCrypto(
            String steganographyMethod,
            CryptographyAlgorithm cryptographyAlgorithm,
            CryptographyMode cryptographyMode)
            throws IOException {
        String hiddenFilePath = tempDir.getAbsolutePath() + "/hidden.bmp";
        String outputFilePath = tempDir.getAbsolutePath() + "/output";

        // Embed
        Embed embed = new Embed();
        embed.setInput(MESSAGE_FILE_PATH);
        embed.setCover(COVER_FILE_PATH);
        embed.setOutput(hiddenFilePath);
        embed.setSteganographyMethod(steganographyMethod);
        embed.setCryptography(new Cryptography(cryptographyAlgorithm, cryptographyMode, PASSWORD));
        embed.execute();

        // Extract
        Extract extract = new Extract();
        extract.setCover(hiddenFilePath);
        extract.setOutput(outputFilePath);
        extract.setSteganographyMethod(steganographyMethod);
        extract.setCryptography(
                new Cryptography(cryptographyAlgorithm, cryptographyMode, PASSWORD));
        extract.execute();

        // Compare
        boolean areEqual =
                Files.mismatch(
                                new File(MESSAGE_FILE_PATH).toPath(),
                                new File(outputFilePath + ".java").toPath())
                        == -1;
        Assertions.assertTrue(areEqual);
    }
}
