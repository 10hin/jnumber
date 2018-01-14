package number.algebra;

public interface ModuleElement<M extends ModuleElement<M, R>, R extends RingElement<R>> extends AdditiveGroupElement<M> {

    M scalarMultiply(R scalar);

}
