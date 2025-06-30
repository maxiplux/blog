package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository;


import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostMongoRepository extends MongoRepository<BlogPostEntity, String> {

    Optional<BlogPostEntity> findBySlug(String slug);

    List<BlogPostEntity> findByStatus(String status);

    List<BlogPostEntity> findByAuthorId(String authorId);

    @Query("{ 'tags.name': ?0 }")
    List<BlogPostEntity> findByTagsName(String tagName);

    boolean existsBySlug(String slug);
}
