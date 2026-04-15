#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo ""
echo "=============================="
echo "  Backend Setup"
echo "=============================="
echo ""

echo "  [1/3] Gradle build (skip tests)..."
./gradlew clean compileJava -q

echo ""
echo "  [2/3] Spotless format..."
./gradlew spotlessApply -q

echo ""
echo "  [3/3] Build JAR verification..."
./gradlew bootJar -q

echo ""
echo -e "${GREEN}  Setup complete!${NC}"
echo ""
echo "  Run:  ./gradlew bootRun --args='--spring.profiles.active=local'"
echo ""
echo -e "${YELLOW}  Prerequisites:${NC}"
echo "    - PostgreSQL (localhost:5432)"
echo "    - Redis (localhost:6379)"
echo "    - GEMINI_API_KEY (optional, for AI features)"
echo ""
