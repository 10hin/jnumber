package number.number;

import java.io.Serializable;

import number.algebra.FieldElement;

public class Rational implements FieldElement<Rational>, Comparable<Rational>, Serializable {

    private static final long serialVersionUID = 7125251676225058831L;
    public static final Rational ZERO = Rational.valueOf(MathInteger.ZERO, MathInteger.ONE);
    public static final Rational ONE = Rational.valueOf(MathInteger.ONE, MathInteger.ONE);

    private final MathInteger num;
    private final MathInteger den;

    private Rational(final MathInteger num, final MathInteger den) {
        if (den.compareTo(MathInteger.ZERO) == 0) {
            throw new ArithmeticException("division by zero");
        }
        if (num.compareTo(MathInteger.ZERO) == 0) {
            this.num = MathInteger.ZERO;
            this.den = MathInteger.ONE;
        } else {
            final MathInteger gcd = num.gcd(den);
            if (den.isPositive()) {
                this.num = num.divide(gcd);
                this.den = den.divide(gcd);
            } else {
                this.num = num.negate().divide(gcd);
                this.den = den.negate().divide(gcd);
            }
        }
    }

    public static Rational valueOf(final MathInteger num, final MathInteger den) {
        return new Rational(num, den);
    }

    public static Rational valueOf(final MathInteger integer) {
        return valueOf(integer, MathInteger.ONE);
    }

    public static Rational valueOf(final long num, final long den) {
        return valueOf(MathInteger.valueOf(num), MathInteger.valueOf(den));
    }

    public static Rational valueOf(final long integer) {
        return valueOf(MathInteger.valueOf(integer));
    }

    public static Rational valueOf(final Long num, final Long den) {
        return valueOf(num.longValue(), den.longValue());
    }

    public static Rational valueOf(final Long integer) {
        return valueOf(integer.longValue());
    }

    public static Rational valueOf(final Integer num, final Integer den) {
        return valueOf(num.longValue(), den.longValue());
    }

    public static Rational valueOf(final Integer integer) {
        return valueOf(integer.longValue());
    }

    public static Rational valueOf(final Short num, final Short den) {
        return valueOf(num.longValue(), den.longValue());
    }

    public static Rational valueOf(final Short integer) {
        return valueOf(integer.longValue());
    }

    public static Rational valueOf(final Byte num, final Byte den) {
        return valueOf(num.longValue(), den.longValue());
    }

    public static Rational valueOf(final Byte integer) {
        return valueOf(integer.longValue());
    }

    @Override
    public Rational multiply(final Rational other) {
        return valueOf(this.num.multiply(other.num), this.den.multiply(other.den));
    }

    @Override
    public Rational add(Rational other) {
        return valueOf( //
                this.num.multiply(other.den).add(this.den.multiply(other.num)), //
                this.den.multiply(other.den));
    }

    @Override
    public Rational negate() {
        return valueOf(this.num.negate(), this.den);
    }

    @Override
    public Rational subtract(Rational other) {
        return valueOf( //
                this.num.multiply(other.den).subtract(this.den.multiply(other.num)), //
                this.den.multiply(other.den));
    }

    @Override
    public Rational invert() {
        if (this.num.isZero()) {
            throw new ArithmeticException("division by zero");
        }
        return valueOf(this.den, this.num);
    }

    @Override
    public Rational divide(Rational other) {
        if (other.isZero()) {
            throw new ArithmeticException("division by zero");
        }
        return valueOf( //
                this.num.multiply(other.den), //
                this.den.multiply(other.num));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Rational)) {
            return false;
        }
        Rational other = (Rational) o;
        return this.num.equals(other.num) && this.den.equals(other.den);
    }

    @Override
    public int hashCode() {
        return this.num.hashCode() ^ Integer.rotateLeft(this.den.hashCode(), 16);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.num.toString());
        if (!this.den.equals(MathInteger.ONE)) {
            builder.append("/");
            builder.append(this.den.toString());
        }
        return builder.toString();
    }

    @Override
    public boolean isZero() {
        return this.equals(ZERO);
    }

    @Override
    public boolean isOne() {
        return this.equals(ONE);
    }

    public boolean isPositive() {
        return this.compareTo(ZERO) > 0;
    }

    public boolean isNegative() {
        return this.compareTo(ZERO) < 0;
    }

    @Override
    public int compareTo(Rational other) {
        return this.num.multiply(other.den).compareTo(this.den.multiply(other.num));
    }

}
