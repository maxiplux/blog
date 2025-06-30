package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper;

import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.TagEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagEntityMapper {

    @Mapping(target = "id", ignore = true)
    TagEntity toEntity(Tag tag);


    Tag toDomain(TagEntity entity);
}
