package ar.edu.itba.cripto.cryptography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CryptographyTest {

    private static Stream<Arguments> provideCryptoCombinations() {
        return Stream.of(CryptographyAlgorithm.values())
                .flatMap(
                        algorithm ->
                                Stream.of(CryptographyMode.values())
                                        .map(mode -> Arguments.of(algorithm, mode)));
    }

    @ParameterizedTest
    @MethodSource("provideCryptoCombinations")
    public void test(CryptographyAlgorithm algorithm, CryptographyMode mode) {

        Cryptography encryptCryptography = new Cryptography(algorithm, mode, "password");
        Cryptography decryptCryptography = new Cryptography(algorithm, mode, "password");

        String message =
                "Hello, World!, this message is going to be encrypted and decrypted by the following algorithm: "
                        + algorithm
                        + " using the following mode: "
                        + mode;

        byte[] encrypted = encryptCryptography.encrypt(message.getBytes());
        byte[] decrypted = decryptCryptography.decrypt(encrypted);

        Assertions.assertEquals(message, new String(decrypted));
    }
}
