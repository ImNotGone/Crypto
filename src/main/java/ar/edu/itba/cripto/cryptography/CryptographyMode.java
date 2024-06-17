package ar.edu.itba.cripto.cryptography;

public enum CryptographyMode {
    ECB("ECB", false),
    CFB("CFB8", true),
    OFB("OFB", true),
    CBC("CBC", true);

    private final String mode;
    private final boolean usesIV;

    CryptographyMode(String name, boolean usesIV) {
        this.mode = name;
        this.usesIV = usesIV;
    }

    public String getMode() {
        return mode;
    }

    public boolean usesIV() {
        return usesIV;
    }
}
