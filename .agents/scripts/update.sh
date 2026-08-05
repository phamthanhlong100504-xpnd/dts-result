#!/usr/bin/env bash
# =============================================================================
# update.sh — Cập nhật .agents submodule lên phiên bản mới nhất
#
# Cách dùng:
#   Chạy từ THƯ MỤC GỐC của project đích:
#
#   bash path/to/update.sh
#   bash path/to/update.sh --target .agents
#   bash path/to/update.sh --version v1.2.0
# =============================================================================

set -euo pipefail

# ─── Defaults ────────────────────────────────────────────────────────────────
DEFAULT_TARGET=".agents"

# ─── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ─── Helper Functions ────────────────────────────────────────────────────────
info()    { echo -e "${BLUE}[INFO]${NC}    $*"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}    $*"; }
error()   { echo -e "${RED}[ERROR]${NC}   $*" >&2; }

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║          🔄 AI-Coding Agent Update (Git Submodule)         ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_usage() {
    echo "Usage: bash update.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --target   <dir>      Thư mục submodule (default: $DEFAULT_TARGET)"
    echo "  --version  <tag>      Checkout version cụ thể (tag hoặc commit hash)"
    echo "  --help                Hiển thị hướng dẫn này"
    echo ""
    echo "Examples:"
    echo "  bash update.sh"
    echo "  bash update.sh --version v1.2.0"
    echo "  bash update.sh --target .agents --version abc1234"
}

# ─── Parse Arguments ─────────────────────────────────────────────────────────
TARGET_DIR="$DEFAULT_TARGET"
VERSION=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --target)
            TARGET_DIR="$2"
            shift 2
            ;;
        --version)
            VERSION="$2"
            shift 2
            ;;
        --help)
            print_usage
            exit 0
            ;;
        *)
            error "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# ─── Main ────────────────────────────────────────────────────────────────────
print_banner

# 1. Check git
if ! command -v git &> /dev/null; then
    error "Git chưa được cài đặt."
    exit 1
fi

# 2. Check we are in a git repo
if ! git rev-parse --is-inside-work-tree &> /dev/null; then
    error "Thư mục hiện tại không phải là một git repository."
    exit 1
fi

# 3. Navigate to git root
GIT_ROOT=$(git rev-parse --show-toplevel)
cd "$GIT_ROOT"

# 4. Check submodule exists
if [ ! -d "$TARGET_DIR" ]; then
    error "Thư mục '$TARGET_DIR' không tồn tại."
    error "Hãy chạy setup.sh trước để thêm submodule."
    exit 1
fi

if [ ! -f ".gitmodules" ] || ! grep -q "$TARGET_DIR" ".gitmodules" 2>/dev/null; then
    error "'$TARGET_DIR' không phải là git submodule."
    error "Hãy chạy setup.sh để thêm submodule đúng cách."
    exit 1
fi

info "Submodule: $TARGET_DIR"
echo ""

# 5. Get current commit before update
CURRENT_COMMIT=$(cd "$TARGET_DIR" && git rev-parse --short HEAD 2>/dev/null || echo "unknown")
info "Phiên bản hiện tại: $CURRENT_COMMIT"

# 6. Update
if [ -n "$VERSION" ]; then
    # Checkout specific version
    info "Đang checkout version: $VERSION"
    cd "$TARGET_DIR"
    git fetch --all --tags
    git checkout "$VERSION"
    cd "$GIT_ROOT"
else
    # Update to latest
    info "Đang update lên bản mới nhất..."
    git submodule update --remote "$TARGET_DIR"
fi

# 7. Get new commit
NEW_COMMIT=$(cd "$TARGET_DIR" && git rev-parse --short HEAD 2>/dev/null || echo "unknown")

# 8. Show changelog
echo ""
if [ "$CURRENT_COMMIT" != "$NEW_COMMIT" ]; then
    success "Đã update: $CURRENT_COMMIT → $NEW_COMMIT"
    echo ""
    echo -e "${BOLD}Changelog:${NC}"
    echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
    cd "$TARGET_DIR"
    git log --oneline --no-decorate "${CURRENT_COMMIT}..${NEW_COMMIT}" 2>/dev/null || \
        git log --oneline --no-decorate -5
    cd "$GIT_ROOT"
    echo -e "${CYAN}─────────────────────────────────────────────────────────────${NC}"
    echo ""
    echo -e " ${BOLD}Đừng quên commit thay đổi:${NC}"
    echo -e "   ${CYAN}git add $TARGET_DIR${NC}"
    echo -e "   ${CYAN}git commit -m \"chore: update AI-Coding agents to $NEW_COMMIT\"${NC}"
else
    success "Đã ở phiên bản mới nhất ($CURRENT_COMMIT). Không có thay đổi."
fi
echo ""
