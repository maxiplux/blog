package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;


import app.quantun.blog.application.command.port.out.AuthorCommandRepositoryPort;
import app.quantun.blog.application.port.out.AuthorRepositoryPort;
import app.quantun.blog.application.query.port.out.AuthorQueryRepositoryPort;
import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.AuthorEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.AuthorEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.AuthorMongoRepository;
import app.quantun.blog.shared.valueobject.Email;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorRepositoryAdapter implements AuthorRepositoryPort, AuthorCommandRepositoryPort, AuthorQueryRepositoryPort {

    private final AuthorMongoRepository mongoRepository;
    private final AuthorEntityMapper mapper;

    public AuthorRepositoryAdapter(AuthorMongoRepository mongoRepository,
                                   AuthorEntityMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Author save(Author author) {
        AuthorEntity entity = mapper.toEntity(author);
        AuthorEntity savedEntity = mongoRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Author> findById(AuthorId id) {
        return mongoRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Author> findByEmail(Email email) {
        return mongoRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return mongoRepository.existsByEmail(email.value());
    }

    @Override
    public void deleteById(AuthorId id) {
        mongoRepository.deleteById(id.value());
    }
}
