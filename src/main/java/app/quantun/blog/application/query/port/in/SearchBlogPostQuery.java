package app.quantun.blog.application.query.port.in;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;

/**
 * Query Use Case para búsquedas y listados paginados de blog posts
 * Optimizado para operaciones de consulta con filtros y paginación
 */
public interface SearchBlogPostQuery {

    /**
     * Obtiene todos los posts publicados con paginación
     *
     * @param pageRequest Parámetros de paginación y ordenamiento
     * @return Página de posts publicados
     */
    PageResponse<BlogPostListItem> getAllPublished(PageRequest pageRequest);

    /**
     * Obtiene todos los posts de un autor específico con paginación
     *
     * @param authorId    ID del autor
     * @param pageRequest Parámetros de paginación y ordenamiento
     * @return Página de posts del autor
     */
    PageResponse<BlogPostListItem> getAllByAuthor(AuthorId authorId, PageRequest pageRequest);

    /**
     * Busca posts por título o contenido con paginación
     *
     * @param searchTerm  Término de búsqueda
     * @param pageRequest Parámetros de paginación y ordenamiento
     * @return Página de posts que coinciden con la búsqueda
     */
    PageResponse<BlogPostListItem> searchByTitleOrContent(String searchTerm, PageRequest pageRequest);

    /**
     * Obtiene posts por tag con paginación
     *
     * @param tagName     Nombre del tag
     * @param pageRequest Parámetros de paginación y ordenamiento
     * @return Página de posts con el tag especificado
     */
    PageResponse<BlogPostListItem> getByTag(String tagName, PageRequest pageRequest);
}
