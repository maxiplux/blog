package app.quantun.blog.content.domain.service;

import app.quantun.blog.content.domain.model.Slug;
import app.quantun.blog.content.domain.model.Title;
import app.quantun.blog.content.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleSlugService {

    private final ArticleRepository articleRepository;

    // Lógica compleja que no pertenece a Article
    public Slug generateUniqueSlug(Title title) {
        Slug baseSlug = Slug.fromTitle(title);

        if (!articleRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        // Generar slug único agregando número
        int counter = 1;
        Slug uniqueSlug;
        do {
            uniqueSlug = new Slug(baseSlug.getValue() + "-" + counter);
            counter++;
        } while (articleRepository.existsBySlug(uniqueSlug));

        return uniqueSlug;
    }
}
