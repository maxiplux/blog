# PowerShell Script - add-gitkeep.ps1
# Adds .gitkeep files to empty directories so they can be tracked in Git

Write-Host "📝 Adding .gitkeep files to empty directories..." -ForegroundColor Green

# Counter for tracking
$count = 0

# Find all directories and add .gitkeep to empty ones
$directories = Get-ChildItem -Path "src" -Recurse -Directory -ErrorAction SilentlyContinue

foreach ($dir in $directories) {
    $items = Get-ChildItem -Path $dir.FullName -Force -ErrorAction SilentlyContinue
    if ($items.Count -eq 0) {
        $gitkeepPath = Join-Path $dir.FullName ".gitkeep"
        if (!(Test-Path $gitkeepPath)) {
            New-Item -ItemType File -Path $gitkeepPath -Force | Out-Null
            Write-Host "✅ Added .gitkeep to: $($dir.FullName)" -ForegroundColor Cyan
            $count++
        }
    }
}

# Also check docs directories if they exist
if (Test-Path "docs") {
    $docDirectories = Get-ChildItem -Path "docs" -Recurse -Directory -ErrorAction SilentlyContinue
    foreach ($dir in $docDirectories) {
        $items = Get-ChildItem -Path $dir.FullName -Force -ErrorAction SilentlyContinue
        if ($items.Count -eq 0) {
            $gitkeepPath = Join-Path $dir.FullName ".gitkeep"
            if (!(Test-Path $gitkeepPath)) {
                New-Item -ItemType File -Path $gitkeepPath -Force | Out-Null
                Write-Host "✅ Added .gitkeep to: $($dir.FullName)" -ForegroundColor Cyan
                $count++
            }
        }
    }
}

Write-Host ""
if ($count -eq 0) {
    Write-Host "ℹ️  No empty directories found or all already have .gitkeep files" -ForegroundColor Yellow
} else {
    Write-Host "🎉 Added .gitkeep files to $count empty directories!" -ForegroundColor Green
}

Write-Host ""
Write-Host "💡 Tip: These .gitkeep files ensure Git tracks your folder structure even when directories are empty" -ForegroundColor Blue
