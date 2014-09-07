package org.tastefuljava.gianadda.exif;

public class Rational extends Number implements Comparable<Rational> {
    private int numerator;
    private int denominator;

    public Rational(int num, int den) {
        numerator = num;
        denominator = den;
    }

    public Rational(int num) {
        this(num, 1);
    }

    @Override
    public int intValue() {
        return numerator/denominator;
    }

    @Override
    public long longValue() {
        return intValue();
    }

    @Override
    public float floatValue() {
        return (float)doubleValue();
    }

    @Override
    public double doubleValue() {
        return (double)numerator/(double)denominator;
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rational other = (Rational) obj;
        return numerator == other.numerator
                && this.denominator == other.denominator;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + numerator;
        hash = 29 * hash + denominator;
        return hash;
    }

    @Override
    public int compareTo(Rational t) {
        return Long.signum((long)numerator*(long)t.denominator
                - (long)t.numerator*(long)denominator);
    }
}
