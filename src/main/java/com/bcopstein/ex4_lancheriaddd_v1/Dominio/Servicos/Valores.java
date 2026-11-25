package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Valores {
    private Valores() {}

    public static long percentual(long centavos, int taxa) {
        return BigDecimal.valueOf(centavos).multiply(BigDecimal.valueOf(taxa, 2))
                .setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
