<#
.SYNOPSIS
    Thêm repo doc-manual (AI-Coding) vào project dưới dạng git submodule.

.DESCRIPTION
    Script PowerShell để tự động thêm repo AI-Coding vào thư mục .agents/
    của project đích dưới dạng Git Submodule.

.PARAMETER RepoUrl
    URL của repo AI-Coding. Default: https://github.com/phamthanhlong100504-xpnd/doc-manual.git

.PARAMETER Branch
    Branch để track. Default: main

.PARAMETER TargetDir
    Thư mục đích cho submodule. Default: .agents

.EXAMPLE
    .\setup.ps1
    .\setup.ps1 -RepoUrl "https://github.com/my-org/doc-manual.git"
    .\setup.ps1 -Branch "develop" -TargetDir ".agents"
#>

param(
    [string]$RepoUrl = "https://github.com/phamthanhlong100504-xpnd/doc-manual.git",
    [string]$Branch  = "main",
    [string]$TargetDir = ".agents"
)

# ─── Colors & Helpers ────────────────────────────────────────────────────────
function Write-Info    { param([string]$Msg) Write-Host "[INFO]    $Msg" -ForegroundColor Blue }
function Write-Success { param([string]$Msg) Write-Host "[SUCCESS] $Msg" -ForegroundColor Green }
function Write-Warn    { param([string]$Msg) Write-Host "[WARN]    $Msg" -ForegroundColor Yellow }
function Write-Err     { param([string]$Msg) Write-Host "[ERROR]   $Msg" -ForegroundColor Red }

function Print-Banner {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║           🚀 AI-Coding Agent Setup (Git Submodule)         ║" -ForegroundColor Cyan
    Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

# ─── Main ────────────────────────────────────────────────────────────────────
Print-Banner

# 1. Check git is installed
try {
    $gitVersion = git --version 2>&1
    Write-Info "Git da duoc cai dat: $gitVersion"
}
catch {
    Write-Err "Git chua duoc cai dat. Vui long cai Git truoc."
    exit 1
}

# 2. Check we are in a git repo
$isGitRepo = git rev-parse --is-inside-work-tree 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Err "Thu muc hien tai khong phai la mot git repository."
    Write-Err "Hay chay script nay tu thu muc goc cua project dich."
    Write-Host ""
    Write-Host "  Vi du:"
    Write-Host "    cd C:\path\to\your-project"
    Write-Host "    .\path\to\setup.ps1"
    exit 1
}

# 3. Navigate to git root
$gitRoot = (git rev-parse --show-toplevel 2>&1).Trim()
$currentDir = (Get-Location).Path

# Normalize paths for comparison
$normalizedGitRoot = $gitRoot -replace '/', '\'
$normalizedCurrentDir = $currentDir -replace '/', '\'

if ($normalizedGitRoot -ne $normalizedCurrentDir) {
    Write-Warn "Ban khong o thu muc goc cua git repo."
    Write-Warn "Dang chuyen den: $gitRoot"
    Set-Location $gitRoot
}

Write-Info "Project root: $(Get-Location)"
Write-Info "Repo URL:     $RepoUrl"
Write-Info "Branch:       $Branch"
Write-Info "Target:       $TargetDir"
Write-Host ""

# 4. Check if target already exists
if (Test-Path $TargetDir) {
    $gitmodulesPath = Join-Path (Get-Location) ".gitmodules"
    $isSubmodule = $false

    if (Test-Path $gitmodulesPath) {
        $content = Get-Content $gitmodulesPath -Raw
        if ($content -match [regex]::Escape($TargetDir)) {
            $isSubmodule = $true
        }
    }

    if ($isSubmodule) {
        Write-Warn "'$TargetDir' da ton tai nhu mot git submodule."
        Write-Host ""
        $confirm = Read-Host "Ban co muon update len ban moi nhat? (y/N)"
        if ($confirm -eq 'y' -or $confirm -eq 'Y') {
            Write-Info "Dang update submodule..."
            git submodule update --remote $TargetDir
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Da update '$TargetDir' len ban moi nhat!"
            }
            else {
                Write-Err "Update that bai. Vui long kiem tra lai."
            }
            exit 0
        }
        else {
            Write-Info "Da huy. Khong co thay doi nao."
            exit 0
        }
    }
    else {
        Write-Err "'$TargetDir' da ton tai nhung khong phai la git submodule."
        Write-Err "Vui long xoa hoac doi ten thu muc '$TargetDir' truoc khi chay lai script."
        exit 1
    }
}

# 5. Add submodule
Write-Info "Dang them git submodule..."
git submodule add -b $Branch $RepoUrl $TargetDir
if ($LASTEXITCODE -ne 0) {
    Write-Err "Khong the them submodule. Kiem tra URL va quyen truy cap."
    exit 1
}

# 6. Initialize and update
Write-Info "Dang khoi tao submodule..."
git submodule init
git submodule update

# 7. Verify
$rulesDir = Join-Path $TargetDir "rules"
$workflowsDir = Join-Path $TargetDir "workflows"

if ((Test-Path $rulesDir) -and (Test-Path $workflowsDir)) {
    Write-Success "Cai dat thanh cong! ✅"
}
else {
    Write-Warn "Submodule da duoc them nhung cau truc chua dung. Hay kiem tra lai."
}

# 8. Print summary
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host " ✅ Setup hoan tat!" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host " Cau truc da tao:"
Write-Host "   $(Get-Location)\"
Write-Host "   └── $TargetDir\"
Write-Host "       ├── rules/"
Write-Host "       ├── workflows/"
Write-Host "       ├── manifest.json"
Write-Host "       └── README.md"
Write-Host ""
Write-Host " Buoc tiep theo:" -ForegroundColor White
Write-Host "   1. Commit thay doi:" -ForegroundColor White
Write-Host "      git add .gitmodules $TargetDir" -ForegroundColor Cyan
Write-Host "      git commit -m `"chore: add AI-Coding agents submodule`"" -ForegroundColor Cyan
Write-Host ""
Write-Host "   2. Khi dong nghiep clone project:" -ForegroundColor White
Write-Host "      git clone --recurse-submodules <project-url>" -ForegroundColor Cyan
Write-Host ""
Write-Host "   3. Neu da clone roi ma chua co ${TargetDir}:" -ForegroundColor White
Write-Host "      git submodule init; git submodule update" -ForegroundColor Cyan
Write-Host ""
Write-Host "   4. Update len ban moi nhat:" -ForegroundColor White
Write-Host "      git submodule update --remote $TargetDir" -ForegroundColor Cyan
Write-Host ""
