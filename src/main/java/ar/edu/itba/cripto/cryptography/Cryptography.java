package ar.edu.itba.cripto.cryptography;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {

    private static final byte[] SALT = new byte[8];
    private static final int ITERATIONS = 10000;
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public Cryptography(CryptographyAlgorithm algorithm, CryptographyMode mode, String password) {

        String transformation = algorithm.getAlgorithm() + "/" + mode.getMode() + "/PKCS5Padding";

        try {

            this.encryptCipher = Cipher.getInstance(transformation);
            this.decryptCipher = Cipher.getInstance(transformation);

            // Generate key and IV from password
            int keySize = algorithm.getKeySize();
            int ivSize = mode.usesIV() ? algorithm.getIvSize() : 0;

            byte[] keyIV = deriveKeyIV(password, keySize + ivSize);

            byte[] keyBytes = new byte[keySize / 8];
            System.arraycopy(keyIV, 0, keyBytes, 0, keySize / 8);

            Key key = new SecretKeySpec(keyBytes, algorithm.getAlgorithm());

            IvParameterSpec iv = null;
            if (mode.usesIV()) {
                byte[] ivBytes = new byte[ivSize / 8];
                System.arraycopy(keyIV, keySize / 8, ivBytes, 0, ivSize / 8);
                iv = new IvParameterSpec(ivBytes);
            }

            // Initialize ciphers
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
            decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);

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

    private byte[] deriveKeyIV(String password, int size)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, size);
        SecretKey key = factory.generateSecret(spec);
        return key.getEncoded();
    }
}
