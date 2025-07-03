package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;

import app.quantun.blog.application.command.port.out.TagCommandRepositoryPort;
import app.quantun.blog.application.port.out.TagRepositoryPort;
import app.quantun.blog.application.query.port.out.TagQueryRepositoryPort;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.TagEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.TagEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.TagMongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TagRepositoryAdapter implements TagRepositoryPort, TagCommandRepositoryPort, TagQueryRepositoryPort {

    private final TagMongoRepository mongoRepository;
    private final TagEntityMapper mapper;

    public TagRepositoryAdapter(TagMongoRepository mongoRepository,
                                TagEntityMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Tag save(Tag tag) {
        TagEntity entity = mapper.toEntity(tag);
        TagEntity savedEntity = mongoRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Tag> findBySlug(String slug) {
        return mongoRepository.findBySlug(slug)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return mongoRepository.existsBySlug(slug);
    }
}