#!/usr/bin/env bash
# Click HUB 백엔드 API E2E 자동 검증 실행 스크립트.
# backend/src/test/java/com/skala/clickhub/e2e/ApiE2ETests.java를 실행하고,
# 결과를 터미널에서 바로 훑어볼 수 있는 요약으로 출력한다.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
backend_dir="$repo_root/backend"
report_file="$backend_dir/build/e2e-report/api-e2e-report.md"

bold=$'\033[1m'
dim=$'\033[2m'
green=$'\033[32m'
red=$'\033[31m'
cyan=$'\033[36m'
reset=$'\033[0m'

echo ""
echo "${bold}${cyan}╭──────────────────────────────────────────────────────────╮${reset}"
echo "${bold}${cyan}│  Click HUB · API E2E 자동 검증                            │${reset}"
echo "${bold}${cyan}│  실제 PostgreSQL(Testcontainers) + 실 HTTP 호출            │${reset}"
echo "${bold}${cyan}│  대상: 명세 1~9장, 38개 엔드포인트                        │${reset}"
echo "${bold}${cyan}╰──────────────────────────────────────────────────────────╯${reset}"
echo ""
echo "${dim}▶ cd backend && ./gradlew test --tests \"com.skala.clickhub.e2e.ApiE2ETests\"${reset}"
echo ""

cd "$backend_dir"
if ./gradlew test --tests "com.skala.clickhub.e2e.ApiE2ETests"; then
  gradle_status=0
else
  gradle_status=$?
fi

echo ""
if [ ! -f "$report_file" ]; then
  echo "${red}리포트 파일을 찾을 수 없습니다: $report_file${reset}"
  exit 1
fi

total=$(grep -c '^| [✅❌] |' "$report_file" || true)
passed=$(grep -c '^| ✅ |' "$report_file" || true)
failed=$((total - passed))

echo "${bold}──────────────────────────────────────────────────────────${reset}"
if [ "$failed" -eq 0 ]; then
  echo "${bold}${green}  ✅ RESULT  ${passed} / ${total} 시나리오 통과${reset}"
  echo "${green}     Method · Path · Status Code — RESTful 규격 100% 준수${reset}"
else
  echo "${bold}${red}  ❌ RESULT  ${passed} / ${total} 시나리오 통과 (${failed}건 실패)${reset}"
fi
echo "${bold}──────────────────────────────────────────────────────────${reset}"
echo ""
echo "${bold}도메인별 결과${reset}"

awk -F'|' '
  /^\| [✅❌] \|/ {
    result=$2; domain=$3;
    gsub(/^ +| +$/, "", result);
    gsub(/^ +| +$/, "", domain);
    total[domain]++;
    if (result == "✅") pass[domain]++;
    if (!seen[domain]++) order[++n] = domain;
  }
  END {
    for (i = 1; i <= n; i++) {
      d = order[i];
      printf("  %-14s %2d/%2d\n", d, pass[d], total[d]);
    }
  }
' "$report_file"

echo ""
echo "${dim}리포트 전문: ${report_file#$repo_root/}${reset}"
echo ""

exit "$gradle_status"
