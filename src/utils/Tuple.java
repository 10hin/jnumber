package utils;

import java.util.Objects;

public final class Tuple<CAR, CDR> {

    private final CAR car;
    private final CDR cdr;

    private Tuple(CAR car, CDR cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public CAR car() {
        return this.car;
    }

    public CDR cdr() {
        return this.cdr;
    }

    @Override
    public String toString() {
        return "(" + Objects.toString(car) + ", " + Objects.toString(cdr) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tuple)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Tuple other = (Tuple) obj;
        return Objects.equals(this.car, other.car) && Objects.equals(this.cdr, other.cdr);
    }

    public int hashCode() {
        int rotated = Integer.rotateRight(this.cdr.hashCode(), 7);
        return this.car.hashCode() ^ rotated;
    }

    public static <CAR, CDR> Tuple<CAR, CDR> of(CAR car, CDR cdr) {
        return new Tuple<CAR, CDR>(car, cdr);
    }

}
