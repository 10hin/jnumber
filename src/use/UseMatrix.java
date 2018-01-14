package use;

import java.util.Arrays;

import number.algebra.Matrices;
import number.number.MathInteger;

public class UseMatrix {

    public static void main(String[] args) {

        Matrices<MathInteger> m23 = Matrices.of(MathInteger.class, 2, 3);
        Matrices<MathInteger> m34 = Matrices.of(MathInteger.class, 3, 4);

        Matrices<MathInteger>.Matrix a = m23.getWith(mathIntegerOf(new long[][] {
            new long[] {1, 2, 3},
            new long[] {4, 5, 6},
        }));

        Matrices<MathInteger>.Matrix b = m34.getWith(mathIntegerOf(new long[][] {
            { 1,  2,  3,  4},
            { 5,  6,  7,  8},
            { 9, 10, 11, 12},
        }));

        Matrices<MathInteger>.Matrix c = a.multiply(b);
        System.out.println(c);

        Matrices<MathInteger>.Matrix d = Matrices.createMatrixWith( //
                mathIntegerOf(new long[][] {
                    new long[] {9, 8, 7},
                    new long[] {6, 5, 4},
                }));

        

    }

    private static MathInteger[][] mathIntegerOf(final long[][] array) {
        return Arrays.stream(array) //
                .map((row) -> Arrays.stream(row) //
                        .mapToObj(l -> MathInteger.valueOf(l)) //
                        .toArray((n) -> new MathInteger[n])) //
                .toArray((n) -> new MathInteger[n][]);
    }

}
