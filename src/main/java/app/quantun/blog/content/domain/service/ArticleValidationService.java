package app.quantun.blog.content.domain.service;

import app.quantun.blog.content.domain.model.Article;
import app.quantun.blog.content.domain.model.AuthorId;
import app.quantun.blog.shared.domain.DomainException;
import org.springframework.stereotype.Service;

@Service
public class ArticleValidationService {

    public void validateOwnership(Article article, AuthorId requestingAuthorId) {
        if (!article.isOwnedBy(requestingAuthorId)) {
            throw new DomainException("Author does not have permission to modify this article");
        }
    }

    public void validatePublishability(Article article) {
        if (article.getContent().wordCount() < 50) {
            throw new DomainException("Article must have at least 50 words to be published");
        }

        if (article.getTags().isEmpty()) {
            throw new DomainException("Article must have at least one tag to be published");
        }
    }
}
