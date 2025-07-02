package app.quantun.blog.infrastructure.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    // Configuración específica para el dominio si es necesaria
    // Los servicios de aplicación se registran automáticamente con @Service
    // Los adapters se registran automáticamente con @Component
    // Los ports se resuelven automáticamente por tipo durante la inyección
}
