package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper;


import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.PostStatus;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.domain.model.Comment;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.BlogPostEntity;
import app.quantun.blog.shared.valueobject.Slug;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BlogPostEntityMapper {

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "slug.value", target = "slug")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "tagsToEntities")
    @Mapping(source = "comments", target = "comments", qualifiedByName = "commentsToEntities")
    BlogPostEntity toEntity(BlogPost blogPost);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToPostId")
    @Mapping(source = "slug", target = "slug", qualifiedByName = "stringToSlug")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "entitiesToTags")
    @Mapping(source = "comments", target = "comments", qualifiedByName = "entitiesToComments")
    BlogPost toDomain(BlogPostEntity entity);

    @Named("stringToPostId")
    default PostId stringToPostId(String id) {
        return PostId.of(id);
    }

    @Named("stringToSlug")
    default Slug stringToSlug(String slug) {
        return Slug.of(slug);
    }

    @Named("statusToString")
    default String statusToString(PostStatus status) {
        return status.name();
    }

    @Named("stringToStatus")
    default PostStatus stringToStatus(String status) {
        return PostStatus.valueOf(status);
    }

    @Named("tagsToEntities")
    default Set<BlogPostEntity.TagEntity> tagsToEntities(Set<Tag> tags) {
        return tags.stream()
                .map(tag -> new BlogPostEntity.TagEntity(tag.getName(), tag.getSlug()))
                .collect(Collectors.toSet());
    }

    @Named("entitiesToTags")
    default Set<Tag> entitiesToTags(Set<BlogPostEntity.TagEntity> entities) {
        return entities.stream()
                .map(entity -> new Tag(entity.getName(), entity.getSlug()))
                .collect(Collectors.toSet());
    }

    @Named("commentsToEntities")
    default List<BlogPostEntity.CommentEntity> commentsToEntities(List<Comment> comments) {
        return comments.stream()
                .map(comment -> new BlogPostEntity.CommentEntity(
                        comment.getId(),
                        comment.getContent(),
                        comment.getAuthorName(),
                        comment.getAuthorEmail(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Named("entitiesToComments")
    default List<Comment> entitiesToComments(List<BlogPostEntity.CommentEntity> entities) {
        return entities.stream()
                .map(entity -> new Comment(
                        entity.getId(),
                        PostId.of(""), // Will be set by parent post
                        entity.getContent(),
                        entity.getAuthorName(),
                        entity.getAuthorEmail(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}