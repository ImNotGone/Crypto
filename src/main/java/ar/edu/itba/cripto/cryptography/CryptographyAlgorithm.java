package ar.edu.itba.cripto.cryptography;

public enum CryptographyAlgorithm {
    AES128("AES", 128),
    AES192("AES", 192),
    AES256("AES", 256),
    DES("DES", 64);

    private final String algorithm;
    private final int keySize;

    CryptographyAlgorithm(String algorithm, int keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    public int getKeySize() {
        return keySize;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
