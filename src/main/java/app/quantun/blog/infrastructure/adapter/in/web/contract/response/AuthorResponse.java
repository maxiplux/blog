package app.quantun.blog.infrastructure.adapter.in.web.contract.response;



import app.quantun.blog.domain.model.Author;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuthorResponse(
        String id,
        String name,
        String email,
        String bio,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static AuthorResponse fromDomain(Author author) {
        return AuthorResponse.builder()
                .id(author.getId().value())
                .name(author.getName())
                .email(author.getEmail().value())
                .bio(author.getBio())
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .build();
    }
}
