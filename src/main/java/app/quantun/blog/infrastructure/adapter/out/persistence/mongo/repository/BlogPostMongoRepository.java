package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository;

import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostMongoRepository extends MongoRepository<BlogPostEntity, String> {

    // ===== MÉTODOS EXISTENTES =====
    Optional<BlogPostEntity> findBySlug(String slug);
    List<BlogPostEntity> findByStatus(String status);
    List<BlogPostEntity> findByAuthorId(String authorId);

    boolean existsBySlug(String slug);

    @Query("{ 'tags.name': ?0 }")
    List<BlogPostEntity> findByTagsName(String tagName);

    // ===== NUEVOS MÉTODOS PARA CQRS CON PAGINACIÓN =====

    /**
     * Encuentra posts por status con paginación
     */
    Page<BlogPostEntity> findByStatus(String status, Pageable pageable);

    /**
     * Encuentra posts por autor con paginación
     */
    Page<BlogPostEntity> findByAuthorId(String authorId, Pageable pageable);

    /**
     * Encuentra posts por tag con paginación
     */
    @Query("{ 'tags.name': ?0 }")
    Page<BlogPostEntity> findByTagsName(String tagName, Pageable pageable);

    /**
     * Búsqueda de texto en título o contenido con paginación
     */
    Page<BlogPostEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    /**
     * Búsqueda avanzada con múltiples criterios
     */
    @Query("{ $or: [ " +
            "{ 'title': { $regex: ?0, $options: 'i' } }, " +
            "{ 'content': { $regex: ?0, $options: 'i' } }, " +
            "{ 'summary': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<BlogPostEntity> searchByTerm(String searchTerm, Pageable pageable);

    // ===== MÉTODOS DE CONTEO PARA ESTADÍSTICAS =====

    /**
     * Cuenta posts por status
     */
    long countByStatus(String status);

    /**
     * Cuenta posts por autor
     */
    long countByAuthorId(String authorId);

    /**
     * Cuenta posts por tag
     */
    @Query(value = "{ 'tags.name': ?0 }", count = true)
    long countByTagName(String tagName);
}
