package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.protocol.SettlementFee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class TransactionParametersFeeTest {
    @Test
    public void remainderVsizeUsesIntegerFloor() {
        TransactionParameters params = parameters(0.0);
        Assertions.assertEquals(0L, params.getRequiredFeeAmount(11.9));
        Assertions.assertEquals(1L, params.getRequiredFeeAmount(12.0));
        Assertions.assertEquals(1L, params.getRequiredFeeAmount(23.9));
        Assertions.assertEquals(13L, params.getRequiredFeeAmount(161.0));
        Assertions.assertEquals(SettlementFee.minimumTokens(161), params.getRequiredFeeAmount(161.4));
    }

    @Test
    public void exactDivisibleVsizeMatchesFloor() {
        TransactionParameters params = parameters(0.0);
        Assertions.assertEquals(2L, params.getRequiredFeeAmount(24.0));
        Assertions.assertEquals(SettlementFee.minimumTokens(24), params.getRequiredFeeAmount(24));
    }

    @Test
    public void requestedFeeAboveFloorWins() {
        TransactionParameters params = new TransactionParameters(
                List.of(), List.of(), List.of(), List.of(), Set.of(),
                0.0, 0.0, 0.0, 50L, null, false, false, false);
        Assertions.assertEquals(50L, params.getRequiredFeeAmount(12.0));
    }

    private static TransactionParameters parameters(double feeRate) {
        return new TransactionParameters(
                List.of(), List.of(), List.of(), List.of(), Set.of(),
                feeRate, 0.0, 0.0, null, null, false, false, false);
    }
}
