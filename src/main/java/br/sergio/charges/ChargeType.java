package br.sergio.charges;

import lombok.Getter;

import java.awt.Color;

@Getter
public enum ChargeType {

    ELECTRON(-1, 0xff0000, "-"),
    PROTON(1, 0x00c0ff, "+"),
    NEUTRON(0, 0x808080, " ");

    private final double sign;
    private final Color color;
    private final String symbol;

    ChargeType(double sign, int color, String symbol) {
        this.sign = sign;
        this.color = new Color(color);
        this.symbol = symbol;
    }

    public static ChargeType of(double value) {
        if (value > 0) {
            return PROTON;
        } else if (value < 0) {
            return ELECTRON;
        } else {
            return NEUTRON;
        }
    }

}
