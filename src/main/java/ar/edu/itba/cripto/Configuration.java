package ar.edu.itba.cripto;

import ar.edu.itba.cripto.cryptography.Cryptography;
import ar.edu.itba.cripto.cryptography.CryptographyAlgorithm;
import ar.edu.itba.cripto.cryptography.CryptographyMode;
import ar.edu.itba.cripto.steganography.Embed;
import ar.edu.itba.cripto.steganography.Extract;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.List;

public class Configuration {

    private static final Option[] options = {
        new Option("h", "help", false, "Show help"),
        new Option("embed", "embed", false, "Embed a message in an image"),
        new Option("extract", "extract", false, "Extract a message from an image"),
        new Option("in", "input", true, "Input file"),
        new Option("out", "output", true, "Output file"),
        new Option("p", "cover", true, "Cover file"),
        new Option("steg", "steganography", true, "Steganography method"),

        // Cryptography options, optional (only password is required)
        new Option("a", "algorithm", true, "Cryptography algorithm"),
        new Option("m", "mode", true, "Cryptography mode"),
        new Option("pass", "password", true, "Password"),
    };

    public static int run(String[] args) {
        // Create the command line parser
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        for (Option option : Configuration.options) {
            options.addOption(option);
        }

        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            return 1;
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
            return 0;
        }

        if (cmd.hasOption("embed")) {
            Embed embed = new Embed();

            if (!cmd.hasOption("input")) {
                System.err.println("Input file is required");
                return 1;
            }

            try {
                embed.setInput(cmd.getOptionValue("input"));
            } catch (IOException e) {
                System.err.println("Error reading input file: " + e.getMessage());
                return 1;
            }

            if (!cmd.hasOption("cover")) {
                System.err.println("Cover file is required");
                return 1;
            }

            try {
                embed.setCover(cmd.getOptionValue("cover"));
            } catch (IOException e) {
                System.err.println("Error reading cover file: " + e.getMessage());
                return 1;
            }

            if (!cmd.hasOption("output")) {
                System.err.println("Output file is required");
                return 1;
            }

            embed.setOutput(cmd.getOptionValue("output"));

            if (!cmd.hasOption("steganography")) {
                System.err.println("Steganography method is required");
                return 1;
            }

            try {
                embed.setSteganographyMethod(cmd.getOptionValue("steganography"));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid steganography method");
                return 1;
            }

            try {
                Cryptography cryptography = getCryptography(cmd);
                if (cryptography != null) {
                    embed.setCryptography(cryptography);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid cryptography parameters");
            }

            try {
                embed.execute();
            } catch (IOException e) {
                System.err.println("Error writing output file: " + e.getMessage());
            }

            return 0;
        }

        if (cmd.hasOption("extract")) {
            Extract extract = new Extract();

            if (!cmd.hasOption("p")) {
                System.err.println("Cover file is required");
                return 1;
            }

            try {
                extract.setCover(cmd.getOptionValue("p"));
            } catch (IOException e) {
                System.err.println("Error reading cover file: " + e.getMessage());
                return 1;
            }

            if (!cmd.hasOption("output")) {
                System.err.println("Output file is required");
                return 1;
            }

            extract.setOutput(cmd.getOptionValue("output"));

            if (!cmd.hasOption("steganography")) {
                System.err.println("Steganography method is required");
                return 1;
            }

            try {
                extract.setSteganographyMethod(cmd.getOptionValue("steganography"));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid steganography method");
                return 1;
            }

            try {
                Cryptography cryptography = getCryptography(cmd);
                if (cryptography != null) {
                    extract.setCryptography(cryptography);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid cryptography parameters");
            }

            try {
                extract.execute();
            } catch (IOException e) {
                System.err.println("Error writing output file: " + e.getMessage());
                return 1;
            }

            return 0;
        }

        printHelp(options);

        return 0;
    }

    private static void printHelp(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        List<Option> optionsList = List.of(Configuration.options);
        formatter.setOptionComparator(
                (o1, o2) -> optionsList.indexOf(o2) - optionsList.indexOf(o2));
        formatter.printHelp("stegobmp ", options);
    }

    private static Cryptography getCryptography(CommandLine cmd) {
        if (cmd.hasOption("algorithm") || cmd.hasOption("mode") || cmd.hasOption("password")) {
            if (!cmd.hasOption("password")) {
                throw new IllegalArgumentException("Password is required");
            }

            CryptographyAlgorithm algorithm = CryptographyAlgorithm.AES128;
            CryptographyMode mode = CryptographyMode.CBC;

            if (cmd.hasOption("algorithm")) {
                algorithm = CryptographyAlgorithm.valueOf(cmd.getOptionValue("algorithm"));
            }

            if (cmd.hasOption("mode")) {
                mode = CryptographyMode.valueOf(cmd.getOptionValue("mode"));
            }

            return new Cryptography(algorithm, mode, cmd.getOptionValue("password"));
        }
        return null;
    }
}
