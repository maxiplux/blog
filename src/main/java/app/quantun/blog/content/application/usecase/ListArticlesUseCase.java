package app.quantun.blog.content.application;

import app.quantun.blog.content.domain.model.ArticleStatus;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListArticlesUseCase {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Transactional(readOnly = true)
    public List<ArticleSummaryResponse> executePublished(int page, int size) {
        return articleRepository.findPublishedArticles(page, size)
                .stream()
                .map(articleMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArticleSummaryResponse> executeByTag(String tag) {
        return articleRepository.findByTag(tag)
                .stream()
                .filter(article -> article.getStatus() == ArticleStatus.PUBLISHED)
                .map(articleMapper::toSummaryResponse)
                .toList();
    }
}
