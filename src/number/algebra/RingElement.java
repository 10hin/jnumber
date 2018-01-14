package number.algebra;

public interface RingElement<R extends RingElement<R>> extends AdditiveGroupElement<R> {

    R multiply(R other);

}
