package ar.edu.itba.cripto.cryptography;

import java.security.Key;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {

    private static final byte[] SALT = "salado".getBytes(); // "salado
    private static final int ITERATIONS = 65536;
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public Cryptography(CryptographyAlgorithm algorithm, CryptographyMode mode, String password) {

        int keySize = algorithm.getKeySize();
        String transformation = algorithm.getAlgorithm() + "/" + mode.name() + "/PKCS5Padding";

        try {

            this.encryptCipher = Cipher.getInstance(transformation);
            this.decryptCipher = Cipher.getInstance(transformation);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, keySize);
            SecretKey secretKey = keyFactory.generateSecret(spec);
            Key key = new SecretKeySpec(secretKey.getEncoded(), algorithm.getAlgorithm());

            // ECB does not need IV
            if (mode == CryptographyMode.ECB) {
                encryptCipher.init(Cipher.ENCRYPT_MODE, key);
                decryptCipher.init(Cipher.DECRYPT_MODE, key);

            } else {

                IvParameterSpec emptyIV =
                        new IvParameterSpec(new byte[encryptCipher.getBlockSize()]);

                encryptCipher.init(Cipher.ENCRYPT_MODE, key, emptyIV);
                decryptCipher.init(Cipher.DECRYPT_MODE, key, emptyIV);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error initializing cryptography", e);
        }
    }

    public byte[] encrypt(byte[] message) {
        try {
            return encryptCipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Illegal block size", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Bad padding", e);
        }
    }

    public byte[] decrypt(byte[] message) {
        try {
            return decryptCipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Illegal block size", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Bad padding", e);
        }
    }
}
