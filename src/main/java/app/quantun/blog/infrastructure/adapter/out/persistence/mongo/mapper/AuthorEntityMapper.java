package app.quantun.blog.infrastructure.adapter.out.persistence.mongo.mapper;

import app.quantun.blog.domain.model.Author;
import app.quantun.blog.domain.model.AuthorId;
import app.quantun.blog.infrastructure.adapter.out.persistence.mongo.entity.AuthorEntity;
import app.quantun.blog.shared.valueobject.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuthorEntityMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "authorIdToString")
    @Mapping(source = "email", target = "email", qualifiedByName = "emailToString")
    AuthorEntity toEntity(Author author);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToAuthorId")
    @Mapping(source = "email", target = "email", qualifiedByName = "stringToEmail")
    Author toDomain(AuthorEntity entity);

    @Named("authorIdToString")
    default String authorIdToString(AuthorId id) {
        return id.value();
    }

    @Named("stringToAuthorId")
    default AuthorId stringToAuthorId(String id) {
        return AuthorId.of(id);
    }

    @Named("emailToString")
    default String emailToString(Email email) {
        return email.value();
    }

    @Named("stringToEmail")
    default Email stringToEmail(String email) {
        return Email.of(email);
    }
}
