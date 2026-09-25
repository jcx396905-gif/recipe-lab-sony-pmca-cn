#!/usr/bin/env bash
# Conventional Commits gate. Zero dependencies — there is no Node in this repo.
#
#   tools/check-commit-msg.sh "feat(ui): add a thing"      check one subject
#   tools/check-commit-msg.sh --range BASE..HEAD           check every subject in a range
#
# Merge and Revert subjects, and GitHub's "(#123)" squash suffix, are accepted.
#
# In --range mode it also warns when the branch name carries an issue number that a commit
# does not reference. That is a warning, not a failure: release commits and back-merges
# legitimately have no issue.
set -uo pipefail

TYPES='feat|fix|docs|refactor|perf|test|build|ci|chore|revert'
SCOPES='ui|input|browser|tools|recipes|build|ci|docs|deps|release'
RE="^($TYPES)(\(($SCOPES)\))?!?: .{1,72}$"

fail=0

check() {
  local subject="$1" source="$2"
  case "$subject" in
    Merge\ *|Revert\ *) return 0 ;;
  esac
  # strip GitHub's squash-merge PR suffix before matching
  local bare
  bare="$(printf '%s' "$subject" | sed -E 's/ \(#[0-9]+\)$//')"
  if ! printf '%s' "$bare" | grep -qE "$RE"; then
    echo "✗ $source: $subject" >&2
    fail=1
  else
    echo "✓ $source: $subject"
  fi
}

if [ "${1:-}" = "--range" ]; then
  range="${2:?usage: $0 --range BASE..HEAD}"
  while IFS= read -r s; do [ -n "$s" ] && check "$s" "commit"; done < <(git log --format='%s' "$range")

  # Every commit on a work branch should say which issue it belongs to. The branch name is
  # where the issue number lives, so use it as the expectation.
  BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
  ISSUE="$(printf '%s' "$BRANCH" | sed -nE 's|^[a-z]+/([0-9]+)-.*|\1|p')"
  if [ -n "$ISSUE" ]; then
    while IFS= read -r sha; do
      [ -n "$sha" ] || continue
      msg="$(git log -1 --format='%s%n%b' "$sha")"
      case "$msg" in
        Merge\ *|Revert\ *) continue ;;
      esac
      if ! printf '%s' "$msg" | grep -qE "#$ISSUE\b"; then
        echo "! $(git log -1 --format='%h %s' "$sha") does not reference #$ISSUE" >&2
        echo "    add a footer:  Closes #$ISSUE   (or  Refs #$ISSUE  if it does not finish the issue)" >&2
      fi
    done < <(git log --format='%H' "$range")
  fi
else
  check "${1:?usage: $0 \"<subject>\" | --range BASE..HEAD}" "subject"
fi

if [ "$fail" -ne 0 ]; then
  cat >&2 <<MSG

Expected:  type(scope): subject
  type   $TYPES
  scope  $SCOPES  (optional)
  subject  <= 72 chars, imperative, no trailing period

Link the issue with a "Closes #123" footer, not in the subject. See docs/CONTRIBUTING.md.
MSG
fi
exit "$fail"
