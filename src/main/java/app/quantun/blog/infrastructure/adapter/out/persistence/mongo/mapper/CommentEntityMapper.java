package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper;

import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CommentEntityMapper {

    @Mapping(source = "postId", target = "postId", qualifiedByName = "postIdToString")
    CommentEntity toEntity(Comment comment);

    @Mapping(source = "postId", target = "postId", qualifiedByName = "stringToPostId")
    Comment toDomain(CommentEntity entity);

    @Named("postIdToString")
    default String postIdToString(PostId postId) {
        return postId.value();
    }

    @Named("stringToPostId")
    default PostId stringToPostId(String postId) {
        return PostId.of(postId);
    }
}
