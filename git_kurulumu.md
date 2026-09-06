# GitHub'a Commit Rehberi

## 1. Git Kurulumu (Henüz yüklü değilse)

1. https://git-scm.com/download/win adresinden Git'i indirin
2. Setup'ı çalıştırın ve varsayılan ayarlarla kurun
3. Bilgisayarı yeniden başlatın

## 2. GitHub Repository Oluşturma

1. GitHub.com'a gidin
2. Yeni repository oluşturun: "health-insurance-system"
3. Public veya Private seçin
4. README.md ekleme (şimdilik değil, sonra ekleriz)
5. "Create repository" butonuna tıklayın

## 3. Git Komutları

### Terminal'i açın ve proje dizinine gidin:
```bash
cd C:\Users\mertc\OneDrive\Masaüstü\micro
```

### Git'i başlatın:
```bash
git init
```

### Tüm dosyaları ekleyin:
```bash
git add .
```

### İlk commit'i yapın:
```bash
git commit -m "Initial commit: Health Insurance Claims Management System"
```

### GitHub repository'sini ekleyin:
```bash
git remote add origin https://github.com/KULLANICI_ADI/health-insurance-system.git
```

### Kodları GitHub'a gönderin:
```bash
git branch -M main
git push -u origin main
```

## 4. Sonraki Commit'ler

Değişiklikleri göndermek için:
```bash
git add .
git commit -m "Açıklama mesajı"
git push
```

## 5. GitHub Desktop Alternatifi

CLI yerine GitHub Desktop kullanabilirsiniz:

1. https://desktop.github.com/ adresinden indirin
2. Kurulumu yapın
3. GitHub hesabınızla giriş yapın
4. "File" -> "Add Local Repository" seçin
5. Proje dizininizi seçin
6. Değişiklikleri yapın
7. "Commit to main" butonuna tıklayın
8. "Publish repository" ile GitHub'a gönderin

## 6. Proje Dosyalarını Kontrol Edin

Commit yapmadan önce şu dosyaların olduğundan emin olun:
- README.md (detaylı proje açıklaması)
- ARCHITECTURE.md (mimari dokümantasyonu)
- docker-compose.yml (Docker konfigürasyonu)
- .github/workflows/ci-cd.yml (CI/CD pipeline)
- Tüm Java sınıfları (Türkçe açıklamalarla)
- pom.xml dosyaları (her service için)
- application.yaml dosyaları (her service için)

## 7. İgnore Edilecek Dosyalar

.gitignore dosyası ekleyin (şu anda ekli değilse):
```bash
echo "# Logs
*.log
logs/

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db

# Maven
target/
.mvn/

# Docker
.dockerignore" > .gitignore
```

## 8. Commit Mesajı Örnekleri

İyi commit mesajları:
- "feat: Add JWT authentication to all services"
- "fix: Redis cache configuration issue"
- "docs: Update architecture documentation"
- "refactor: Improve claim service error handling"
- "test: Add integration tests for saga pattern"