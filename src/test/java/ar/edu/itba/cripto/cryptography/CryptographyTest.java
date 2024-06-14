package ar.edu.itba.cripto.cryptography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class CryptographyTest {

    @Test
    public void test() {
        for (CryptographyAlgorithm algorithm : CryptographyAlgorithm.values()) {
            for (CryptographyMode mode : CryptographyMode.values()) {
                Cryptography cryptography = new Cryptography(algorithm, mode, "password");

                String message =
                        "Hello, World!, this message is going to be encrypted and decrypted by the following algorithm: "
                                + algorithm
                                + " using the following mode: "
                                + mode;

                byte[] encrypted = cryptography.encrypt(message.getBytes());
                byte[] decrypted = cryptography.decrypt(encrypted);

                Assertions.assertEquals(message, new String(decrypted));
            }
        }
    }
}
