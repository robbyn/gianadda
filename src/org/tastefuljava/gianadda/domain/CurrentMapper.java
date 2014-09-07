package org.tastefuljava.gianadda.domain;

public class CurrentMapper {
    private static final ThreadLocal<Mapper> CURRENT = new ThreadLocal<>();

    private CurrentMapper() {
        // Private constructor to prevent instanciation
    }

    public static Mapper get() {
        return CURRENT.get();
    }

    public static void set(Mapper mapper) {
        CURRENT.set(mapper);
    }
}
