package app.quantun.blog.content.application;

import app.quantun.blog.content.domain.model.Article;
import app.quantun.blog.content.dto.ArticleResponse;
import app.quantun.blog.content.dto.ArticleSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    // Protege el dominio de cambios en la API
    @Mapping(target = "id", source = "articleId.value")
    @Mapping(target = "title", source = "title.value")
    @Mapping(target = "slug", source = "slug.value")
    @Mapping(target = "content", source = "content.value")
    @Mapping(target = "authorId", source = "authorId.value")
    @Mapping(target = "wordCount", source = "viewCount")
    ArticleResponse toResponse(Article article);

    @Mapping(target = "id", source = "articleId.value")
    @Mapping(target = "title", source = "title.value")
    @Mapping(target = "slug", source = "slug.value")
    
    @Mapping(target = "excerpt", expression = "java(article.getContent().getValue().length() > 150 ? article.getContent().getValue().substring(0, 150) : article.getContent().getValue())")
    @Mapping(target = "authorId", source = "authorId.value")
    @Mapping(target = "wordCount", source = "viewCount")
    ArticleSummaryResponse toSummaryResponse(Article article);
}
