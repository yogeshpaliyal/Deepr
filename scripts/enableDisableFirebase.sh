#!/bin/bash

# Cross-platform sed -i handling
sedi() {
  # Usage: sedi 's/foo/bar/' filename
  if [[ "$(uname)" == "Darwin" ]]; then
    sed -i '' "$@"
  else
    sed -i "$@"
  fi
}

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
GRADLE_FILE="$PROJECT_ROOT/app/build.gradle.kts"

ENABLE_FIREBASE=${1:-true} # Default to true if not provided

# Check if the gradle file exists
if [ ! -f "$GRADLE_FILE" ]; then
  echo "Error: $GRADLE_FILE not found. Please run this script from the project root or ensure the file exists."
  exit 1
fi

# Handle Firebase plugin lines
if [ "$ENABLE_FIREBASE" = false ]; then
  # Comment out the lines if not already commented
  sedi '/^[[:space:]]*id("com.google.gms.google-services")/s/^/\/\/ /' "$GRADLE_FILE"
  sedi '/^[[:space:]]*id("com.google.firebase.crashlytics")/s/^/\/\/ /' "$GRADLE_FILE"
  # Remove duplicate comment markers if any
  sedi 's/^\(\/\/ \)*\/\//\/\//g' "$GRADLE_FILE"
else
  # Uncomment the lines if commented
  sedi 's/^[[:space:]]*\/\/[[:space:]]*\(id("com.google.gms.google-services")\)/\1/' "$GRADLE_FILE"
  sedi 's/^[[:space:]]*\/\/[[:space:]]*\(id("com.google.firebase.crashlytics")\)/\1/' "$GRADLE_FILE"
fi

# Usage:
#   bash ./scripts/enableDisableFirebase.sh false   # to comment out Firebase plugins
#   bash ./scripts/enableDisableFirebase.sh true    # to uncomment Firebase plugins
# Or make the script executable and run directly:
#   chmod +x ./scripts/enableDisableFirebase.sh
#   ./scripts/enableDisableFirebase.sh false
