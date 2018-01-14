package use;

import number.algebra.VectorSpace;
import number.number.Rational;

public class UseVector {

    public static void main(String[] args) {
        VectorSpace<Rational> Q2 = VectorSpace.of(Rational.class, 2);
        VectorSpace<Rational>.Vector e1 = Q2.getWith(Rational.valueOf(1), Rational.valueOf(0));
        VectorSpace<Rational>.Vector e2 = Q2.getWith(Rational.valueOf(0), Rational.valueOf(1));
        System.out.println(e1);
        System.out.println(e2);
        VectorSpace<Rational>.Vector a1 = e1.add(e2);
        VectorSpace<Rational>.Vector a2 = e2.subtract(e1);
        System.out.println(a1);
        System.out.println(a2);
    }

}
