package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository;

import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.AuthorEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorMongoRepository extends MongoRepository<AuthorEntity, String> {
    Optional<AuthorEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}