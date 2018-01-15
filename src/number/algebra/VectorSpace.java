package number.algebra;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import utils.CacheMap;
import utils.Tuple;

public class VectorSpace<F extends FieldElement<F>> {

    private static final CacheMap<Tuple<Class<?>, Integer>, VectorSpace<?>> CACHE = new CacheMap<>();

    private final Class<F> coefficientClass;
    private final int dim;
    private final int hashCode;
    private final String toString;

    private VectorSpace(final Class<F> coefficientClass, int dim) {
        this.coefficientClass = coefficientClass;
        this.dim = dim;
        this.hashCode = this.coefficientClass.hashCode() ^ this.dim;
        this.toString = "VectorSpace(" + dim + ", " + coefficientClass.getCanonicalName() + ")";
    }

    public int dim() {
        return this.dim;
    }

    public VectorSpace<F>.Vector getWith(@SuppressWarnings("unchecked") final F... coeffs) {
        if (this.dim != coeffs.length) {
            throw new AlgebraicException("invalid length of coefficient");
        }
        if (this.coefficientClass != coeffs.getClass().getComponentType()) {
            throw new ClassCastException(coeffs.getClass().getComponentType().getName() + " cannot be cast to "
                    + this.coefficientClass.getName());
        }
        return new Vector(this, Arrays.copyOf(coeffs, this.dim));
    }

    public static <F extends FieldElement<F>> VectorSpace<F> of(Class<F> coefficientClass, int dim) {
        final Tuple<Class<?>, Integer> key = Tuple.of(coefficientClass, dim);
        final VectorSpace<?> cachedVal = CACHE.get(key);
        if (cachedVal != null && cachedVal.coefficientClass == coefficientClass) {
            @SuppressWarnings("unchecked")
            VectorSpace<F> ret = (VectorSpace<F>) cachedVal;
            return ret;
        }

        VectorSpace<?> newInstance = new VectorSpace<>(coefficientClass, dim);
        VectorSpace<?> isAbsent = CACHE.putIfAbsent(key, newInstance);
        if (isAbsent != null) {
            @SuppressWarnings("unchecked")
            VectorSpace<F> registeredByOtherThread = (VectorSpace<F>) isAbsent;
            return registeredByOtherThread;
        }

        @SuppressWarnings("unchecked")
        VectorSpace<F> ret = (VectorSpace<F>) newInstance;
        return ret;

    }

    public static <F extends FieldElement<F>> VectorSpace<F>.Vector createVectorWith(@SuppressWarnings("unchecked") F... coeffs) {

        final Class<F> coefficientClass;
        try {
            @SuppressWarnings("unchecked")
            final Class<F> coefficirntClass_ = (Class<F>) coeffs.getClass().getComponentType();
            coefficientClass = coefficirntClass_;
        } catch (ClassCastException e) {
            throw new InternalError("casting array class compoenent type, but failed.", e);
        }

        final VectorSpace<F> space = of(coefficientClass, coeffs.length);
        return space.getWith(coeffs);

    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof VectorSpace)) {
            return false;
        }
        VectorSpace<?> other = (VectorSpace<?>) o;
        return Objects.equals(this.coefficientClass, other.coefficientClass) && Objects.equals(this.dim, other.dim);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    public class Vector implements ModuleElement<Vector, F> {
        private final VectorSpace<F> space;
        private final F[] coeffs;
        private final AtomicReference<Vector> negate = new AtomicReference<>();

        /**
         * 
         * @param space
         *            このvectorが所属する{@link VectorSpace}インスタンス。
         * @param coeffs
         *            係数配列。この配列インスタンスが直接フィールドに設定されるため、外部から参照できないインスタンスを設定すること。
         */
        private Vector(final VectorSpace<F> space, final F[] coeffs) {
            this.space = space;
            this.coeffs = coeffs;
        }

        /**
         * このインスタンスの所属する{@link VectorSpace}インスタンス
         * 
         * @return
         */
        public VectorSpace<F> space() {
            return this.space;
        }

        /**
         * このvectorの次元数
         * 
         * @return
         */
        public int dim() {
            return this.space.dim;
        }

        @Override
        public Vector add(final Vector other) {
            F[] newCoeffs = Arrays.copyOf(this.coeffs, this.space.dim);
            for (int cnt = 0; cnt < this.space.dim; cnt++) {
                newCoeffs[cnt] = newCoeffs[cnt].add(other.coeffs[cnt]);
            }
            return getWith(newCoeffs);
        }

        @Override
        public Vector negate() {
            if (this.negate.get() == null) {
                F[] newCoeffs = Arrays.copyOf(this.coeffs, this.space.dim);
                for (int cnt = 0; cnt < this.space.dim; cnt++) {
                    newCoeffs[cnt] = newCoeffs[cnt].negate();
                }

                final Vector negate = getWith(newCoeffs);
                // まだ他のスレッドからは参照できないので直接setしてよい
                negate.negate.set(this);

                // このインスタンス(外部に公開済みの可能性有)のインスタンス変数に
                // 設定すると他スレッドに公開される
                this.negate.compareAndSet(null, negate);

                // CASが成功していても失敗していてもnegateフィールドから値を取得する
            }
            return this.negate.get();
        }

        @Override
        public Vector subtract(final Vector other) {
            F[] newCoeffs = Arrays.copyOf(this.coeffs, this.space.dim);
            for (int cnt = 0; cnt < this.space.dim; cnt++) {
                newCoeffs[cnt] = newCoeffs[cnt].subtract(other.coeffs[cnt]);
            }
            return getWith(newCoeffs);
        }

        @Override
        public boolean isZero() {
            for (F coeff : this.coeffs) {
                if (!coeff.isZero()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Vector scalarMultiply(final F scalar) {
            F[] newCoeffs = Arrays.copyOf(this.coeffs, this.space.dim);
            for (int cnt = 0; cnt < this.space.dim; cnt++) {
                newCoeffs[cnt] = newCoeffs[cnt].multiply(scalar);
            }
            return getWith(newCoeffs);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (!(o instanceof VectorSpace.Vector)) {
                return false;
            }
            VectorSpace<?>.Vector other = (VectorSpace<?>.Vector) o;
            if (!Objects.equals(this.space, other.space)) {
                return false;
            }
            return Arrays.deepEquals(this.coeffs, other.coeffs);
        }

        @Override
        public int hashCode() {
            return this.space.hashCode() ^ this.coeffs.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Vector(");
            boolean first = true;
            for (F coeff : this.coeffs) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append(coeff);
            }
            return builder //
                    .append(")") //
                    .toString();
        }

    }

}
