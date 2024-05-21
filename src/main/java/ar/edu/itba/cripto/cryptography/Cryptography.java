package ar.edu.itba.cripto.cryptography;

public class Cryptography {

    private CryptographyAlgorithm algorithm;
    private CryptographyMode mode;
    private String password;

    public Cryptography(CryptographyAlgorithm algorithm, CryptographyMode mode, String password) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.password = password;
    }

    public Byte[] encrypt(Byte[] message) {
        return null;
    }

    public Byte[] decrypt(Byte[] message) {
        return null;
    }
}
