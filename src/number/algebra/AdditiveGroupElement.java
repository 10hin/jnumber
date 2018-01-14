package number.algebra;

public interface AdditiveGroupElement<A extends AdditiveGroupElement<A>> {

    A add(A other);

    A negate();

    default A subtract(A other) {
        return this.add(other.negate());
    }

    boolean isZero();

}
