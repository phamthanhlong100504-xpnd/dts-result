#!/usr/bin/env bash
# =============================================================================
# setup.sh — Thêm repo doc-manual (AI-Coding) vào project dưới dạng git submodule
#
# Cách dùng:
#   Chạy từ THƯ MỤC GỐC của project đích:
#
#   bash path/to/setup.sh
#   bash path/to/setup.sh --repo <custom-url>
#   bash path/to/setup.sh --branch <branch-name>
#   bash path/to/setup.sh --target <target-dir>
#
#   Hoặc dùng curl:
#   curl -sSL https://raw.githubusercontent.com/phamthanhlong100504-xpnd/doc-manual/main/scripts/setup.sh | bash
# =============================================================================

set -euo pipefail

# ─── Defaults ────────────────────────────────────────────────────────────────
DEFAULT_REPO="https://github.com/phamthanhlong100504-xpnd/doc-manual.git"
DEFAULT_BRANCH="main"
DEFAULT_TARGET=".agents"

# ─── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ─── Helper Functions ────────────────────────────────────────────────────────
info()    { echo -e "${BLUE}[INFO]${NC}    $*"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}    $*"; }
error()   { echo -e "${RED}[ERROR]${NC}   $*" >&2; }

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║           🚀 AI-Coding Agent Setup (Git Submodule)         ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_usage() {
    echo "Usage: bash setup.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --repo    <url>     URL của repo AI-Coding (default: $DEFAULT_REPO)"
    echo "  --branch  <name>    Branch để track (default: $DEFAULT_BRANCH)"
    echo "  --target  <dir>     Thư mục đích (default: $DEFAULT_TARGET)"
    echo "  --help              Hiển thị hướng dẫn này"
    echo ""
    echo "Examples:"
    echo "  bash setup.sh"
    echo "  bash setup.sh --repo https://github.com/my-org/doc-manual.git"
    echo "  bash setup.sh --branch develop --target .agents"
}

# ─── Parse Arguments ─────────────────────────────────────────────────────────
REPO_URL="$DEFAULT_REPO"
BRANCH="$DEFAULT_BRANCH"
TARGET_DIR="$DEFAULT_TARGET"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --repo)
            REPO_URL="$2"
            shift 2
            ;;
        --branch)
            BRANCH="$2"
            shift 2
            ;;
        --target)
            TARGET_DIR="$2"
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

# 1. Check git is installed
if ! command -v git &> /dev/null; then
    error "Git chưa được cài đặt. Vui lòng cài Git trước."
    exit 1
fi
info "Git đã được cài đặt: $(git --version)"

# 2. Check we are in a git repo
if ! git rev-parse --is-inside-work-tree &> /dev/null; then
    error "Thư mục hiện tại không phải là một git repository."
    error "Hãy chạy script này từ thư mục gốc của project đích."
    echo ""
    echo "  Ví dụ:"
    echo "    cd /path/to/your-project"
    echo "    bash path/to/setup.sh"
    exit 1
fi

# 3. Ensure we are at the root of the git repo
GIT_ROOT=$(git rev-parse --show-toplevel)
CURRENT_DIR=$(pwd)
if [ "$GIT_ROOT" != "$CURRENT_DIR" ]; then
    warn "Bạn không ở thư mục gốc của git repo."
    warn "Đang chuyển đến: $GIT_ROOT"
    cd "$GIT_ROOT"
fi

info "Project root: $(pwd)"
info "Repo URL:     $REPO_URL"
info "Branch:       $BRANCH"
info "Target:       $TARGET_DIR"
echo ""

# 4. Check if target already exists
if [ -d "$TARGET_DIR" ]; then
    if [ -f ".gitmodules" ] && grep -q "$TARGET_DIR" ".gitmodules" 2>/dev/null; then
        warn "'$TARGET_DIR' đã tồn tại như một git submodule."
        echo ""
        read -p "$(echo -e "${YELLOW}Bạn có muốn update lên bản mới nhất? (y/N): ${NC}")" CONFIRM
        if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
            info "Đang update submodule..."
            git submodule update --remote "$TARGET_DIR"
            success "Đã update '$TARGET_DIR' lên bản mới nhất!"
            exit 0
        else
            info "Đã hủy. Không có thay đổi nào."
            exit 0
        fi
    else
        error "'$TARGET_DIR' đã tồn tại nhưng không phải là git submodule."
        error "Vui lòng xóa hoặc đổi tên thư mục '$TARGET_DIR' trước khi chạy lại script."
        exit 1
    fi
fi

# 5. Add submodule
info "Đang thêm git submodule..."
git submodule add -b "$BRANCH" "$REPO_URL" "$TARGET_DIR"

# 6. Initialize and update
info "Đang khởi tạo submodule..."
git submodule init
git submodule update

# 7. Verify
if [ -d "$TARGET_DIR/rules" ] && [ -d "$TARGET_DIR/workflows" ]; then
    success "Cài đặt thành công! ✅"
else
    warn "Submodule đã được thêm nhưng cấu trúc chưa đúng. Hãy kiểm tra lại."
fi

# 8. Print summary
echo ""
echo -e "${CYAN}${BOLD}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}${BOLD} ✅ Setup hoàn tất!${NC}"
echo -e "${CYAN}${BOLD}═══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e " Cấu trúc đã tạo:"
echo -e "   $(pwd)/"
echo -e "   └── ${BOLD}$TARGET_DIR/${NC}"
echo -e "       ├── rules/"
echo -e "       ├── workflows/"
echo -e "       ├── manifest.json"
echo -e "       └── README.md"
echo ""
echo -e " ${BOLD}Bước tiếp theo:${NC}"
echo -e "   1. Commit thay đổi:"
echo -e "      ${CYAN}git add .gitmodules $TARGET_DIR${NC}"
echo -e "      ${CYAN}git commit -m \"chore: add AI-Coding agents submodule\"${NC}"
echo ""
echo -e "   2. Khi đồng nghiệp clone project:"
echo -e "      ${CYAN}git clone --recurse-submodules <project-url>${NC}"
echo ""
echo -e "   3. Nếu đã clone rồi mà chưa có $TARGET_DIR:"
echo -e "      ${CYAN}git submodule init && git submodule update${NC}"
echo ""
echo -e "   4. Update lên bản mới nhất:"
echo -e "      ${CYAN}git submodule update --remote $TARGET_DIR${NC}"
echo ""
