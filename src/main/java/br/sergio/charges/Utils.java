package br.sergio.charges;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static String formatDouble(double n, int precision) {
        if (n == 0) {
            return "0";
        }

        if (n == (long) n) {
            return String.valueOf((long) n);
        }

        String nStr = String.valueOf(n);
        boolean useScientific = nStr.contains("E");

        if (!useScientific) {
            if (n < Integer.MAX_VALUE) {
                BigDecimal bd = BigDecimal.valueOf(n)
                        .setScale(precision, RoundingMode.HALF_UP)
                        .stripTrailingZeros();
                return bd.toPlainString().replace('.', ',');
            }
            return toScientific(n, precision, nStr + "E0");
        }

        return toScientific(n, precision, nStr);
    }

    public static String formatDouble(double n) {
        if (n == (long) n) {
            return String.valueOf((long) n);
        }
        return String.valueOf(n);
    }

    private static String toScientific(double n, int precision, String nStr) {
        int exponent = Integer.parseInt(nStr.substring(nStr.indexOf('E') + 1));
        double significand = n / Math.pow(10, exponent);

        BigDecimal bd = BigDecimal.valueOf(significand)
                .setScale(precision, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        String significandStr = bd.toPlainString().replace('.', ',');

        return significandStr + "·10" + toSuperscript(exponent);
    }

    private static String toSuperscript(int n) {
        String normal = String.valueOf(Math.abs(n));
        StringBuilder sb = new StringBuilder();

        if (n < 0) {
            sb.append('⁻');
        }

        for (char c : normal.toCharArray()) {
            sb.append(digitToSuperscript(c));
        }

        return sb.toString();
    }

    private static char digitToSuperscript(char digit) {
        return switch (digit) {
            case '0' -> '⁰';
            case '1' -> '¹';
            case '2' -> '²';
            case '3' -> '³';
            case '4' -> '⁴';
            case '5' -> '⁵';
            case '6' -> '⁶';
            case '7' -> '⁷';
            case '8' -> '⁸';
            case '9' -> '⁹';
            default -> throw new IllegalArgumentException("Not a digit: " + digit);
        };
    }

    public static Vector fixPosition(Game game, double x, double y) {
        Dimension size = game.getSize();
        if(x < Game.CHARGE_RADIUS) {
            x = Game.CHARGE_RADIUS;
        } else if(x > size.width - Game.CHARGE_RADIUS) {
            x = size.width - Game.CHARGE_RADIUS;
        }
        if(y < Game.CHARGE_RADIUS) {
            y = Game.CHARGE_RADIUS;
        } else if(y > size.height - Game.CHARGE_RADIUS) {
            y = size.height - Game.CHARGE_RADIUS;
        }
        return new Vector(x, y);
    }

}
