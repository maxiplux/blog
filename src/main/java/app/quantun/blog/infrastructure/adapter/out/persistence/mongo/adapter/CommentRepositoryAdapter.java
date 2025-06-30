package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.adapter;


import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.port.out.CommentRepositoryPort;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.CommentEntity;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper.CommentEntityMapper;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.repository.CommentMongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentRepositoryAdapter implements CommentRepositoryPort {

    private final CommentMongoRepository mongoRepository;
    private final CommentEntityMapper mapper;

    public CommentRepositoryAdapter(CommentMongoRepository mongoRepository,
                                    CommentEntityMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = mapper.toEntity(comment);
        CommentEntity savedEntity = mongoRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Comment> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Comment> findByPostId(PostId postId) {
        return mongoRepository.findByPostId(postId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}