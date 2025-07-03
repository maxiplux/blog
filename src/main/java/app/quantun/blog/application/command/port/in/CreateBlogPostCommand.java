package app.quantun.blog.application.command.port.in;

import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;

import java.util.Set;

/**
 * Command Use Case para crear blog posts
 * Enfocado únicamente en operaciones de escritura y lógica de negocio
 */
public interface CreateBlogPostCommand {

    /**
     * Crea un nuevo blog post como borrador
     *
     * @param command Datos del comando de creación
     * @return Blog post creado
     */
    BlogPost createBlogPost(CreateBlogPostCommandData command);

    /**
     * Datos del comando para crear un blog post
     * Record inmutable con validaciones integradas
     */
    record CreateBlogPostCommandData(
            String title,
            String content,
            String summary,
            AuthorId authorId,
            Set<String> tagNames
    ) {
        public CreateBlogPostCommandData {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }
            if (authorId == null) {
                throw new IllegalArgumentException("Author ID cannot be null");
            }
            // tagNames puede ser null o vacío
        }
    }
}
