package app.quantun.blog.content.infrastructure.persistence;

import app.quantun.blog.content.domain.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoArticleRepository extends MongoRepository<Article, String> {

    Optional<Article> findBySlugValue(String slug);

    List<Article> findByAuthorIdValue(String authorId);

    List<Article> findByStatus(String status);

    @Query("{ 'tags': ?0, 'status': 'PUBLISHED' }")
    List<Article> findByTagAndPublished(String tag);

    @Query("{ 'status': 'PUBLISHED' }")
    Page<Article> findPublishedArticles(Pageable pageable);

    @Query("{ 'title.value': { $regex: ?0, $options: 'i' } }")
    List<Article> findByTitleContainingIgnoreCase(String searchTerm);

    long countByAuthorIdValue(String authorId);

    boolean existsBySlugValue(String slug);
}
