package app.quantun.blog.content.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleSummaryResponse {
    private String id;
    private String title;
    private String slug;
    private String excerpt; // Primeros 150 caracteres del contenido
    private String authorId;
    private String status;
    private Set<String> tags;
    private LocalDateTime publishedAt;
    private int viewCount;
    private int wordCount;
}
