package app.quantun.blog.infrastructure.config;


import app.quantun.blog.domain.port.out.AuthorRepositoryPort;
import app.quantun.blog.domain.port.out.BlogPostRepositoryPort;
import app.quantun.blog.domain.port.out.CommentRepositoryPort;
import app.quantun.blog.domain.service.AuthorService;
import app.quantun.blog.domain.service.BlogPostService;
import app.quantun.blog.domain.service.CommentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public BlogPostService blogPostService(BlogPostRepositoryPort blogPostRepositoryPort,
                                           AuthorRepositoryPort authorRepositoryPort) {
        return new BlogPostService(blogPostRepositoryPort, authorRepositoryPort);
    }

    @Bean
    public CommentService commentService(CommentRepositoryPort commentRepositoryPort,
                                         BlogPostRepositoryPort blogPostRepositoryPort) {
        return new CommentService(commentRepositoryPort, blogPostRepositoryPort);
    }

    @Bean
    public AuthorService authorService(AuthorRepositoryPort authorRepositoryPort) {
        return new AuthorService(authorRepositoryPort);
    }
}
