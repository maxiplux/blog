package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import app.quantun.blog.shared.valueobject.Slug;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BlogPostRepositoryAdapter implements BlogPostRepositoryPort {

    private final BlogPostMongoRepository mongoRepository;
    private final BlogPostEntityMapper mapper;

    public BlogPostRepositoryAdapter(BlogPostMongoRepository mongoRepository,
                                     BlogPostEntityMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public BlogPost save(BlogPost blogPost) {
        BlogPostEntity entity = mapper.toEntity(blogPost);
        BlogPostEntity savedEntity = mongoRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BlogPost> findById(PostId postId) {
        return mongoRepository.findById(postId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<BlogPost> findBySlug(Slug slug) {
        return mongoRepository.findBySlug(slug.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<BlogPost> findByStatus(PostStatus status) {
        return mongoRepository.findByStatus(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<BlogPost> findByAuthorId(String authorId) {
        return mongoRepository.findByAuthorId(authorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<BlogPost> findByTagName(String tagName) {
        return mongoRepository.findByTagsName(tagName)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(PostId postId) {
        mongoRepository.deleteById(postId.value());
    }

    @Override
    public boolean existsBySlug(Slug slug) {
        return mongoRepository.existsBySlug(slug.value());
    }
}
