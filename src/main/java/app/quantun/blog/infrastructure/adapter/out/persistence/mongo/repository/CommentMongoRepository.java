package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository;

import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.CommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMongoRepository extends MongoRepository<CommentEntity, String> {
    List<CommentEntity> findByPostId(String postId);
}