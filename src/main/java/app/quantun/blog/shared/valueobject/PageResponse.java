package app.quantun.blog.shared.valueobject;

import java.util.List;

/**
 * Value object para respuestas paginadas
 * Contiene tanto los datos como metadata de paginación
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public PageResponse {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements cannot be negative");
        }
    }

    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
        boolean hasNext = pageRequest.page() + 1 < totalPages;
        boolean hasPrevious = pageRequest.page() > 0;

        return new PageResponse<>(
                content,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }

    public static <T> PageResponse<T> empty(PageRequest pageRequest) {
        return new PageResponse<>(
                List.of(),
                pageRequest.page(),
                pageRequest.size(),
                0L,
                0,
                false,
                false
        );
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}
