package ar.edu.itba.cripto.cryptography;

public enum CryptographyMode {
    ECB("ECB", false, true),
    CFB("CFB8", true, false),
    OFB("OFB", true, false),
    CBC("CBC", true, true);

    private final String mode;
    private final boolean usesIV;
    private final boolean usesPadding;

    CryptographyMode(String name, boolean usesIV, boolean usesPadding) {
        this.mode = name;
        this.usesIV = usesIV;
        this.usesPadding = usesPadding;
    }

    public String getMode() {
        return mode;
    }

    public boolean usesIV() {
        return usesIV;
    }

    public boolean usesPadding() {
        return usesPadding;
    }
}
