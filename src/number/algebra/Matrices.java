package number.algebra;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import utils.CacheMap;
import utils.Tuple;

public class Matrices<R extends RingElement<R>> {

    private static final CacheMap<Tuple<Class<?>, Tuple<Integer, Integer>>, Matrices<?>> CACHE = new CacheMap<>();

    private final Class<R> coefficientClass;
    private final Class<R[]> rowArrayClass;
    private final int rowSize;
    private final int columnSize;

    private Matrices(final Class<R> coefficientClass, final int rowSize, final int columnSize) {
        this.coefficientClass = coefficientClass;
        @SuppressWarnings("unchecked")
        final Class<R[]> rowArrayClass = (Class<R[]>) Array.newInstance(coefficientClass, 0).getClass();
        this.rowArrayClass = rowArrayClass;
        this.columnSize = columnSize;
        this.rowSize = rowSize;
    }

    public Matrix getWith(final R[][] coeffs) {
        @SuppressWarnings("unchecked")
        final R[][] defensiveCopy = (R[][]) Array.newInstance(this.rowArrayClass, coeffs.length);
        if (coeffs.length != this.rowSize) {
            throw new IllegalArgumentException(
                    "coefficients has unmatch row size: " + coeffs.length + ", need: " + this.rowSize);
        }
        for (int idx = 0; idx < rowSize; idx++) {
            R[] row = coeffs[idx];
            if (row.length != this.columnSize) {
                throw new IllegalArgumentException("coefficients has row " + idx + " that has unmmatch column size: "
                        + row.length + ", need: " + this.columnSize);
            }
            defensiveCopy[idx] = Arrays.copyOf(row, row.length);
        }
        return new Matrix(this, defensiveCopy);
    }

    public static <R extends RingElement<R>> Matrices<R> of(final Class<R> coefficientClass, final int rowSize,
            final int columnSize) {
        final Tuple<Integer, Integer> size = Tuple.of(rowSize, columnSize);
        final Tuple<Class<?>, Tuple<Integer, Integer>> key = Tuple.of(coefficientClass, size);
        final Matrices<?> cachedVal = CACHE.get(key);
        if (cachedVal != null && cachedVal.coefficientClass == coefficientClass) {
            @SuppressWarnings("unchecked")
            Matrices<R> ret = (Matrices<R>) cachedVal;
            return ret;
        }
        Matrices<?> newInstance = new Matrices<>(coefficientClass, rowSize, columnSize);
        Matrices<?> isAbsent = CACHE.putIfAbsent(key, newInstance);
        if (isAbsent != null) {
            @SuppressWarnings("unchecked")
            Matrices<R> registerdByOtherThread = (Matrices<R>) isAbsent;
            return registerdByOtherThread;
        }
        @SuppressWarnings("unchecked")
        Matrices<R> ret = (Matrices<R>) newInstance;
        return ret;
    }

    public static <R extends RingElement<R>> Matrices<R>.Matrix createMatrixWith(final R[][] coeffs) {

        final Class<R> coefficientClass;
        try {
            @SuppressWarnings("unchecked")
            final Class<R[]> rowClass = (Class<R[]>) coeffs.getClass().getComponentType();

            @SuppressWarnings("unchecked")
            final Class<R> coefficientClass_ = (Class<R>) rowClass.getComponentType();

            coefficientClass = coefficientClass_;
        } catch (ClassCastException e) {
            throw new InternalError("casting array class compoenent type, but failed.", e);
        }

        final Matrices<R> matrices = of(coefficientClass, coeffs.length, coeffs[0].length);
        return matrices.getWith(coeffs);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VectorSpace)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final Matrices other = (Matrices) obj;
        return this.coefficientClass == other.coefficientClass //
                && this.rowArrayClass == other.rowArrayClass //
                && this.columnSize == other.columnSize //
                && this.rowSize == other.rowSize;
    }

    public class Matrix implements ModuleElement<Matrix, R> {
        private final Matrices<R> matrices;
        private final R[][] coeffs;
        private final AtomicReference<Matrix> negate = new AtomicReference<>();
        private final AtomicReference<Integer> hashCode = new AtomicReference<>();
        private final AtomicReference<String> toString = new AtomicReference<>();

        private Matrix(final Matrices<R> matrices, final R[][] coeffs) {
            this.matrices = matrices;
            this.coeffs = coeffs;
        }

        public Matrices<R> matrices() {
            return this.matrices;
        }

        public int rowSize() {
            return this.matrices.rowSize;
        }

        public int columnSize() {
            return this.matrices.columnSize;
        }

        private void checkAndThrow(final Matrix other) {
            if (!this.matrices().equals(other.matrices())) {
                if (this.matrices().coefficientClass != other.matrices().coefficientClass) {
                    throw new IllegalArgumentException("matrix cannot add to different coefficient class matrix "
                            + this.matrices.coefficientClass.getCanonicalName() + " != "
                            + other.matrices.coefficientClass.getCanonicalName());
                }
                throw new IllegalArgumentException("matrix cannot add to different size (" + this.matrices.rowSize
                        + ", " + this.matrices.columnSize + ") != (" + other.matrices.rowSize + ", "
                        + other.matrices.columnSize + ")");
            }
        }

        @Override
        public Matrix add(final Matrices<R>.Matrix other) {
            checkAndThrow(other);
            @SuppressWarnings("unchecked")
            final R[][] newCoeffs = (R[][]) Array.newInstance(this.matrices.rowArrayClass, this.matrices.rowSize);
            for (int i = 0; i < this.matrices.rowSize; i++) {
                newCoeffs[i] = Arrays.copyOf(this.coeffs[i], this.coeffs[i].length);
                for (int j = 0; j < this.matrices.columnSize; j++) {
                    newCoeffs[i][j] = newCoeffs[i][j].add(other.coeffs[i][j]);
                }
            }
            return getWith(newCoeffs);
        }

        @Override
        public Matrix negate() {
            if (this.negate.get() == null) {
                @SuppressWarnings("unchecked")
                final R[][] newCoeffs = (R[][]) Array.newInstance(this.matrices.rowArrayClass, this.matrices.rowSize);
                for (int i = 0; i < this.matrices.rowSize; i++) {
                    newCoeffs[i] = Arrays.copyOf(this.coeffs[i], this.coeffs[i].length);
                    for (int j = 0; j < this.matrices.columnSize; j++) {
                        newCoeffs[i][j] = newCoeffs[i][j].negate();
                    }
                }

                final Matrix negate = getWith(newCoeffs);
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
        public Matrix subtract(final Matrices<R>.Matrix other) {
            checkAndThrow(other);
            @SuppressWarnings("unchecked")
            final R[][] newCoeffs = (R[][]) Array.newInstance(this.matrices.rowArrayClass, this.matrices.rowSize);
            for (int i = 0; i < this.matrices.rowSize; i++) {
                newCoeffs[i] = Arrays.copyOf(this.coeffs[i], this.coeffs[i].length);
                for (int j = 0; j < this.matrices.columnSize; j++) {
                    newCoeffs[i][j] = newCoeffs[i][j].subtract(other.coeffs[i][j]);
                }
            }
            return getWith(newCoeffs);
        }

        @Override
        public boolean isZero() {
            for (int i = 0; i < this.matrices.rowSize; i++) {
                for (int j = 0; j < this.matrices.columnSize; j++) {
                    if (!this.coeffs[i][j].isZero()) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public Matrix scalarMultiply(final R scalar) {
            @SuppressWarnings("unchecked")
            final R[][] newCoeffs = (R[][]) Array.newInstance(this.matrices.rowArrayClass, this.matrices.rowSize);
            for (int i = 0; i < this.matrices.rowSize; i++) {
                newCoeffs[i] = Arrays.copyOf(this.coeffs[i], this.coeffs[i].length);
                for (int j = 0; j < this.matrices.columnSize; j++) {
                    newCoeffs[i][j] = newCoeffs[i][j].multiply(scalar);
                }
            }
            return getWith(newCoeffs);
        }

        public Matrix multiply(Matrix other) {
            if (this.matrices.columnSize != other.matrices.rowSize) {
                throw new IllegalArgumentException("not multiplicatable matrix this has column size "
                        + this.matrices.columnSize + ", but other matrix has row size " + other.matrices.rowSize
                        + "(other must have row size equal to this column size)");
            }

            Matrices<R> resultsMatrices = of(this.matrices.coefficientClass, this.matrices.rowSize,
                    other.matrices.columnSize);

            @SuppressWarnings("unchecked")
            R[][] resultsCoeffs = (R[][]) Array.newInstance(resultsMatrices.coefficientClass, resultsMatrices.rowSize,
                    resultsMatrices.columnSize);

            for (int i = 0; i < resultsMatrices.rowSize; i++) {
                for (int j = 0; j < resultsMatrices.columnSize; j++) {
                    R sum = null;
                    for (int k = 0; k < this.matrices.columnSize; k++) {
                        if (sum == null) {
                            sum = this.coeffs[i][k].multiply(other.coeffs[k][j]);
                        } else {
                            sum = sum.add(this.coeffs[i][k].multiply(other.coeffs[k][j]));
                        }
                    }
                    resultsCoeffs[i][j] = sum;
                }
            }

            return resultsMatrices.getWith(resultsCoeffs);

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (!(this.getClass().isInstance(obj))) {
                return false;
            }
            @SuppressWarnings("unchecked")
            Matrix other = (Matrix) obj;
            if (!this.matrices.equals(other.matrices)) {
                return false;
            }
            for (int i = 0; i < this.matrices.rowSize; i++) {
                for (int j = 0; j < this.matrices.columnSize; j++) {
                    if (!this.coeffs[i][j].equals(other.coeffs[i][j])) {
                        return false;
                    }
                }
            }

            return true;

        }

        @Override
        public int hashCode() {
            if (this.hashCode.get() == null) {
                this.hashCode.compareAndSet(null, this.matrices.hashCode() ^ this.coeffs.hashCode());
            }
            return this.hashCode.get().intValue();
        }

        @Override
        public String toString() {
            if (this.toString.get() == null) {
                final String toString = constructString(this);

                this.toString.compareAndSet(null, toString);
            }
            return this.toString.get();
        }

    }

    private static <R extends RingElement<R>> String constructString(final Matrices<R>.Matrix self) {

        final int[] columnWidth = new int[self.matrices.columnSize];
        Arrays.fill(columnWidth, 0);

        for (int i = 0; i < self.matrices.rowSize; i++) {
            for (int j = 0; j < self.matrices.columnSize; j++) {
                int columnLength = self.coeffs[i][j].toString().length();
                columnWidth[j] = Math.max(columnLength, columnWidth[j]);
            }
        }

        StringBuilder builder = new StringBuilder();
        boolean firstRow = true;
        for (int i = 0; i < self.matrices.rowSize; i++) {
            if (firstRow) {
                firstRow = false;
            } else {
                builder.append(System.lineSeparator());
            }
            builder.append("[");
            boolean firstColumn = true;
            for (int j = 0; j < self.matrices.columnSize; j++) {
                if (firstColumn) {
                    firstColumn = false;
                } else {
                    builder.append(" ");
                }
                builder.append(String.format("%" + columnWidth[j] + "s", self.coeffs[i][j].toString()));
            }
            builder.append("]");
        }

        return builder.toString();

    }

}
