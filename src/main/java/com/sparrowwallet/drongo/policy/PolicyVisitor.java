package com.sparrowwallet.drongo.policy;

public interface PolicyVisitor<T> {
    T visitSingleMldsa87();

    T visitMultiMldsa87();

    T visitSingleMldsa44();

    T visitMultiMldsa44();

    T visitSingleSegwit();

    T visitMultiSegwit();

    T visitSingleSp();
}
