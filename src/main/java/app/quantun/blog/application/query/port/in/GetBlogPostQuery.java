package app.quantun.blog.application.query.port.in;

import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.shared.valueobject.Slug;

/**
 * Query Use Case para obtener blog posts individuales
 * Enfocado únicamente en operaciones de lectura
 */
public interface GetBlogPostQuery {

    /**
     * Obtiene un blog post por su ID
     *
     * @param postId ID del post
     * @return Blog post completo con toda la información
     */
    BlogPostReadModel getById(PostId postId);

    /**
     * Obtiene un blog post por su slug
     *
     * @param slug Slug del post
     * @return Blog post completo con toda la información
     */
    BlogPostReadModel getBySlug(Slug slug);
}
