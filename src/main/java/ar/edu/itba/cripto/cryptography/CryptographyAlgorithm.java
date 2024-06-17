package ar.edu.itba.cripto.cryptography;

public enum CryptographyAlgorithm {
    AES128("AES", 128, 128),
    AES192("AES", 192, 128),
    AES256("AES", 256, 128),
    DES("DESede", 192, 64);

    private final String algorithm;
    private final int keySize;
    private final int ivSize;

    CryptographyAlgorithm(String algorithm, int keySize, int ivSize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.ivSize = ivSize;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getIvSize() {
        return ivSize;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
