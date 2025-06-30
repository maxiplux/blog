package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository;

import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.TagEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagMongoRepository extends MongoRepository<TagEntity, String> {
    Optional<TagEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
