package app.quantun.blog.content.infrastructure.persistence;

import app.quantun.blog.content.domain.model.*;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {

    private final MongoArticleRepository mongoRepository;

    @Override
    public Article save(Article article) {
        return mongoRepository.save(article);
    }

    @Override
    public Optional<Article> findById(ArticleId articleId) {
        return mongoRepository.findById(articleId.getValue());
    }

    @Override
    public Optional<Article> findBySlug(Slug slug) {
        return mongoRepository.findBySlugValue(slug.getValue());
    }

    @Override
    public List<Article> findByAuthorId(AuthorId authorId) {
        return mongoRepository.findByAuthorIdValue(authorId.getValue());
    }

    @Override
    public List<Article> findByStatus(ArticleStatus status) {
        return mongoRepository.findByStatus(status.name());
    }

    @Override
    public List<Article> findByTag(String tag) {
        return mongoRepository.findByTagAndPublished(tag.toLowerCase().trim());
    }

    @Override
    public List<Article> findPublishedArticles(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return mongoRepository.findPublishedArticles(pageRequest).getContent();
    }

    @Override
    public List<Article> searchByTitle(String searchTerm) {
        return mongoRepository.findByTitleContainingIgnoreCase(searchTerm);
    }

    @Override
    public long countByAuthorId(AuthorId authorId) {
        return mongoRepository.countByAuthorIdValue(authorId.getValue());
    }

    @Override
    public void delete(ArticleId articleId) {
        mongoRepository.deleteById(articleId.getValue());
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return mongoRepository.existsBySlugValue(slug.getValue());
    }
}
