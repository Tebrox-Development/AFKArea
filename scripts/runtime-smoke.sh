#!/usr/bin/env bash
set -euo pipefail

PAPER_VERSION="${PAPER_VERSION:-26.2}"
PAPER_BUILD="${PAPER_BUILD:-121}"
VERTEXCORE_VERSION="${VERTEXCORE_VERSION:-v1.1.0}"
ENABLE_WORLDGUARD="${ENABLE_WORLDGUARD:-false}"
WORLDGUARD_VERSION="${WORLDGUARD_VERSION:-7.0.18}"
WORLDEDIT_VERSION="${WORLDEDIT_VERSION:-7.4.4}"

USER_AGENT="AFKArea-runtime-smoke/1.0 (https://github.com/Tebrox-Development/AFKArea)"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_SUFFIX="base"
if [[ "${ENABLE_WORLDGUARD}" == "true" ]]; then
  RUNTIME_SUFFIX="worldguard"
fi

RUNTIME_DIR="${ROOT_DIR}/target/runtime-smoke-${PAPER_VERSION}-${PAPER_BUILD}-${RUNTIME_SUFFIX}"
SERVER_DIR="${RUNTIME_DIR}/server"
DEPENDENCY_DIR="${RUNTIME_DIR}/dependencies"

LOG_FILE="${SERVER_DIR}/logs/latest.log"
BUILD_JSON="${RUNTIME_DIR}/paper-builds.json"

FINAL_NAME="$(
  mvn -B -ntp help:evaluate \
    -Dexpression=project.build.finalName \
    -q \
    -DforceStdout
)"

PLUGIN_JAR="${ROOT_DIR}/target/${FINAL_NAME}.jar"

rm -rf "${RUNTIME_DIR}"
mkdir -p "${SERVER_DIR}/plugins" "${DEPENDENCY_DIR}"

if [[ ! -f "${PLUGIN_JAR}" ]]; then
  echo "AFKArea build artifact not found: ${PLUGIN_JAR}" >&2
  exit 1
fi

echo "Resolving VertexCore ${VERTEXCORE_VERSION}..."

mvn -B -ntp \
  org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
  -Dartifact="com.github.Tebrox-Development:VertexCore:${VERTEXCORE_VERSION}" \
  -DoutputDirectory="${DEPENDENCY_DIR}"

VERTEXCORE_JAR="$(
  find "${DEPENDENCY_DIR}" \
    -maxdepth 1 \
    -type f \
    -iname 'vertexcore-*.jar' \
    -print \
    -quit
)"

if [[ -z "${VERTEXCORE_JAR}" || ! -f "${VERTEXCORE_JAR}" ]]; then
  echo "VertexCore runtime artifact could not be resolved." >&2
  find "${DEPENDENCY_DIR}" -maxdepth 1 -type f -print >&2 || true
  exit 1
fi

if [[ "${ENABLE_WORLDGUARD}" == "true" ]]; then
  echo "Downloading WorldEdit ${WORLDEDIT_VERSION} and WorldGuard ${WORLDGUARD_VERSION} distribution JARs..."

  WORLDEDIT_JAR="${DEPENDENCY_DIR}/worldedit-bukkit-${WORLDEDIT_VERSION}-dist.jar"
  WORLDGUARD_JAR="${DEPENDENCY_DIR}/worldguard-bukkit-${WORLDGUARD_VERSION}-dist.jar"

  curl --fail --silent --show-error --location \
    --header "User-Agent: ${USER_AGENT}" \
    "https://ci.enginehub.org/repository/download/bt10/29295%3Aid/worldedit-bukkit-7.4.4-dist.jar?branch=version%2F7.4.x&guest=1" \
    --output "${WORLDEDIT_JAR}"

  curl --fail --silent --show-error --location \
    --header "User-Agent: ${USER_AGENT}" \
    "https://ci.enginehub.org/repository/download/bt11/29860%3Aid/worldguard-bukkit-7.0.18-dist.jar?branch=version%2F7.0.x&guest=1" \
    --output "${WORLDGUARD_JAR}"

  if [[ ! -s "${WORLDEDIT_JAR}" ]]; then
    echo "WorldEdit distribution JAR could not be downloaded." >&2
    exit 1
  fi

  if [[ ! -s "${WORLDGUARD_JAR}" ]]; then
    echo "WorldGuard distribution JAR could not be downloaded." >&2
    exit 1
  fi
fi

echo "Downloading Paper ${PAPER_VERSION} build ${PAPER_BUILD}..."

curl --fail --silent --show-error --location \
  --header "User-Agent: ${USER_AGENT}" \
  "https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds" \
  --output "${BUILD_JSON}"

PAPER_URL="$(
  python3 - "${BUILD_JSON}" "${PAPER_BUILD}" <<'PY'
import json
import sys

path, build_id = sys.argv[1], int(sys.argv[2])

with open(path, encoding="utf-8") as handle:
    builds = json.load(handle)

build = next(
    (entry for entry in builds if entry.get("id") == build_id),
    None
)

if build is None:
    raise SystemExit(
        f"Pinned Paper build {build_id} not found"
    )

if build.get("channel") != "STABLE":
    raise SystemExit(
        f"Pinned Paper build {build_id} is not STABLE"
    )

download = build.get(
    "downloads",
    {}
).get("server:default")

if not download or not download.get("url"):
    raise SystemExit(
        f"Pinned Paper build {build_id} has no server download"
    )

print(download["url"])
PY
)"

curl --fail --silent --show-error --location \
  --header "User-Agent: ${USER_AGENT}" \
  "${PAPER_URL}" \
  --output "${SERVER_DIR}/paper.jar"

cp "${VERTEXCORE_JAR}" "${SERVER_DIR}/plugins/VertexCore.jar"
cp "${PLUGIN_JAR}" "${SERVER_DIR}/plugins/AFKArea.jar"

if [[ "${ENABLE_WORLDGUARD}" == "true" ]]; then
  cp "${WORLDEDIT_JAR}" "${SERVER_DIR}/plugins/WorldEdit.jar"
  cp "${WORLDGUARD_JAR}" "${SERVER_DIR}/plugins/WorldGuard.jar"
fi

printf 'eula=true\n' > "${SERVER_DIR}/eula.txt"

cat > "${SERVER_DIR}/server.properties" <<'EOF'
server-port=0
online-mode=false
enable-query=false
enable-rcon=false
motd=AFKArea Runtime Smoke
EOF

mkfifo "${SERVER_DIR}/console.in"

pushd "${SERVER_DIR}" >/dev/null

exec 3<>console.in

java \
  -Xms512M \
  -Xmx1024M \
  -jar paper.jar \
  --nogui \
  <console.in \
  >server-console.log \
  2>&1 &

SERVER_PID=$!

popd >/dev/null

cleanup() {
  if kill -0 "${SERVER_PID}" 2>/dev/null; then
    printf 'stop\n' >&3 || true

    for _ in $(seq 1 30); do
      kill -0 "${SERVER_PID}" 2>/dev/null || return 0
      sleep 1
    done

    kill "${SERVER_PID}" 2>/dev/null || true
  fi
}

trap cleanup EXIT

READY=0

for _ in $(seq 1 120); do
  if ! kill -0 "${SERVER_PID}" 2>/dev/null; then
    echo "Paper exited before reaching ready state." >&2
    cat "${SERVER_DIR}/server-console.log" >&2 || true
    exit 1
  fi

  if [[ -f "${LOG_FILE}" ]] \
    && grep -Fq 'VertexCore enabled.' "${LOG_FILE}" \
    && grep -Fq 'AFKArea has been enabled' "${LOG_FILE}" \
    && grep -Eq 'Done \([0-9.]+s\)! For help, type "help"' "${LOG_FILE}"; then

    READY=1
    break
  fi

  sleep 1
done

if [[ "${READY}" -ne 1 ]]; then
  echo "Timed out waiting for AFKArea runtime startup." >&2
  cat "${SERVER_DIR}/server-console.log" >&2 || true
  exit 1
fi

if [[ "${ENABLE_WORLDGUARD}" == "true" ]]; then
  if ! grep -Fq 'WorldGuard integration is available' "${LOG_FILE}"; then
    echo "AFKArea did not detect the WorldGuard integration as available." >&2
    cat "${LOG_FILE}" >&2
    exit 1
  fi
else
  if ! grep -Fq 'WorldGuard not found - Worldguard integration is disabled' "${LOG_FILE}"; then
    echo "AFKArea did not reach the expected no-WorldGuard fallback." >&2
    cat "${LOG_FILE}" >&2
    exit 1
  fi
fi

if grep -Eiq \
  '(Could not load.*(AFKArea|VertexCore|WorldGuard|WorldEdit)|Error occurred while enabling (AFKArea|VertexCore|WorldGuard|WorldEdit)|NoClassDefFoundError:.*(afkarea|vertexCore|worldguard|worldedit))' \
  "${LOG_FILE}"; then

  echo "Plugin startup error detected." >&2
  cat "${LOG_FILE}" >&2
  exit 1
fi

if [[ "${ENABLE_WORLDGUARD}" == "true" ]]; then
  echo "AFKArea, VertexCore and WorldGuard reached ready state."
else
  echo "AFKArea and VertexCore reached ready state without WorldGuard."
fi

printf 'stop\n' >&3

for _ in $(seq 1 60); do
  if ! kill -0 "${SERVER_PID}" 2>/dev/null; then
    wait "${SERVER_PID}"
    trap - EXIT
    break
  fi

  sleep 1
done

if kill -0 "${SERVER_PID}" 2>/dev/null; then
  echo "Paper did not stop within 60 seconds." >&2
  exit 1
fi

if ! grep -Fq 'AFKArea has been disabled' "${LOG_FILE}"; then
  echo "AFKArea disable marker missing." >&2
  cat "${LOG_FILE}" >&2
  exit 1
fi

if ! grep -Fq 'VertexCore disabled.' "${LOG_FILE}"; then
  echo "VertexCore disable marker missing." >&2
  cat "${LOG_FILE}" >&2
  exit 1
fi

if grep -Eiq \
  '(Error occurred while disabling (AFKArea|VertexCore|WorldGuard|WorldEdit)|Exception.*while disabling.*(AFKArea|VertexCore|WorldGuard|WorldEdit))' \
  "${LOG_FILE}"; then

  echo "Plugin shutdown error detected." >&2
  cat "${LOG_FILE}" >&2
  exit 1
fi

echo "Paper ${PAPER_VERSION} build ${PAPER_BUILD} runtime smoke passed (WorldGuard=${ENABLE_WORLDGUARD})."
