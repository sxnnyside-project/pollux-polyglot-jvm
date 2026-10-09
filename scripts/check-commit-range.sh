#!/usr/bin/env bash
set -euo pipefail

BASE="${1:-HEAD~1}"
HEAD="${2:-HEAD}"

pattern='^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-zA-Z0-9_-]+\))?!?: .+$'

status=0
while IFS= read -r sha; do
  msg="$(git log -1 --format=%s "$sha")"
  if ! [[ "$msg" =~ $pattern ]]; then
    echo "ERROR: commit $sha does not match Conventional Commits:"
    echo "  $msg"
    status=1
  fi
done < <(git rev-list "$BASE..$HEAD")

exit $status
