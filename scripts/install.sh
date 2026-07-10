#!/usr/bin/env bash
#
# Build the Omarchy Theme Sync plugin zip and install it into one or more
# locally installed JetBrains IDEs.
#
# Usage:
#   scripts/install.sh                 # interactive: pick an IDE from a menu
#   scripts/install.sh all             # install into every detected IDE
#   scripts/install.sh IntelliJIdea    # install into all IntelliJ IDEA versions
#   scripts/install.sh Rider2026.1     # install into one specific IDE
#   scripts/install.sh --no-build ...  # skip the Gradle build, reuse existing zip
#
# After installing, fully restart the target IDE(s) to load the plugin.

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$PROJECT_DIR/build/distributions"
# JetBrains stores per-IDE plugins here on Linux; config dirs mirror the names.
PLUGINS_ROOT="$HOME/.local/share/JetBrains"
CONFIG_ROOT="$HOME/.config/JetBrains"
PLUGIN_DIR_NAME="omarchy-theme-sync"

BUILD=1
FILTERS=()
for arg in "$@"; do
  case "$arg" in
    --no-build) BUILD=0 ;;
    -h|--help) grep '^#' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) FILTERS+=("$arg") ;;
  esac
done

# --- Build ------------------------------------------------------------------
if [[ $BUILD -eq 1 ]]; then
  echo "==> Building plugin zip (./gradlew buildPlugin)"
  JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk}" "$PROJECT_DIR/gradlew" \
    -p "$PROJECT_DIR" buildPlugin --console=plain
fi

ZIP="$(ls -t "$DIST_DIR"/*.zip 2>/dev/null | head -1 || true)"
if [[ -z "$ZIP" ]]; then
  echo "ERROR: no plugin zip found in $DIST_DIR (build failed or --no-build with no prior build?)" >&2
  exit 1
fi
echo "==> Plugin zip: $ZIP"

# --- Discover installed IDEs ------------------------------------------------
# The authoritative list of installed IDEs is the set of config dirs; the
# plugins dir uses the same <Product><Version> name under PLUGINS_ROOT.
mapfile -t ALL_IDES < <(
  find "$CONFIG_ROOT" -maxdepth 1 -mindepth 1 -type d -printf '%f\n' 2>/dev/null |
    grep -E '^[A-Za-z]+[0-9]{4}\.[0-9]+$' | sort
)
if [[ ${#ALL_IDES[@]} -eq 0 ]]; then
  echo "ERROR: no JetBrains IDEs found under $CONFIG_ROOT" >&2
  exit 1
fi

# --- Select targets ---------------------------------------------------------
TARGETS=()
if [[ ${#FILTERS[@]} -eq 0 ]]; then
  echo "==> Detected IDEs:"
  select choice in "${ALL_IDES[@]}" "all"; do
    if [[ "$choice" == "all" ]]; then TARGETS=("${ALL_IDES[@]}"); break
    elif [[ -n "${choice:-}" ]]; then TARGETS=("$choice"); break
    else echo "Invalid selection."; fi
  done
elif [[ " ${FILTERS[*]} " == *" all "* ]]; then
  TARGETS=("${ALL_IDES[@]}")
else
  for f in "${FILTERS[@]}"; do
    for ide in "${ALL_IDES[@]}"; do
      [[ "$ide" == "$f" || "$ide" == "$f"* ]] && TARGETS+=("$ide")
    done
  done
fi

if [[ ${#TARGETS[@]} -eq 0 ]]; then
  echo "ERROR: no installed IDE matched: ${FILTERS[*]}" >&2
  echo "Available: ${ALL_IDES[*]}" >&2
  exit 1
fi

# --- Install ----------------------------------------------------------------
for ide in "${TARGETS[@]}"; do
  dest="$PLUGINS_ROOT/$ide"
  mkdir -p "$dest"
  rm -rf "${dest:?}/$PLUGIN_DIR_NAME"
  unzip -q -o "$ZIP" -d "$dest"
  echo "==> Installed into $ide  ($dest/$PLUGIN_DIR_NAME)"
done

echo
echo "Done. Restart the IDE(s) above to load Omarchy Theme Sync."
echo "In each IDE, verify under: Settings > Plugins (Installed) and"
echo "Settings > Appearance & Behavior > Omarchy Theme Sync."
