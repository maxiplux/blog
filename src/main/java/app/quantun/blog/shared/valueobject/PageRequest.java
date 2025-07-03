package app.quantun.blog.shared.valueobject;

import java.util.Objects;

/**
 * Value object para requests de paginación con ordenamiento
 * Inmutable y con validaciones integradas
 */
public record PageRequest(
        int page,
        int size,
        String sortBy,
        SortDirection sortDirection
) {
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        Objects.requireNonNull(sortBy, "Sort field cannot be null");
        Objects.requireNonNull(sortDirection, "Sort direction cannot be null");
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, "createdAt", SortDirection.DESC);
    }

    public static PageRequest of(int page, int size, String sortBy, SortDirection sortDirection) {
        return new PageRequest(page, size, sortBy, sortDirection);
    }

    public static PageRequest defaultRequest() {
        return new PageRequest(0, 10, "createdAt", SortDirection.DESC);
    }

    public int offset() {
        return page * size;
    }

    public enum SortDirection {
        ASC, DESC
    }
}
