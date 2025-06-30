package app.quantun.blog.content.domain.repository;

import app.quantun.blog.content.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(ArticleId articleId);

    Optional<Article> findBySlug(Slug slug);

    List<Article> findByAuthorId(AuthorId authorId);

    List<Article> findByStatus(ArticleStatus status);

    List<Article> findByTag(String tag);

    List<Article> findPublishedArticles(int page, int size);

    List<Article> searchByTitle(String searchTerm);

    long countByAuthorId(AuthorId authorId);

    void delete(ArticleId articleId);

    boolean existsBySlug(Slug slug);
}
