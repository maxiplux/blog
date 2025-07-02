#!/bin/bash
# Bash Script - add-gitkeep.sh
# Adds .gitkeep files to empty directories so they can be tracked in Git

echo "📝 Adding .gitkeep files to empty directories..."

# Counter for tracking
count=0

# Find all directories under src and add .gitkeep to empty ones
if [ -d "src" ]; then
    while IFS= read -r -d '' dir; do
        if [ -z "$(ls -A "$dir" 2>/dev/null)" ]; then
            touch "$dir/.gitkeep"
            echo "✅ Added .gitkeep to: $dir"
            ((count++))
        fi
    done < <(find src -type d -print0 2>/dev/null)
fi

# Also check docs directories if they exist
if [ -d "docs" ]; then
    while IFS= read -r -d '' dir; do
        if [ -z "$(ls -A "$dir" 2>/dev/null)" ]; then
            touch "$dir/.gitkeep"
            echo "✅ Added .gitkeep to: $dir"
            ((count++))
        fi
    done < <(find docs -type d -print0 2>/dev/null)
fi

# Check .github directory if it exists
if [ -d ".github" ]; then
    while IFS= read -r -d '' dir; do
        if [ -z "$(ls -A "$dir" 2>/dev/null)" ]; then
            touch "$dir/.gitkeep"
            echo "✅ Added .gitkeep to: $dir"
            ((count++))
        fi
    done < <(find .github -type d -print0 2>/dev/null)
fi

echo ""
if [ $count -eq 0 ]; then
    echo "ℹ️  No empty directories found or all already have .gitkeep files"
else
    echo "🎉 Added .gitkeep files to $count empty directories!"
fi

echo ""
echo "💡 Tip: These .gitkeep files ensure Git tracks your folder structure even when directories are empty"
echo ""
echo "To make this script executable, run: chmod +x add-gitkeep.sh"
