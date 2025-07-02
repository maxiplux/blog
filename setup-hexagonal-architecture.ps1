# PowerShell Script - setup-hexagonal-architecture.ps1
# Creates the complete folder structure for Hexagonal Architecture project

Write-Host "🏗️  Setting up Hexagonal Architecture folder structure..." -ForegroundColor Green

# Main source directories
$folders = @(
    # Main Java source structure
    "src/main/java/app/quantun/blog",
    
    # Shared layer
    "src/main/java/app/quantun/blog/shared/valueobject",
    "src/main/java/app/quantun/blog/shared/exception",
    
    # Domain layer
    "src/main/java/app/quantun/blog/domain/model",
    
    # Application layer
    "src/main/java/app/quantun/blog/application/port/in",
    "src/main/java/app/quantun/blog/application/port/out",
    "src/main/java/app/quantun/blog/application/service",
    
    # Infrastructure layer - Input adapters
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web",
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web/contract/request",
    "src/main/java/app/quantun/blog/infrastructure/adapter/in/web/contract/response",
    
    # Infrastructure layer - Output adapters
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/adapter",
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/entity",
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/mapper",
    "src/main/java/app/quantun/blog/infrastructure/adapter/out/persistence/mongo/repository",
    
    # Infrastructure layer - Configuration
    "src/main/java/app/quantun/blog/infrastructure/config",
    
    # Resources
    "src/main/resources",
    "src/main/resources/static",
    "src/main/resources/templates",
    
    # Test structure
    "src/test/java/app/quantun/blog",
    "src/test/java/app/quantun/blog/domain",
    "src/test/java/app/quantun/blog/application",
    "src/test/java/app/quantun/blog/infrastructure",
    "src/test/java/app/quantun/blog/infrastructure/adapter/in/web",
    "src/test/java/app/quantun/blog/infrastructure/adapter/out/persistence",
    "src/test/resources",
    
    # Build directories
    "gradle/wrapper",
    
    # Documentation
    "docs",
    "docs/architecture",
    "docs/api",
    
    # Configuration
    ".github/workflows"
)

foreach ($folder in $folders) {
    if (!(Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder -Force | Out-Null
        Write-Host "✅ Created: $folder" -ForegroundColor Cyan
    } else {
        Write-Host "📁 Exists: $folder" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎉 Hexagonal Architecture folder structure created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Structure Summary:" -ForegroundColor White
Write-Host "   🎯 Domain Layer: Business logic and entities" -ForegroundColor Magenta
Write-Host "   🔄 Application Layer: Use cases and orchestration" -ForegroundColor Blue
Write-Host "   🔌 Infrastructure Layer: External adapters" -ForegroundColor Yellow
Write-Host "   🧪 Test Structure: Complete testing hierarchy" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "1. Add your domain entities in: src/main/java/app/quantun/blog/domain/model/" -ForegroundColor Gray
Write-Host "2. Define use cases in: src/main/java/app/quantun/blog/application/port/in/" -ForegroundColor Gray
Write-Host "3. Implement controllers in: src/main/java/app/quantun/blog/infrastructure/adapter/in/web/" -ForegroundColor Gray
