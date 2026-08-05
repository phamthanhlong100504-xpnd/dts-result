<#
.SYNOPSIS
    Cập nhật .agents submodule lên phiên bản mới nhất.

.DESCRIPTION
    Script PowerShell để update submodule AI-Coding trong thư mục .agents/
    lên phiên bản mới nhất hoặc version cụ thể.

.PARAMETER TargetDir
    Thư mục submodule. Default: .agents

.PARAMETER Version
    Tag hoặc commit hash cụ thể để checkout. Nếu không chỉ định thì update lên bản mới nhất.

.EXAMPLE
    .\update.ps1
    .\update.ps1 -Version "v1.2.0"
    .\update.ps1 -TargetDir ".agents" -Version "abc1234"
#>

param(
    [string]$TargetDir = ".agents",
    [string]$Version   = ""
)

# ─── Colors & Helpers ────────────────────────────────────────────────────────
function Write-Info    { param([string]$Msg) Write-Host "[INFO]    $Msg" -ForegroundColor Blue }
function Write-Success { param([string]$Msg) Write-Host "[SUCCESS] $Msg" -ForegroundColor Green }
function Write-Warn    { param([string]$Msg) Write-Host "[WARN]    $Msg" -ForegroundColor Yellow }
function Write-Err     { param([string]$Msg) Write-Host "[ERROR]   $Msg" -ForegroundColor Red }

function Print-Banner {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║          🔄 AI-Coding Agent Update (Git Submodule)         ║" -ForegroundColor Cyan
    Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

# ─── Main ────────────────────────────────────────────────────────────────────
Print-Banner

# 1. Check git
try {
    $null = git --version 2>&1
}
catch {
    Write-Err "Git chua duoc cai dat."
    exit 1
}

# 2. Check we are in a git repo
$isGitRepo = git rev-parse --is-inside-work-tree 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Err "Thu muc hien tai khong phai la mot git repository."
    exit 1
}

# 3. Navigate to git root
$gitRoot = (git rev-parse --show-toplevel 2>&1).Trim()
$savedLocation = Get-Location
Set-Location $gitRoot

# 4. Check submodule exists
if (-not (Test-Path $TargetDir)) {
    Write-Err "Thu muc '$TargetDir' khong ton tai."
    Write-Err "Hay chay setup.ps1 truoc de them submodule."
    Set-Location $savedLocation
    exit 1
}

$gitmodulesPath = Join-Path (Get-Location) ".gitmodules"
if (-not (Test-Path $gitmodulesPath)) {
    Write-Err "'$TargetDir' khong phai la git submodule."
    Write-Err "Hay chay setup.ps1 de them submodule dung cach."
    Set-Location $savedLocation
    exit 1
}

$gitmodulesContent = Get-Content $gitmodulesPath -Raw
if (-not ($gitmodulesContent -match [regex]::Escape($TargetDir))) {
    Write-Err "'$TargetDir' khong phai la git submodule."
    Set-Location $savedLocation
    exit 1
}

Write-Info "Submodule: $TargetDir"
Write-Host ""

# 5. Get current commit before update
Push-Location $TargetDir
$currentCommit = (git rev-parse --short HEAD 2>&1).Trim()
Pop-Location
Write-Info "Phien ban hien tai: $currentCommit"

# 6. Update
if ($Version -ne "") {
    # Checkout specific version
    Write-Info "Dang checkout version: $Version"
    Push-Location $TargetDir
    git fetch --all --tags
    git checkout $Version
    Pop-Location
}
else {
    # Update to latest
    Write-Info "Dang update len ban moi nhat..."
    git submodule update --remote $TargetDir
}

# 7. Get new commit
Push-Location $TargetDir
$newCommit = (git rev-parse --short HEAD 2>&1).Trim()
Pop-Location

# 8. Show changelog
Write-Host ""
if ($currentCommit -ne $newCommit) {
    Write-Success "Da update: $currentCommit -> $newCommit"
    Write-Host ""
    Write-Host "Changelog:" -ForegroundColor White
    Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Cyan

    Push-Location $TargetDir
    git log --oneline --no-decorate "${currentCommit}..${newCommit}" 2>&1 | ForEach-Object {
        Write-Host "  $_"
    }
    Pop-Location

    Write-Host "─────────────────────────────────────────────────────────────" -ForegroundColor Cyan
    Write-Host ""
    Write-Host " Dung quen commit thay doi:" -ForegroundColor White
    Write-Host "   git add $TargetDir" -ForegroundColor Cyan
    Write-Host "   git commit -m `"chore: update AI-Coding agents to $newCommit`"" -ForegroundColor Cyan
}
else {
    Write-Success "Da o phien ban moi nhat ($currentCommit). Khong co thay doi."
}

Write-Host ""

# Restore original location
Set-Location $savedLocation
