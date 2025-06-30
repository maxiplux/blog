package app.quantun.blog.content.application.usecase;

import app.quantun.blog.content.application.ArticleMapper;
import app.quantun.blog.content.domain.model.Article;
import app.quantun.blog.content.domain.model.ArticleId;
import app.quantun.blog.content.domain.model.AuthorId;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import app.quantun.blog.content.domain.service.ArticleValidationService;
import app.quantun.blog.content.dto.ArticleResponse;
import app.quantun.blog.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishArticleUseCase {

    private final ArticleRepository articleRepository;
    private final ArticleValidationService validationService;
    private final ArticleMapper articleMapper;

    @Transactional
    public ArticleResponse execute(String articleId, String requestingAuthorId) {

        // Buscar el artículo
        Article article = articleRepository.findById(ArticleId.of(articleId))
                .orElseThrow(() -> new DomainException("Article not found"));

        // Validar ownership
        validationService.validateOwnership(article, AuthorId.of(requestingAuthorId));

        // Validar que se puede publicar
        validationService.validatePublishability(article);

        // Publicar
        article.publish();

        // Guardar
        Article savedArticle = articleRepository.save(article);

        return articleMapper.toResponse(savedArticle);
    }
}
