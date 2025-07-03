package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.application.command.port.out.BlogPostCommandRepositoryPort;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.BlogPostEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.BlogPostMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter para operaciones de comando (escritura) de blog posts
 * Implementa patrones de escritura optimizados para CQRS
 */
@Component
public class BlogPostCommandRepositoryAdapter implements BlogPostCommandRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(BlogPostCommandRepositoryAdapter.class);

    private final BlogPostMongoRepository mongoRepository;
    private final BlogPostEntityMapper mapper;

    public BlogPostCommandRepositoryAdapter(BlogPostMongoRepository mongoRepository,
                                            BlogPostEntityMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public BlogPost save(BlogPost blogPost) {
        logger.debug("Saving blog post with ID: {}", blogPost.getId().value());

        BlogPostEntity entity = mapper.toEntity(blogPost);
        BlogPostEntity savedEntity = mongoRepository.save(entity);

        logger.debug("Blog post saved successfully with ID: {}", savedEntity.getId());

        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(PostId postId) {
        logger.info("Deleting blog post with ID: {}", postId.value());

        mongoRepository.deleteById(postId.value());

        logger.info("Blog post deleted successfully with ID: {}", postId.value());
    }
}
