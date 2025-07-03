package app.quantun.blog.application.command.port.in;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;

/**
 * Command Use Case para publicar blog posts
 * Maneja la transición de estado de borrador a publicado
 */
public interface PublishBlogPostCommand {

    /**
     * Publica un blog post (cambia estado de DRAFT a PUBLISHED)
     *
     * @param postId ID del post a publicar
     * @return Blog post publicado
     */
    BlogPost publishPost(PostId postId);

    /**
     * Archiva un blog post (cambia estado a ARCHIVED)
     *
     * @param postId ID del post a archivar
     * @return Blog post archivado
     */
    BlogPost archivePost(PostId postId);
}
