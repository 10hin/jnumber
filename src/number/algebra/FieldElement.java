package number.algebra;

public interface FieldElement<F extends FieldElement<F>> extends RingElement<F> {

    F invert();

    default F divide(F other) {
        return this.multiply(other.invert());
    }

    boolean isOne();

}
