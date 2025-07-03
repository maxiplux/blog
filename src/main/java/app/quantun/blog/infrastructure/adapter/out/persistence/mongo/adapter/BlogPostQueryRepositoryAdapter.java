package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.application.query.model.BlogPostListItem;
import app.quantun.blog.application.query.model.BlogPostReadModel;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.AuthorEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.AuthorMongoRepository;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import app.quantun.blog.shared.valueobject.PageRequest;
import app.quantun.blog.shared.valueobject.PageResponse;
import app.quantun.blog.shared.valueobject.Slug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter para operaciones de consulta (query) de blog posts
 * Implementa patrones de lectura optimizados para CQRS
 */
@Component
public class BlogPostQueryRepositoryAdapter implements BlogPostQueryRepositoryPort {

    private final BlogPostMongoRepository blogPostRepository;
    private final AuthorMongoRepository authorRepository;
    private final BlogPostEntityMapper entityMapper;

    public BlogPostQueryRepositoryAdapter(BlogPostMongoRepository blogPostRepository,
                                          AuthorMongoRepository authorRepository,
                                          BlogPostEntityMapper entityMapper) {
        this.blogPostRepository = blogPostRepository;
        this.authorRepository = authorRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    public Optional<BlogPostReadModel> findById(PostId postId) {
        return blogPostRepository.findById(postId.value())
                .map(this::toReadModel);
    }

    @Override
    public Optional<BlogPostReadModel> findBySlug(Slug slug) {
        return blogPostRepository.findBySlug(slug.value())
                .map(this::toReadModel);
    }

    @Override
    public Optional<BlogPost> findDomainById(PostId postId) {
        return blogPostRepository.findById(postId.value())
                .map(entityMapper::toDomain);
    }

    @Override
    public Optional<BlogPost> findDomainBySlug(Slug slug) {
        return blogPostRepository.findBySlug(slug.value())
                .map(entityMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return blogPostRepository.existsBySlug(slug.value());
    }

    @Override
    public PageResponse<BlogPostListItem> findAllPublished(PageRequest pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<BlogPostEntity> page = blogPostRepository.findByStatus("PUBLISHED", pageable);

        return PageResponse.of(
                page.getContent().stream()
                        .map(this::toListItem)
                        .toList(),
                pageRequest,
                page.getTotalElements()
        );
    }

    @Override
    public PageResponse<BlogPostListItem> findByAuthorId(AuthorId authorId, PageRequest pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<BlogPostEntity> page = blogPostRepository.findByAuthorId(authorId.value(), pageable);

        return PageResponse.of(
                page.getContent().stream()
                        .map(this::toListItem)
                        .toList(),
                pageRequest,
                page.getTotalElements()
        );
    }

    @Override
    public PageResponse<BlogPostListItem> findByTagName(String tagName, PageRequest pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<BlogPostEntity> page = blogPostRepository.findByTagsName(tagName.toLowerCase(), pageable);

        return PageResponse.of(
                page.getContent().stream()
                        .map(this::toListItem)
                        .toList(),
                pageRequest,
                page.getTotalElements()
        );
    }

    @Override
    public PageResponse<BlogPostListItem> searchByTitleOrContent(String searchTerm, PageRequest pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        Page<BlogPostEntity> page = blogPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                searchTerm, searchTerm, pageable);

        return PageResponse.of(
                page.getContent().stream()
                        .map(this::toListItem)
                        .toList(),
                pageRequest,
                page.getTotalElements()
        );
    }

    @Override
    public long countPublishedPosts() {
        return blogPostRepository.countByStatus("PUBLISHED");
    }

    @Override
    public long countPostsByAuthor(AuthorId authorId) {
        return blogPostRepository.countByAuthorId(authorId.value());
    }

    private Pageable createPageable(PageRequest pageRequest) {
        Sort.Direction direction = pageRequest.sortDirection() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, pageRequest.sortBy());

        return org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                sort
        );
    }

    private BlogPostReadModel toReadModel(BlogPostEntity entity) {
        // Obtener información del autor para el read model
        String authorName = null;
        String authorEmail = null;

        Optional<AuthorEntity> authorEntity = authorRepository.findById(entity.getAuthorId());
        if (authorEntity.isPresent()) {
            authorName = authorEntity.get().getName();
            authorEmail = authorEntity.get().getEmail();
        }

        return new BlogPostReadModel(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSummary(),
                Slug.of(entity.getSlug()),
                entity.getAuthorId(),
                authorName,
                authorEmail,
                entity.getStatusAsEnum(),
                entity.getTagNames(),
                entity.getComments().stream()
                        .map(comment -> new BlogPostReadModel.CommentReadModel(
                                comment.getId(),
                                comment.getContent(),
                                comment.getAuthorName(),
                                comment.getAuthorEmail(),
                                comment.getCreatedAt()
                        ))
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPublishedAt()
        );
    }

    private BlogPostListItem toListItem(BlogPostEntity entity) {
        // Obtener nombre del autor para el list item
        String authorName = null;
        Optional<AuthorEntity> authorEntity = authorRepository.findById(entity.getAuthorId());
        if (authorEntity.isPresent()) {
            authorName = authorEntity.get().getName();
        }

        return new BlogPostListItem(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                Slug.of(entity.getSlug()),
                entity.getAuthorId(),
                authorName,
                entity.getStatusAsEnum(),
                entity.getTagNames(),
                entity.getComments() != null ? entity.getComments().size() : 0,
                entity.getCreatedAt(),
                entity.getPublishedAt()
        );
    }
}
