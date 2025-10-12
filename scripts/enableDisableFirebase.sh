#!/bin/bash

ENABLE_FIREBASE=${1:-true} # Default to true if not provided

# Handle Firebase plugin lines
if [ "$ENABLE_FIREBASE" = false ]; then
  # Comment out the lines if not already commented
  sed -i '' '/^[[:space:]]*id("com.google.gms.google-services")/s/^/\/\/ /' app/build.gradle.kts
  sed -i '' '/^[[:space:]]*id("com.google.firebase.crashlytics")/s/^/\/\/ /' app/build.gradle.kts
  # Remove duplicate comment markers if any
  sed -i '' 's/^\(\/\/ \)*\/\//\/\//g' app/build.gradle.kts
else
  # Uncomment the lines if commented
  sed -i '' 's/^[[:space:]]*\/\/[[:space:]]*\(id("com.google.gms.google-services")\)/\1/' app/build.gradle.kts
  sed -i '' 's/^[[:space:]]*\/\/[[:space:]]*\(id("com.google.firebase.crashlytics")\)/\1/' app/build.gradle.kts
fi

# Usage:
#   bash ./scripts/enableDisableFirebase.sh false   # to comment out Firebase plugins
#   bash ./scripts/enableDisableFirebase.sh true    # to uncomment Firebase plugins
# Or make the script executable and run directly:
#   chmod +x ./scripts/enableDisableFirebase.sh
#   ./scripts/enableDisableFirebase.sh false
