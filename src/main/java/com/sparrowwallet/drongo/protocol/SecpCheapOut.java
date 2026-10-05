package com.sparrowwallet.drongo.protocol;

/**
 * secp is offered. The warning fires on every receive and send. The click after the warning works.
 */
public final class SecpCheapOut {
    public static final String WARNING = "secp is cheap and not quantum-safe. The payment still goes.";

    private SecpCheapOut() {
    }

    public static String warnReceive() {
        return WARNING;
    }

    public static String warnSend() {
        return WARNING;
    }
}
