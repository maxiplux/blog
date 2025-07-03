package app.quantun.blog.application.query.service;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.application.query.port.in.GetBlogPostQuery;
import app.quantun.blog.application.query.port.in.SearchBlogPostQuery;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para queries de blog posts
 * Implementa todos los casos de uso de lectura con optimizaciones específicas
 * Incluye estrategias de cache para mejorar performance
 */
@Service
@Transactional(readOnly = true)
public class BlogPostQueryService implements GetBlogPostQuery, SearchBlogPostQuery {

    private static final Logger logger = LoggerFactory.getLogger(BlogPostQueryService.class);

    private final BlogPostQueryRepositoryPort queryRepository;

    public BlogPostQueryService(BlogPostQueryRepositoryPort queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Override
    @Cacheable(value = "blogpost-details", key = "#postId.value()")
    public BlogPostReadModel getById(PostId postId) {
        logger.debug("Querying blog post by ID: {} (cache miss expected)", postId.value());

        return queryRepository.findById(postId)
                .orElseThrow(() -> new BlogPostNotFoundException(
                        "Blog post not found with ID: " + postId.value()
                ));
    }

    @Override
    @Cacheable(value = "blogpost-details", key = "#slug.value()")
    public BlogPostReadModel getBySlug(Slug slug) {
        logger.debug("Querying blog post by slug: {} (cache miss expected)", slug.value());

        return queryRepository.findBySlug(slug)
                .orElseThrow(() -> new BlogPostNotFoundException(
                        "Blog post not found with slug: " + slug.value()
                ));
    }

    @Override
    @Cacheable(value = "blogpost-lists",
            key = "'published:' + #pageRequest.page() + ':' + #pageRequest.size() + ':' + #pageRequest.sortBy() + ':' + #pageRequest.sortDirection()")
    public PageResponse<BlogPostListItem> getAllPublished(PageRequest pageRequest) {
        logger.debug("Querying published posts with pagination: page={}, size={}, sortBy={} (cache miss expected)",
                pageRequest.page(), pageRequest.size(), pageRequest.sortBy());

        PageResponse<BlogPostListItem> result = queryRepository.findAllPublished(pageRequest);

        logger.debug("Found {} published posts out of {} total",
                result.content().size(), result.totalElements());

        return result;
    }

    @Override
    @Cacheable(value = "blogpost-lists",
            key = "'author:' + #authorId.value() + ':' + #pageRequest.page() + ':' + #pageRequest.size() + ':' + #pageRequest.sortBy()")
    public PageResponse<BlogPostListItem> getAllByAuthor(AuthorId authorId, PageRequest pageRequest) {
        logger.debug("Querying posts by author: {} with pagination: page={}, size={} (cache miss expected)",
                authorId.value(), pageRequest.page(), pageRequest.size());

        PageResponse<BlogPostListItem> result = queryRepository.findByAuthorId(authorId, pageRequest);

        logger.debug("Found {} posts for author {} out of {} total",
                result.content().size(), authorId.value(), result.totalElements());

        return result;
    }

    @Override
    @Cacheable(value = "blogpost-searches",
            key = "'search:' + #searchTerm + ':' + #pageRequest.page() + ':' + #pageRequest.size()",
            condition = "#searchTerm != null and #searchTerm.length() > 2")
    public PageResponse<BlogPostListItem> searchByTitleOrContent(String searchTerm, PageRequest pageRequest) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty search term, returning empty result");
            return PageResponse.empty(pageRequest);
        }

        String trimmedSearchTerm = searchTerm.trim();
        logger.debug("Searching posts by term: '{}' with pagination: page={}, size={} (cache miss expected)",
                trimmedSearchTerm, pageRequest.page(), pageRequest.size());

        PageResponse<BlogPostListItem> result = queryRepository.searchByTitleOrContent(trimmedSearchTerm, pageRequest);

        logger.debug("Found {} posts matching search term '{}' out of {} total",
                result.content().size(), trimmedSearchTerm, result.totalElements());

        return result;
    }

    @Override
    @Cacheable(value = "blogpost-lists",
            key = "'tag:' + #tagName + ':' + #pageRequest.page() + ':' + #pageRequest.size()",
            condition = "#tagName != null and #tagName.length() > 1")
    public PageResponse<BlogPostListItem> getByTag(String tagName, PageRequest pageRequest) {
        if (tagName == null || tagName.trim().isEmpty()) {
            logger.debug("Empty tag name, returning empty result");
            return PageResponse.empty(pageRequest);
        }

        String trimmedTagName = tagName.trim().toLowerCase();
        logger.debug("Querying posts by tag: '{}' with pagination: page={}, size={} (cache miss expected)",
                trimmedTagName, pageRequest.page(), pageRequest.size());

        PageResponse<BlogPostListItem> result = queryRepository.findByTagName(trimmedTagName, pageRequest);

        logger.debug("Found {} posts with tag '{}' out of {} total",
                result.content().size(), trimmedTagName, result.totalElements());

        return result;
    }

    /**
     * Método para obtener estadísticas (sin cache debido a naturaleza dinámica)
     */
    public QueryStatistics getQueryStatistics() {
        logger.debug("Generating query statistics (no cache)");

        long totalPublished = queryRepository.countPublishedPosts();

        return QueryStatistics.builder()
                .totalPublishedPosts(totalPublished)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Clase para estadísticas de queries
     */
    public record QueryStatistics(
            long totalPublishedPosts,
            java.time.LocalDateTime lastUpdated
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalPublishedPosts;
            private java.time.LocalDateTime lastUpdated;

            public Builder totalPublishedPosts(long totalPublishedPosts) {
                this.totalPublishedPosts = totalPublishedPosts;
                return this;
            }

            public Builder lastUpdated(java.time.LocalDateTime lastUpdated) {
                this.lastUpdated = lastUpdated;
                return this;
            }

            public QueryStatistics build() {
                return new QueryStatistics(totalPublishedPosts, lastUpdated);
            }
        }
    }
}
