package ar.edu.itba.cripto;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AppTest {

    private static final String coverFilePath = "src/test/resources/cover.bmp";
    private static final String messageFilePath = "src/test/resources/message.java";

    @TempDir public File tempDir;

    @Test
    public void appTestLSB1() throws IOException {

        String hiddenFilePath = tempDir.getAbsolutePath() + "/hidden.bmp";
        String outputFilePath = tempDir.getAbsolutePath() + "/output";

        // Embed
        String[] args = {
                "-embed",
                "-in",
                messageFilePath,
                "-p",
                coverFilePath,
                "-out",
                hiddenFilePath,
                "-steg",
                "LSB1"
        };

        App.main(args);

        // Extract
        args =
                new String[] {
                        "-extract", "-p", hiddenFilePath, "-out", outputFilePath, "-steg", "LSB1"
                };

        App.main(args);

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
        String[] args = {
                "-embed",
                "-in",
                messageFilePath,
                "-p",
                coverFilePath,
                "-out",
                hiddenFilePath,
                "-steg",
                "LSB4"
        };

        App.main(args);

        // Extract
        args =
                new String[] {
                        "-extract", "-p", hiddenFilePath, "-out", outputFilePath, "-steg", "LSB4"
                };

        App.main(args);

        // Compare
        boolean areEqual =
                Files.mismatch(
                        new File(messageFilePath).toPath(),
                        new File(outputFilePath + ".java").toPath())
                        == -1;
        Assertions.assertTrue(areEqual);
    }
}

