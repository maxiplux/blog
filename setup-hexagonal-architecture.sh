#!/bin/bash
# Bash Script - setup-hexagonal-architecture.sh
# Creates the complete folder structure for Hexagonal Architecture project

echo "🏗️  Setting up Hexagonal Architecture folder structure..."

# Define all folders for Hexagonal Architecture
folders=(
    # Main Java source structure
    "src/main/java/app/quantun/blog"
    
    # Shared layer
    "src/main/java/app/quantun/blog/shared/valueobject"
    "src/main/java/app/quantun/blog/shared/exception"
    
    # Domain layer
    "src/main/java/app/quantun/blog/domain/model"
    
    # Application layer
    "src/main/java/app/quantun/blog/application/port/in"
    "src/main/java/app/quantun/blog/application/port/out"
    "src/main/java/app/quantun/blog/application/service"
    
    # Infrastructure layer - Input adapters
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web"
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web/contract/request"
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web/contract/response"
    
    # Infrastructure layer - Output adapters
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/adapter"
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/entity"
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/mapper"
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/repository"
    
    # Infrastructure layer - Configuration
    "src/main/java/app/quantun/blog/infrastructure/config"
    
    # Resources
    "src/main/resources"
    "src/main/resources/static"
    "src/main/resources/templates"
    
    # Test structure
    "src/test/java/app/quantun/blog"
    "src/test/java/app/quantun/blog/domain"
    "src/test/java/app/quantun/blog/application"
    "src/test/java/app/quantun/blog/infrastructure"
    "src/test/java/app/quantun/blog/infrastructure/adapter/in/web"
    "src/test/java/app/quantun/blog/infrastructure/adapter/out/persistence"
    "src/test/resources"
    
    # Build directories
    "gradle/wrapper"
    
    # Documentation
    "docs"
    "docs/architecture"
    "docs/api"
    
    # Configuration
    ".github/workflows"
)

# Create folders
for folder in "${folders[@]}"; do
    if [ ! -d "$folder" ]; then
        mkdir -p "$folder"
        echo "✅ Created: $folder"
    else
        echo "📁 Exists: $folder"
    fi
done

echo ""
echo "🎉 Hexagonal Architecture folder structure created successfully!"
echo ""
echo "📋 Structure Summary:"
echo "   🎯 Domain Layer: Business logic and entities"
echo "   🔄 Application Layer: Use cases and orchestration"
echo "   🔌 Infrastructure Layer: External adapters"
echo "   🧪 Test Structure: Complete testing hierarchy"
echo ""
echo "Next steps:"
echo "1. Add your domain entities in: src/main/java/app/quantun/blog/domain/model/"
echo "2. Define use cases in: src/main/java/app/quantun/blog/application/port/in/"
echo "3. Implement controllers in: src/main/java/app/quantun/blog/infrastructure/adapter/in/web/"
echo ""
echo "To make this script executable, run: chmod +x setup-hexagonal-architecture.sh"
