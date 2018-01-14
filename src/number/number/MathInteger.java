package number.number;

import java.io.Serializable;
import java.math.BigInteger;

import number.algebra.RingElement;

public class MathInteger implements RingElement<MathInteger>, Comparable<MathInteger>, Serializable {

    private static final long serialVersionUID = -4405178281199349765L;

    private final BigInteger value;

    static final MathInteger ZERO = MathInteger.valueOf(BigInteger.ZERO);
    static final MathInteger ONE = MathInteger.valueOf(BigInteger.ONE);

    private MathInteger(final BigInteger value) {
        this.value = value;
    }

    public static MathInteger valueOf(final BigInteger i) {
        return new MathInteger(i);
    }

    public static MathInteger valueOf(long i) {
        return valueOf(BigInteger.valueOf(i));
    }

    @Override
    public MathInteger add(MathInteger other) {
        return valueOf(this.value.add(other.value));
    }

    @Override
    public MathInteger negate() {
        return valueOf(this.value.negate());
    }

    @Override
    public MathInteger subtract(MathInteger other) {
        return valueOf(this.value.subtract(other.value));
    }

    @Override
    public MathInteger multiply(MathInteger other) {
        return valueOf(this.value.multiply(other.value));
    }

    public MathInteger abs() {
        if (this.compareTo(ZERO) < 0) {
            return this.negate();
        } else {
            return this;
        }
    }

    public MathInteger divide(final MathInteger divisor) {
        return valueOf(this.value.divide(divisor.value));
    }

    public MathInteger max(final MathInteger other) {
        if (this.compareTo(other) < 0) {
            return other;
        } else {
            return this;
        }
    }

    public MathInteger min(final MathInteger other) {
        if (this.compareTo(other) > 0) {
            return other;
        } else {
            return this;
        }
    }

    public MathInteger gcd(final MathInteger other) {
        return valueOf(this.value.gcd(other.value));
    }

    @Override
    public int compareTo(final MathInteger other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof MathInteger)) {
            return false;
        }
        MathInteger other = (MathInteger) o;
        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean isZero() {
        return this.equals(ZERO);
    }

    public boolean isPositive() {
        return this.compareTo(ZERO) > 0;
    }

    public boolean isNegative() {
        return this.compareTo(ZERO) < 0;
    }

}
