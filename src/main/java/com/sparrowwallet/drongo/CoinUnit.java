package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.Transaction;

public enum CoinUnit {
    AUTO("Auto") {
        @Override
        public long getTokensValue(double unitValue) {
            throw new UnsupportedOperationException("Auto unit cannot convert coin values");
        }

        @Override
        public double getValue(long tokensValue) {
            throw new UnsupportedOperationException("Auto unit cannot convert coin values");
        }
    },
    GFCN("GFCN") {
        @Override
        public long getTokensValue(double unitValue) {
            return Math.round(unitValue * Transaction.TOKENS_PER_COIN);
        }

        @Override
        public double getValue(long tokensValue) {
            return (double)tokensValue / Transaction.TOKENS_PER_COIN;
        }
    },
    TOKENS("tokens") {
        @Override
        public long getTokensValue(double unitValue) {
            return (long)unitValue;
        }

        @Override
        public double getValue(long tokensValue) {
            return (double)tokensValue;
        }
    };

    private final String label;

    CoinUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract long getTokensValue(double unitValue);

    public abstract double getValue(long tokensValue);

    public double convertFrom(double fromValue, CoinUnit fromUnit) {
        long tokensValue = fromUnit.getTokensValue(fromValue);
        return getValue(tokensValue);
    }

    public static long getAutoThreshold() {
        return 100000000L;
    }

    @Override
    public String toString() {
        return label;
    }
}
