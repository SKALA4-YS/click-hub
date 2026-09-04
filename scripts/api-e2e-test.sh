#!/usr/bin/env bash
# Click HUB 백엔드 API E2E 자동 검증 실행 스크립트.
# backend/src/test/java/com/skala/clickhub/e2e/ApiE2ETests.java를 실행하고,
# 결과를 터미널에서 바로 훑어볼 수 있는 상세 리포트로 출력한다(발표 캡처용).
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
backend_dir="$repo_root/backend"
report_file="$backend_dir/build/e2e-report/api-e2e-report.md"
test_class="com.skala.clickhub.e2e.ApiE2ETests"

bold=$'\033[1m'
dim=$'\033[2m'
green=$'\033[32m'
red=$'\033[31m'
yellow=$'\033[33m'
cyan=$'\033[36m'
reset=$'\033[0m'

hr() { printf '%s\n' "──────────────────────────────────────────────────────────"; }

# ---------- 헤더 ----------
echo ""
echo "${bold}${cyan}╭──────────────────────────────────────────────────────────╮${reset}"
echo "${bold}${cyan}│  Click HUB · API E2E 자동 검증                            │${reset}"
echo "${bold}${cyan}│  실제 PostgreSQL(Testcontainers) + 실 HTTP 호출            │${reset}"
echo "${bold}${cyan}│  대상: 명세 1~9장, 38개 엔드포인트                        │${reset}"
echo "${bold}${cyan}╰──────────────────────────────────────────────────────────╯${reset}"
echo ""

# ---------- 실행 환경 ----------
git_branch="$(git -C "$repo_root" rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
git_commit="$(git -C "$repo_root" rev-parse --short HEAD 2>/dev/null || echo unknown)"
java_version="$(java -version 2>&1 | head -n1 | sed -E 's/^[^"]*"([^"]+)".*/\1/')"
gradle_version="$(cd "$backend_dir" && ./gradlew --version 2>/dev/null | awk '/^Gradle /{print $2}')"
run_started_at="$(date '+%Y-%m-%d %H:%M:%S')"

echo "${bold}실행 환경${reset}"
printf "  %-10s %s\n" "Branch"  "$git_branch (${git_commit})"
printf "  %-10s %s\n" "Java"    "$java_version"
printf "  %-10s %s\n" "Gradle"  "$gradle_version"
printf "  %-10s %s\n" "Started" "$run_started_at"
echo ""
echo "${dim}▶ cd backend && ./gradlew test --tests \"${test_class}\"${reset}"
echo ""

# ---------- 테스트 실행 ----------
start_ts=$(date +%s)
cd "$backend_dir"
if ./gradlew test --tests "$test_class"; then
  gradle_status=0
else
  gradle_status=$?
fi
end_ts=$(date +%s)
elapsed=$((end_ts - start_ts))

echo ""
if [ ! -f "$report_file" ]; then
  echo "${red}리포트 파일을 찾을 수 없습니다: $report_file${reset}"
  exit 1
fi

total=$(grep -c '^| [✅❌] |' "$report_file" || true)
passed=$(grep -c '^| ✅ |' "$report_file" || true)
failed=$((total - passed))
# 컨트롤러 매핑 기준 실제 엔드포인트 수(38개, {id} 자리에 매 실행마다 다른 UUID가
# 들어가서 문자열로는 중복제거가 안 되므로 정적으로 고정한다).
endpoints=38

# ---------- 결과 요약 ----------
hr
if [ "$failed" -eq 0 ]; then
  echo "${bold}${green}  ✅ RESULT   ${passed} / ${total} 시나리오 통과   (${endpoints}개 엔드포인트, ${elapsed}초 소요)${reset}"
  echo "${green}     Method · Path · Status Code — RESTful 규격 100% 준수${reset}"
else
  echo "${bold}${red}  ❌ RESULT   ${passed} / ${total} 시나리오 통과 (${failed}건 실패, ${elapsed}초 소요)${reset}"
fi
hr
echo ""

# ---------- 검증 중 발견 → 수정한 이슈 ----------
echo "${bold}${yellow}검증 중 발견 → 수정한 이슈${reset}"
echo "  POST 생성 API 4곳이 응답 바디엔 ${bold}201${reset}이라 표시하면서, ResponseEntity로"
echo "  감싸지 않아 실제 HTTP 응답은 ${red}200${reset}으로 나가고 있었다. 실제 HTTP 레벨 테스트로만"
echo "  드러난 결함이라 바로 ${green}ResponseEntity<ApiResponse<T>>${reset}로 고쳐 재검증했다."
echo "    - POST /v1/projects"
echo "    - POST /v1/community/boards/{slug}/posts"
echo "    - POST /v1/community/posts/{id}/comments"
echo "    - POST /v1/projects/{id}/comments"
echo ""

# ---------- 도메인별 결과 (막대그래프) ----------
echo "${bold}도메인별 결과${reset}"
awk -F'|' -v green="$green" -v red="$red" -v reset="$reset" '
  /^\| [✅❌] \|/ {
    result=$2; domain=$3;
    gsub(/^ +| +$/, "", result);
    gsub(/^ +| +$/, "", domain);
    total[domain]++;
    if (result == "✅") pass[domain]++;
    if (!seen[domain]++) order[++n] = domain;
  }
  END {
    # 한글은 터미널에서 2칸을 차지해 printf %-Ns로는 정렬이 안 맞으므로,
    # 도메인 이름별 표시 폭에 맞춰 미리 계산한 여백을 직접 붙인다(14칸 기준).
    pad["공개/선택인증"] = " ";
    pad["인증게이트"] = "    ";
    pad["사용자프로필"] = "  ";
    pad["프로젝트(2장)"] = " ";
    pad["커뮤니티(9장)"] = " ";
    pad["구독(6장)"] = "     ";
    pad["알림(6장)"] = "     ";
    for (i = 1; i <= n; i++) {
      d = order[i];
      bars = int((pass[d] / total[d]) * 20);
      bar = "";
      for (j = 0; j < 20; j++) bar = bar (j < bars ? "█" : "░");
      color = (pass[d] == total[d]) ? green : red;
      spacer = (d in pad) ? pad[d] : "  ";
      printf("  %s%s%s%s%s %2d/%2d\n", d, spacer, color, bar, reset, pass[d], total[d]);
    }
  }
' "$report_file"
echo ""

# ---------- 상세 결과 (전체 시나리오) ----------
echo "${bold}상세 결과 (${total}건)${reset}"
awk -F'|' -v green="$green" -v red="$red" -v dim="$dim" -v reset="$reset" '
  /^\| [✅❌] \|/ {
    result=$2; domain=$3; method=$4; path=$5; scenario=$6; expected=$7; actual=$8;
    gsub(/^ +| +$/, "", result); gsub(/^ +| +$/, "", domain); gsub(/^ +| +$/, "", method);
    gsub(/^ +| +$/, "", path); gsub(/`/, "", path); gsub(/^ +| +$/, "", scenario);
    gsub(/^ +| +$/, "", expected); gsub(/^ +| +$/, "", actual);
    if (domain != lastDomain) {
      if (lastDomain != "") print "";
      printf("  %s%s%s\n", "\033[1m", domain, reset);
      lastDomain = domain;
    }
    icon = (result == "✅") ? green"✔"reset : red"✘"reset;
    code = (expected == actual) ? green : red;
    printf("    %s %-6s %-42s %s (%s%s → %s%s)\n", icon, method, path, scenario, code, expected, actual, reset);
  }
' "$report_file"

echo ""
echo "${dim}리포트 전문: ${report_file#$repo_root/}${reset}"
echo ""

exit "$gradle_status"
