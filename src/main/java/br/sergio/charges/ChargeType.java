package br.sergio.charges;

import lombok.Getter;

import java.awt.Color;

@Getter
public enum ChargeType {

    NEGATIVE(-1, new Color(0xff0000), "-"),
    POSITIVE(1, new Color(0x00c0ff), "+"),
    NEUTRAL(0, new Color(0x808080), " ");

    private final double sign;
    private final Color color;
    private final String symbol;

    ChargeType(double sign, Color color, String symbol) {
        this.sign = sign;
        this.color = color;
        this.symbol = symbol;
    }

    public static ChargeType of(double value) {
        if (value > 0) {
            return POSITIVE;
        } else if (value < 0) {
            return NEGATIVE;
        } else {
            return NEUTRAL;
        }
    }

}
