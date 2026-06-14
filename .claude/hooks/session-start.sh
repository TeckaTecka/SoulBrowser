#!/bin/bash
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Validate all Android string XML files are well-formed
echo "Validating Language XML files..."
find "${CLAUDE_PROJECT_DIR}/Language" -name "strings.xml" -print0 | \
  xargs -0 xmllint --noout 2>&1 && echo "All XML files valid."
