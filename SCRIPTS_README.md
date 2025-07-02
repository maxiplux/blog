# 🏗️ Hexagonal Architecture Setup Scripts

These scripts create the complete folder structure for a Hexagonal Architecture project following best practices and clean architecture principles.

## 📦 Available Scripts

| Script | Platform | Purpose |
|--------|----------|---------|
| `setup-hexagonal-architecture.ps1` | Windows (PowerShell) | Creates folder structure |
| `setup-hexagonal-architecture.sh` | Linux/macOS/WSL (Bash) | Creates folder structure |
| `add-gitkeep.ps1` | Windows (PowerShell) | Adds .gitkeep files |
| `add-gitkeep.sh` | Linux/macOS/WSL (Bash) | Adds .gitkeep files |

## 🚀 Usage Instructions

### For Windows (PowerShell)

1. **Run the setup script:**
   ```powershell
   .\setup-hexagonal-architecture.ps1
   ```

2. **Add .gitkeep files for Git tracking:**
   ```powershell
   .\add-gitkeep.ps1
   ```

### For Linux/macOS/WSL (Bash)

1. **Make scripts executable:**
   ```bash
   chmod +x setup-hexagonal-architecture.sh
   chmod +x add-gitkeep.sh
   ```

2. **Run the setup script:**
   ```bash
   ./setup-hexagonal-architecture.sh
   ```

3. **Add .gitkeep files for Git tracking:**
   ```bash
   ./add-gitkeep.sh
   ```

## 📁 Created Structure

The scripts will create this complete Hexagonal Architecture structure:

```
project-root/
├── src/
│   ├── main/
│   │   ├── java/app/quantun/blog/
│   │   │   ├── shared/
│   │   │   │   ├── valueobject/     # Email, Slug, etc.
│   │   │   │   └── exception/       # Domain exceptions
│   │   │   ├── domain/
│   │   │   │   └── model/           # BlogPost, Author, etc.
│   │   │   ├── application/
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/          # Use cases
│   │   │   │   │   └── out/         # Repository ports
│   │   │   │   └── service/         # Application services
│   │   │   └── infrastructure/
│   │   │       ├── adapter/
│   │   │       │   ├── in/web/
│   │   │       │   │   └── contract/
│   │   │       │   │       ├── request/
│   │   │       │   │       └── response/
│   │   │       │   └── out/persistence/mongo/
│   │   │       │       ├── adapter/
│   │   │       │       ├── entity/
│   │   │       │       ├── mapper/
│   │   │       │       └── repository/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       ├── java/app/quantun/blog/
│       │   ├── domain/
│       │   ├── application/
│       │   └── infrastructure/
│       └── resources/
├── docs/
│   ├── architecture/
│   └── api/
├── gradle/wrapper/
└── .github/workflows/
```

## 🎯 Architecture Layers

| Layer | Purpose | Location |
|-------|---------|----------|
| **🎯 Domain** | Business logic, entities, value objects | `domain/model/` |
| **🔄 Application** | Use cases, orchestration | `application/port/`, `application/service/` |
| **🔌 Infrastructure** | External adapters, persistence, web | `infrastructure/adapter/` |
| **🔄 Shared** | Cross-cutting concerns | `shared/` |

## ✨ Script Features

### 🛠️ Setup Scripts
- ✅ **Complete folder structure** for hexagonal architecture
- ✅ **Cross-platform compatibility** (Windows, Linux, macOS)
- ✅ **Idempotent execution** (safe to run multiple times)
- ✅ **Colored output** for better user experience
- ✅ **Progress feedback** during folder creation
- ✅ **Handles existing folders** gracefully

### 📝 GitKeep Scripts
- ✅ **Automatic .gitkeep placement** in empty directories
- ✅ **Git-friendly structure** ensuring all folders are tracked
- ✅ **Smart detection** of empty directories only
- ✅ **Progress reporting** with count of files added

## 🔧 Customization

To customize for your project, edit the scripts and replace:

### Package Structure
```bash
# Current:
"src/main/java/app/quantun/blog"

# Change to your package:
"src/main/java/com/yourcompany/yourapp"
```

### Database Type
```bash
# Current:
"persistence/mongo"

# Change to:
"persistence/postgresql"  # or "redis", "elasticsearch", etc.
```

## 🎬 Example Usage

### Full Setup for New Project
```bash
# Create project directory
mkdir my-hexagonal-blog
cd my-hexagonal-blog

# Copy these scripts to your project
# Run setup script
./setup-hexagonal-architecture.sh

# Add Git tracking for empty folders
./add-gitkeep.sh

# Initialize Git repository
git init
git add .
git commit -m "Initial hexagonal architecture structure"
```

## 🔍 Verification

After running the scripts, verify the structure:

```bash
# Check main structure
tree src/ -d

# Verify .gitkeep files
find src/ -name ".gitkeep" -type f
```

## 🎉 What's Next?

1. **Start with domain modeling:**
   - Create entities in `domain/model/`
   - Define value objects in `shared/valueobject/`

2. **Define your ports:**
   - Input ports (use cases) in `application/port/in/`
   - Output ports (repositories) in `application/port/out/`

3. **Implement adapters:**
   - Controllers in `infrastructure/adapter/in/web/`
   - Repositories in `infrastructure/adapter/out/persistence/`

## 🚫 Troubleshooting

### PowerShell Execution Policy
If you get an execution policy error:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Bash Permission Denied
If you get permission denied:
```bash
chmod +x setup-hexagonal-architecture.sh
chmod +x add-gitkeep.sh
```

### Folders Already Exist
The scripts handle existing folders gracefully and will show:
- ✅ **Created:** for new folders
- 📁 **Exists:** for existing folders

---

**🎯 Result:** A complete, production-ready folder structure following Hexagonal Architecture best practices!
