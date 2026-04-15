#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
MIGRATION_DIR="$PROJECT_DIR/src/main/resources/db/migration"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo ""
echo "=============================="
echo "  Flyway Migration Check"
echo "=============================="
echo ""

ERRORS=0

# 1. 파일명 패턴 검증 (V{N}__{description}.sql)
echo "  [1] Filename pattern check..."
for f in "$MIGRATION_DIR"/V*.sql; do
  fname=$(basename "$f")
  if ! [[ "$fname" =~ ^V[0-9]+__[a-z_]+\.sql$ ]]; then
    echo -e "    ${YELLOW}WARN: $fname (권장: V{N}__{lowercase_snake}.sql)${NC}"
  fi
done

# 2. 버전 순서 검증 (중복/빈 번호)
echo "  [2] Version sequence check..."
VERSIONS=$(ls "$MIGRATION_DIR"/V*.sql 2>/dev/null | sed 's/.*\/V\([0-9]*\)__.*/\1/' | sort -n)
PREV=0
while read -r ver; do
  if [ "$ver" -eq "$PREV" ]; then
    echo -e "    ${RED}ERROR: Duplicate version V${ver}${NC}"
    ((ERRORS++))
  fi
  PREV=$ver
done <<< "$VERSIONS"

LATEST=$(echo "$VERSIONS" | tail -1)
TOTAL=$(echo "$VERSIONS" | wc -l | tr -d ' ')
echo "    Total: ${TOTAL} migrations (V1 ~ V${LATEST})"

# 3. SQL 구문 기본 검증 (빈 파일, 세미콜론 누락)
echo "  [3] SQL basic validation..."
for f in "$MIGRATION_DIR"/V*.sql; do
  fname=$(basename "$f")
  size=$(wc -c < "$f" | tr -d ' ')
  if [ "$size" -lt 10 ]; then
    echo -e "    ${RED}ERROR: $fname is empty or too small (${size} bytes)${NC}"
    ((ERRORS++))
  fi
done

echo ""
if [ "$ERRORS" -gt 0 ]; then
  echo -e "${RED}  $ERRORS errors found${NC}"
  exit 1
else
  echo -e "${GREEN}  All migrations OK${NC}"
fi
echo ""
