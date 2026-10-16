package com.sparrowwallet.drongo.uri;

import com.sparrowwallet.drongo.ChainEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CoinUriTest {
    private static final String ADDRESS = ChainEncoding.address("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4");

    @Test
    public void testSamourai() throws CoinURIParseException {
        String address = ChainEncoding.address("bc1qt4nrm47695ywdg9n30n68jarmxrjnkfmr36994");
        String uri = "gfcn:" + address.toUpperCase(Locale.ROOT) + "?amount=0,001";
        CoinURI coinURI = new CoinURI(uri);

        Assertions.assertEquals(address, coinURI.getAddress().toString());
        Assertions.assertEquals(Long.valueOf(100000), coinURI.getAmount());
    }

    @Test
    public void acceptsHttpsPayjoinUrl() throws CoinURIParseException {
        CoinURI coinURI = payjoinUri("https://example.com/payjoin");
        Assertions.assertNotNull(coinURI.getPayjoinUrl());
        Assertions.assertEquals("https://example.com/payjoin", coinURI.getPayjoinUrl().toString());
    }

    @Test
    public void acceptsHttpOnionPayjoinUrl() throws CoinURIParseException {
        CoinURI coinURI = payjoinUri("http://abcdefghijklmnopqrstuvwxyzabcdefghijklmnop.onion/payjoin");
        Assertions.assertNotNull(coinURI.getPayjoinUrl());
        Assertions.assertEquals("http://abcdefghijklmnopqrstuvwxyzabcdefghijklmnop.onion/payjoin", coinURI.getPayjoinUrl().toString());
    }

    @Test
    public void rejectsNonHttpOnionPayjoinUrl() throws CoinURIParseException {
        CoinURI coinURI = payjoinUri("file://abcdefghijklmnopqrstuvwxyzabcdefghijklmnop.onion/payjoin");
        Assertions.assertNull(coinURI.getPayjoinUrl());
    }

    @Test
    public void rejectsMalformedPayjoinUrl() throws CoinURIParseException {
        CoinURI coinURI = payjoinUri("payjoin");
        Assertions.assertNull(coinURI.getPayjoinUrl());
    }

    @Test
    public void rejectsExponentAmount() {
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("1e2"));
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("1E-8"));
    }

    @Test
    public void rejectsNonDecimalAmount() {
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("+1"));
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("-1"));
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("1.2.3"));
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("\u0663"));
    }

    @Test
    public void acceptsDecimalAmount() throws CoinURIParseException {
        Assertions.assertEquals(Long.valueOf(12345678), amountUri("0.12345678").getAmount());
        Assertions.assertEquals(Long.valueOf(50000000), amountUri("0,5").getAmount());
        Assertions.assertEquals(Long.valueOf(100000000), amountUri("1.").getAmount());
        Assertions.assertEquals(Long.valueOf(50000000), amountUri(".5").getAmount());
    }

    @Test
    public void rejectsTooManyDecimalPlaces() {
        Assertions.assertThrows(OptionalFieldValidationException.class, () -> amountUri("0.123456789"));
    }

    @Test
    public void rejectsBitcoinScheme() {
        Assertions.assertThrows(CoinURIParseException.class, () -> new CoinURI("bitcoin:" + ADDRESS));
    }

    private static CoinURI amountUri(String amount) throws CoinURIParseException {
        return new CoinURI("gfcn:" + ADDRESS + "?amount=" + amount);
    }

    private static CoinURI payjoinUri(String payjoinUrl) throws CoinURIParseException {
        return new CoinURI("gfcn:" + ADDRESS + "?pj=" + URLEncoder.encode(payjoinUrl, StandardCharsets.UTF_8));
    }
}
