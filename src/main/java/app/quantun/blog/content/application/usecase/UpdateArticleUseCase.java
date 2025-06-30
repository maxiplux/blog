package app.quantun.blog.content.application;

import app.quantun.blog.content.domain.model.Article;
import app.quantun.blog.content.domain.model.ArticleId;
import app.quantun.blog.content.domain.model.AuthorId;
import app.quantun.blog.content.domain.model.Title;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import app.quantun.blog.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateArticleUseCase {

    private final ArticleRepository articleRepository;
    private final ArticleValidationService validationService;
    private final ArticleMapper articleMapper;

    @Transactional
    public ArticleResponse execute(String articleId, UpdateArticleRequest request, String requestingAuthorId) {

        // Buscar el artículo
        Article article = articleRepository.findById(ArticleId.of(articleId))
                .orElseThrow(() -> new DomainException("Article not found"));

        // Validar ownership
        validationService.validateOwnership(article, AuthorId.of(requestingAuthorId));

        // Actualizar campos si están presentes
        if (request.getTitle() != null) {
            article.updateTitle(new Title(request.getTitle()));
        }

        if (request.getContent() != null) {
            article.updateContent(new Content(request.getContent()));
        }

        if (request.getTags() != null) {
            // Limpiar tags existentes y agregar nuevos
            article.getTags().clear();
            request.getTags().forEach(article::addTag);
        }

        // Guardar
        Article savedArticle = articleRepository.save(article);

        return articleMapper.toResponse(savedArticle);
    }
}
