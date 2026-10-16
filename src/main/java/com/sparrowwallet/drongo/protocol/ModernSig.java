package com.sparrowwallet.drongo.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dilithium locking scripts. Kind is looked up from (algorithm, param) small-ints.
 * Do not push the integers 44 or 87.
 */
public final class ModernSig {
    public static final int ALGO_DILITHIUM = 1;
    public static final int PARAM_DILITHIUM_44 = 1;
    public static final int PARAM_DILITHIUM_87 = 2;
    public static final int SINGLE_SCRIPT_SIZE = 36;
    public static final int POLICY_SCRIPT_MAX = 401;
    public static final int MAX_KEYS = 12;
    public static final int BECH32_CHAR_LIMIT = 700;

    public record Single(byte[] fingerprint, int algorithm, int param) {}
    public record Policy(int threshold, List<byte[]> hashes, int n, int algorithm, int param) {}

    private ModernSig() {
    }

    public static Script singleScript(byte[] fingerprint, int param) {
        if(fingerprint == null || fingerprint.length != 32) {
            throw new ProtocolException("modern-sig fingerprint is 32 bytes");
        }
        if(param != PARAM_DILITHIUM_44 && param != PARAM_DILITHIUM_87) {
            throw new ProtocolException("modern-sig param is OP_1 (44) or OP_2 (87)");
        }
        List<ScriptChunk> chunks = new ArrayList<>();
        chunks.add(new ScriptChunk(fingerprint.length, fingerprint));
        chunks.add(new ScriptChunk(Script.encodeToOpN(ALGO_DILITHIUM), null));
        chunks.add(new ScriptChunk(Script.encodeToOpN(param), null));
        chunks.add(new ScriptChunk(ScriptOpCodes.OP_CHECKMODERNSIG, null));
        return new Script(chunks);
    }

    public static Script policyScript(int threshold, List<byte[]> hashes, int param) {
        if(hashes == null || hashes.size() < 1 || hashes.size() > MAX_KEYS) {
            throw new ProtocolException("modern-sig policy key count");
        }
        if(threshold < 1 || threshold > hashes.size()) {
            throw new ProtocolException("modern-sig policy threshold");
        }
        if(param != PARAM_DILITHIUM_44 && param != PARAM_DILITHIUM_87) {
            throw new ProtocolException("modern-sig param is OP_1 (44) or OP_2 (87)");
        }
        List<ScriptChunk> chunks = new ArrayList<>();
        chunks.add(new ScriptChunk(Script.encodeToOpN(threshold), null));
        for(byte[] hash : hashes) {
            if(hash == null || hash.length != 32) {
                throw new ProtocolException("modern-sig policy hash is 32 bytes");
            }
            chunks.add(new ScriptChunk(hash.length, hash));
        }
        chunks.add(new ScriptChunk(Script.encodeToOpN(hashes.size()), null));
        chunks.add(new ScriptChunk(Script.encodeToOpN(ALGO_DILITHIUM), null));
        chunks.add(new ScriptChunk(Script.encodeToOpN(param), null));
        chunks.add(new ScriptChunk(ScriptOpCodes.OP_CHECKMODERNPOLICY, null));
        return new Script(chunks);
    }

    public static boolean isKnownKind(int algorithm, int param) {
        return algorithm == ALGO_DILITHIUM && (param == PARAM_DILITHIUM_44 || param == PARAM_DILITHIUM_87);
    }

    public static boolean isTemplate(Script script) {
        Single single = matchSingle(script);
        if(single != null) {
            return isKnownKind(single.algorithm(), single.param());
        }
        Policy policy = matchPolicy(script);
        return policy != null && isKnownKind(policy.algorithm(), policy.param());
    }

    public static Single matchSingle(Script script) {
        List<ScriptChunk> chunks = script.chunks;
        if(chunks.size() != 4) {
            return null;
        }
        byte[] fingerprint = chunks.get(0).data;
        if(fingerprint == null || fingerprint.length != 32) {
            return null;
        }
        Integer algo = decodeSmallInt(chunks.get(1));
        Integer param = decodeSmallInt(chunks.get(2));
        if(algo == null || param == null) {
            return null;
        }
        if(!chunks.get(3).equalsOpCode(ScriptOpCodes.OP_CHECKMODERNSIG)) {
            return null;
        }
        return new Single(fingerprint, algo, param);
    }

    public static Policy matchPolicy(Script script) {
        List<ScriptChunk> chunks = script.chunks;
        if(chunks.size() < 6) {
            return null;
        }
        if(!chunks.get(chunks.size() - 1).equalsOpCode(ScriptOpCodes.OP_CHECKMODERNPOLICY)) {
            return null;
        }
        Integer param = decodeSmallInt(chunks.get(chunks.size() - 2));
        Integer algo = decodeSmallInt(chunks.get(chunks.size() - 3));
        Integer n = decodeSmallInt(chunks.get(chunks.size() - 4));
        Integer threshold = decodeSmallInt(chunks.get(0));
        if(param == null || algo == null || n == null || threshold == null) {
            return null;
        }
        if(n < 1 || n > MAX_KEYS || threshold < 1 || threshold > n) {
            return null;
        }
        if(chunks.size() != n + 5) {
            return null;
        }
        List<byte[]> hashes = new ArrayList<>(n);
        for(int i = 0; i < n; i++) {
            byte[] hash = chunks.get(1 + i).data;
            if(hash == null || hash.length != 32) {
                return null;
            }
            if(i > 0 && compareUnsigned(hashes.get(i - 1), hash) >= 0) {
                return null;
            }
            hashes.add(hash);
        }
        return new Policy(threshold, hashes, n, algo, param);
    }

    private static Integer decodeSmallInt(ScriptChunk chunk) {
        if(chunk.data != null) {
            return null;
        }
        try {
            return Script.decodeFromOpN(chunk.opcode);
        } catch(Exception e) {
            return null;
        }
    }

    private static int compareUnsigned(byte[] left, byte[] right) {
        for(int i = 0; i < 32; i++) {
            int a = left[i] & 0xff;
            int b = right[i] & 0xff;
            if(a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }
}
