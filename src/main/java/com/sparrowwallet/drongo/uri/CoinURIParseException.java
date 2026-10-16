package com.sparrowwallet.drongo.uri;

public class CoinURIParseException extends Exception {
    public CoinURIParseException(String s) {
        super(s);
    }

    public CoinURIParseException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
