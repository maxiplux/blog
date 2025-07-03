package app.quantun.blog.application.command.service;

import app.quantun.blog.application.command.port.in.CreateBlogPostCommand;
import app.quantun.blog.application.command.port.in.PublishBlogPostCommand;
import app.quantun.blog.application.command.port.in.UpdateBlogPostCommand;
import app.quantun.blog.application.command.port.out.BlogPostCommandRepositoryPort;
import app.quantun.blog.application.command.port.out.TagCommandRepositoryPort;
import app.quantun.blog.application.query.port.out.AuthorQueryRepositoryPort;
import app.quantun.blog.application.query.port.out.BlogPostQueryRepositoryPort;
import app.quantun.blog.application.query.port.out.TagQueryRepositoryPort;
import app.quantun.blog.domain.model.BlogPost;
import app.quantun.blog.domain.model.PostId;
import app.quantun.blog.domain.model.Tag;
import app.quantun.blog.shared.exception.AuthorNotFoundException;
import app.quantun.blog.shared.exception.BlogPostNotFoundException;
import app.quantun.blog.shared.valueobject.Slug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para comandos de blog posts
 * Implementa todos los casos de uso de escritura con lógica de negocio completa
 * Incluye invalidación de cache para mantener consistencia con queries
 */
@Service
@Transactional
public class BlogPostCommandService implements CreateBlogPostCommand, UpdateBlogPostCommand, PublishBlogPostCommand {

    private static final Logger logger = LoggerFactory.getLogger(BlogPostCommandService.class);

    private final BlogPostCommandRepositoryPort commandRepository;
    private final BlogPostQueryRepositoryPort queryRepository;
    private final AuthorQueryRepositoryPort authorQueryRepository;
    private final TagCommandRepositoryPort tagCommandRepository;
    private final TagQueryRepositoryPort tagQueryRepository;


    public BlogPostCommandService(BlogPostCommandRepositoryPort commandRepository,
                                  BlogPostQueryRepositoryPort queryRepository,
                                  AuthorQueryRepositoryPort authorQueryRepository,
                                  TagCommandRepositoryPort tagCommandRepository,
                                  TagQueryRepositoryPort tagQueryRepository
    ) {
        this.commandRepository = commandRepository;
        this.queryRepository = queryRepository;
        this.authorQueryRepository = authorQueryRepository;
        this.tagCommandRepository = tagCommandRepository;
        this.tagQueryRepository = tagQueryRepository;

    }

    @Override
    public BlogPost createBlogPost(CreateBlogPostCommandData command) {
        logger.info("Creating blog post with title: '{}'", command.title());

        try {
            // Verificar que el autor existe
            authorQueryRepository.findById(command.authorId())
                    .orElseThrow(() -> new AuthorNotFoundException(
                            "Author not found with ID: " + command.authorId().value()
                    ));

            // Verificar slug único
            Slug proposedSlug = Slug.fromTitle(command.title());
            if (queryRepository.existsBySlug(proposedSlug)) {
                proposedSlug = generateUniqueSlug(proposedSlug);
                logger.debug("Generated unique slug: {}", proposedSlug.value());
            }

            // Crear el blog post
            BlogPost blogPost = BlogPost.createDraft(
                    command.title(),
                    command.content(),
                    command.summary(),
                    command.authorId()
            );

            // Procesar tags si existen
            if (command.tagNames() != null && !command.tagNames().isEmpty()) {
                Set<Tag> tags = processTagNames(command.tagNames());
                blogPost = addTagsToBlogPost(blogPost, tags);
                logger.debug("Added {} tags to blog post", tags.size());
            }

            // Persistir
            BlogPost savedPost = commandRepository.save(blogPost);

            // Invalidar cache después de crear


            logger.info("Blog post created successfully with ID: {}", savedPost.getId().value());
            return savedPost;

        } catch (Exception e) {
            logger.error("Failed to create blog post with title: '{}' - Error: {}",
                    command.title(), e.getMessage());
            throw e;
        }
    }

    @Override
    public BlogPost updateBlogPost(UpdateBlogPostCommandData command) {
        logger.info("Updating blog post with ID: {}", command.postId().value());

        try {
            BlogPost existingPost = queryRepository.findDomainById(command.postId())
                    .orElseThrow(() -> new BlogPostNotFoundException(
                            "Blog post not found with ID: " + command.postId().value()
                    ));

            // Actualizar contenido
            BlogPost updatedPost = existingPost.updateContent(
                    command.title(),
                    command.content(),
                    command.summary()
            );

            // Procesar tags si se proporcionaron
            if (command.tagNames() != null) {
                // Remover tags existentes
                for (Tag existingTag : existingPost.getTags()) {
                    updatedPost = updatedPost.removeTag(existingTag);
                }

                // Agregar nuevos tags
                if (!command.tagNames().isEmpty()) {
                    Set<Tag> newTags = processTagNames(command.tagNames());
                    updatedPost = addTagsToBlogPost(updatedPost, newTags);
                    logger.debug("Updated tags for blog post, new count: {}", newTags.size());
                }
            }

            // Persistir
            BlogPost savedPost = commandRepository.save(updatedPost);


            logger.info("Blog post updated successfully with ID: {}", savedPost.getId().value());
            return savedPost;

        } catch (Exception e) {
            logger.error("Failed to update blog post with ID: {} - Error: {}",
                    command.postId().value(), e.getMessage());
            throw e;
        }
    }

    @Override
    public BlogPost publishPost(PostId postId) {
        logger.info("Publishing blog post with ID: {}", postId.value());

        try {
            BlogPost blogPost = queryRepository.findDomainById(postId)
                    .orElseThrow(() -> new BlogPostNotFoundException(
                            "Blog post not found with ID: " + postId.value()
                    ));

            // Validaciones de negocio antes de publicar
            validateForPublication(blogPost);

            BlogPost publishedPost = blogPost.publish();
            BlogPost savedPost = commandRepository.save(publishedPost);


            logger.info("Blog post published successfully with ID: {}", postId.value());
            return savedPost;

        } catch (Exception e) {
            logger.error("Failed to publish blog post with ID: {} - Error: {}",
                    postId.value(), e.getMessage());
            throw e;
        }
    }

    @Override
    public BlogPost archivePost(PostId postId) {
        logger.info("Archiving blog post with ID: {}", postId.value());

        try {
            BlogPost blogPost = queryRepository.findDomainById(postId)
                    .orElseThrow(() -> new BlogPostNotFoundException(
                            "Blog post not found with ID: " + postId.value()
                    ));

            BlogPost archivedPost = blogPost.archive();
            BlogPost savedPost = commandRepository.save(archivedPost);

            // Invalidar cache después de archivar


            logger.info("Blog post archived successfully with ID: {}", postId.value());
            return savedPost;

        } catch (Exception e) {
            logger.error("Failed to archive blog post with ID: {} - Error: {}",
                    postId.value(), e.getMessage());
            throw e;
        }
    }

    /**
     * Procesa los nombres de tags, creando los que no existen
     */
    private Set<Tag> processTagNames(Set<String> tagNames) {
        return tagNames.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> {
                    String slug = name.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
                    return tagQueryRepository.findBySlug(slug)
                            .orElseGet(() -> {
                                Tag newTag = Tag.create(name);
                                logger.debug("Creating new tag: {}", name);
                                return tagCommandRepository.save(newTag);
                            });
                })
                .collect(Collectors.toSet());
    }

    /**
     * Agrega tags a un blog post
     */
    private BlogPost addTagsToBlogPost(BlogPost blogPost, Set<Tag> tags) {
        BlogPost result = blogPost;
        for (Tag tag : tags) {
            result = result.addTag(tag);
        }
        return result;
    }

    /**
     * Genera un slug único cuando ya existe uno igual
     */
    private Slug generateUniqueSlug(Slug baseSlug) {
        String baseValue = baseSlug.value();
        int counter = 1;
        Slug uniqueSlug;

        do {
            uniqueSlug = Slug.of(baseValue + "-" + counter);
            counter++;
        } while (queryRepository.existsBySlug(uniqueSlug));

        logger.debug("Generated unique slug: {} (attempt {})", uniqueSlug.value(), counter - 1);
        return uniqueSlug;
    }

    /**
     * Validaciones de negocio antes de publicar
     */
    private void validateForPublication(BlogPost blogPost) {
        if (blogPost.getContent().length() < 100) {
            throw new IllegalStateException(
                    "Blog post content must be at least 100 characters long to publish");
        }

        if (blogPost.getTitle().length() < 5) {
            throw new IllegalStateException(
                    "Blog post title must be at least 5 characters long to publish");
        }

        // Verificar que el autor aún existe
        authorQueryRepository.findById(blogPost.getAuthorId())
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot publish post: author no longer exists"));
    }
}
