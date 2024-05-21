package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.model.BMP;

public enum SteganographyMethod {
    LSB1{
        @Override
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    },
    LSB4{
        @Override
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    },
    LSBI{
        @Override
        public BMP embed(Byte[] message, BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Byte[] extract(BMP image) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public abstract BMP embed(Byte[] message, BMP image);
    public abstract Byte[] extract(BMP image);
}