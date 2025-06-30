package app.quantun.blog.content.application.usecase;

import app.quantun.blog.content.domain.model.*;
import app.quantun.blog.content.domain.service.ArticleSlugService;
import app.quantun.blog.content.application.ArticleMapper;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import app.quantun.blog.content.dto.ArticleResponse;
import app.quantun.blog.content.dto.CreateArticleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateArticleUseCase {

    private final ArticleRepository articleRepository;
    private final ArticleSlugService slugService;
    private final ArticleMapper articleMapper;

    @Transactional
    public ArticleResponse execute(CreateArticleRequest request) {

        // Crear Value Objects
        ArticleId articleId = ArticleId.generate();
        Title title = new Title(request.getTitle());
        Content content = new Content(request.getContent());
        AuthorId authorId = AuthorId.of(request.getAuthorId());

        // Crear el artículo
        Article article = Article.create(articleId, title, content, authorId);

        // Agregar tags si existen
        if (request.getTags() != null) {
            request.getTags().forEach(article::addTag);
        }

        // Guardar
        Article savedArticle = articleRepository.save(article);

        return articleMapper.toResponse(savedArticle);
    }
}
