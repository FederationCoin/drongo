package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.protocol.SettlementFee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class TransactionParametersFeeTest {
    @Test
    public void requiredFeeNeverBelowChainMinimum() {
        TransactionParameters zeroRate = new TransactionParameters(
                List.of(), List.of(), List.of(), List.of(), Set.of(),
                0.0, 0.0, 0.0, null, null, false, false, false);
        Assertions.assertEquals(13L, zeroRate.getRequiredFeeAmount(161));
        Assertions.assertEquals(SettlementFee.minimumTokens(12), zeroRate.getRequiredFeeAmount(12));

        TransactionParameters highRate = new TransactionParameters(
                List.of(), List.of(), List.of(), List.of(), Set.of(),
                2.0, 0.0, 0.0, null, null, false, false, false);
        Assertions.assertEquals(24L, highRate.getRequiredFeeAmount(12));

        TransactionParameters userFeeBelow = new TransactionParameters(
                List.of(), List.of(), List.of(), List.of(), Set.of(),
                1.0, 0.0, 0.0, 0L, null, false, false, false);
        Assertions.assertEquals(1L, userFeeBelow.getRequiredFeeAmount(12));
    }
}
