package app.quantun.blog.application.command.port.in;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;

import java.util.Set;

/**
 * Command Use Case para actualizar blog posts
 * Maneja la lógica de negocio para modificaciones de contenido
 */
public interface UpdateBlogPostCommand {

    /**
     * Actualiza el contenido de un blog post
     *
     * @param command Datos del comando de actualización
     * @return Blog post actualizado
     */
    BlogPost updateBlogPost(UpdateBlogPostCommandData command);

    /**
     * Datos del comando para actualizar un blog post
     */
    record UpdateBlogPostCommandData(
            PostId postId,
            String title,
            String content,
            String summary,
            Set<String> tagNames
    ) {
        public UpdateBlogPostCommandData {
            if (postId == null) {
                throw new IllegalArgumentException("Post ID cannot be null");
            }
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }
            // summary y tagNames pueden ser null
        }
    }
}
