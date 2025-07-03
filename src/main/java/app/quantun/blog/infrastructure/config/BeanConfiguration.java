package app.quantun.blog.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuración de beans para CQRS con AOP habilitado
 * <p>
 * Los servicios se registran automáticamente:
 * - Command Services: BlogPostCommandService (@Service)
 * - Query Services: BlogPostQueryService (@Service)
 * - Repository Adapters: BlogPostCommandRepositoryAdapter, BlogPostQueryRepositoryAdapter (@Component)
 * <p>
 * Los puertos se resuelven automáticamente por tipo durante la inyección:
 * - Input Ports: Implementados por los servicios de aplicación
 * - Output Ports: Implementados por los adapters de repositorio
 * <p>
 * AOP habilitado para:
 * - Métricas automáticas de CQRS
 * - Logging de performance
 * - Manejo de errores
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class BeanConfiguration {

    // Con CQRS + AOP, la configuración se simplifica aún más
    // Spring Boot auto-configuración maneja la inyección de dependencias
    // basada en los tipos de los puertos (interfaces)

    // Los Controllers inyectan:
    // - CreateBlogPostCommand -> BlogPostCommandService
    // - GetBlogPostQuery -> BlogPostQueryService
    // - SearchBlogPostQuery -> BlogPostQueryService
    // - PublishBlogPostCommand -> BlogPostCommandService

    // Los Services inyectan:
    // - BlogPostCommandRepositoryPort -> BlogPostCommandRepositoryAdapter  
    // - BlogPostQueryRepositoryPort -> BlogPostQueryRepositoryAdapter

    // Los Aspects interceptan automáticamente para:
    // - Métricas de performance (CQRSMetricsAspect)
    // - Logging de operaciones
    // - Conteo de errores por tipo

    // La configuración de índices MongoDB se maneja en MongoIndexConfiguration
    // La configuración de métricas se maneja en CQRSMetricsConfiguration
    // La migración de datos se activa con: blog.migration.cqrs.enabled=true
}
