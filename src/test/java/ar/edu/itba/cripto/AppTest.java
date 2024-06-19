package ar.edu.itba.cripto;

import ar.edu.itba.cripto.steganography.Embed;
import ar.edu.itba.cripto.steganography.Extract;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AppTest {

    private static final String coverFilePath = "src/test/resources/cover.bmp";
    private static final String messageFilePath = "src/test/resources/message.java";

    @TempDir public File tempDir;

    private Embed embed;
    private Extract extract;

    @Test
    public void appTestLSB1() throws IOException {

        String hiddenFilePath = tempDir.getAbsolutePath() + "/hidden.bmp";
        String outputFilePath = tempDir.getAbsolutePath() + "/output";

        // Embed
        embed = new Embed();
        embed.setInput(messageFilePath);
        embed.setCover(coverFilePath);
        embed.setOutput(hiddenFilePath);
        embed.setSteganographyMethod("LSB1");
        embed.execute();

        // Extract
        extract = new Extract();
        extract.setCover(hiddenFilePath);
        extract.setOutput(outputFilePath);
        extract.setSteganographyMethod("LSB1");
        extract.execute();

        // Compare
        boolean areEqual =
                Files.mismatch(
                                new File(messageFilePath).toPath(),
                                new File(outputFilePath + ".java").toPath())
                        == -1;
        Assertions.assertTrue(areEqual);
    }

    @Test
    public void appTestLSB4() throws IOException {

        String hiddenFilePath = tempDir.getAbsolutePath() + "/hidden.bmp";
        String outputFilePath = tempDir.getAbsolutePath() + "/output";

        // Embed
        embed = new Embed();
        embed.setInput(messageFilePath);
        embed.setCover(coverFilePath);
        embed.setOutput(hiddenFilePath);
        embed.setSteganographyMethod("LSB4");
        embed.execute();

        // Extract
        extract = new Extract();
        extract.setCover(hiddenFilePath);
        extract.setOutput(outputFilePath);
        extract.setSteganographyMethod("LSB4");
        extract.execute();

        // Compare
        boolean areEqual =
                Files.mismatch(
                                new File(messageFilePath).toPath(),
                                new File(outputFilePath + ".java").toPath())
                        == -1;
        Assertions.assertTrue(areEqual);
    }
}
