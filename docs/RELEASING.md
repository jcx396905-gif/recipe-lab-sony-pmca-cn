# Releasing

The version is not a decision any more — it is computed from the commit messages by
[semantic-release](https://semantic-release.gitbook.io/). One button does the rest.

A release is a **tag on `main`**. There is no release branch and nothing gets merged: the
workflow ships whatever is on `main` when you run it. Releasing is a decision about *timing*.

## The button

**Actions → create-release → Run workflow**, or:

```bash
gh workflow run create-release.yml                    # release
gh workflow run create-release.yml -f dry_run=true    # just report what would ship
```

| input | meaning |
|---|---|
| `dry_run` | Report the version that would be released and change nothing. |
| `allow_failing_checks` | Release even though the last `main` build failed. |

It refuses if the last build failed (that build runs the unit tests first), or if no commit
since the last tag carries a releasable type. Then it runs the unit tests once more, and
semantic-release decides the version, writes `AndroidManifest.xml` via `tools/bump-version.sh`,
builds the APK, tags, publishes the release and commits the manifest bump back to `main`.
Finally the rolling `dev` prerelease is rebuilt so its tag follows the new tip.

## What decides the version

| commit type | bump |
|---|---|
| `fix:` `perf:` `refactor:` | patch |
| `feat:` | minor |
| any type with `!`, or a `BREAKING CHANGE:` footer | major |
| `docs:` `chore:` `ci:` `test:` `build:` | nothing |

**A squash merge leaves only the PR title**, so the PR title is what semantic-release
reads. A PR titled `chore:` contributes nothing releasable however large its diff — which
is why `pr-title` is a check. Get the type right on the PR, not on the commits inside it.

**Still verify on a camera.** A green build says it compiles. Install the published APK
over the previous version — it must succeed *without uninstalling*, which is what proves
the signing key is unchanged.

## Doing it by hand

If the workflow is broken:

```bash
git switch main && git pull
./tools/test.sh
GITHUB_TOKEN=$(gh auth token) npx semantic-release
```

`semantic-release` reads the branch from `GITHUB_REF` when it detects GitHub Actions. Running
it locally there is no such variable, so it uses the checked-out branch — which is why the
command above works as written, and why the workflow has to `export GITHUB_REF=refs/heads/main`
before its own invocation.

## Hotfix

There is no hotfix branch. Cut an ordinary `fix/` branch off `main`, PR it with a `fix:` title,
squash-merge it, and run **create-release**. The `fix:` title is what makes it a patch release.

## If something goes wrong

- **Nothing was released.** No commit since the last tag carried a releasable type — most often a PR
  titled `chore:` or `docs:`. Land a `fix:` or `feat:` PR, or merge with a corrected title.
- **The manifest and the tag disagree.** `version-consistency` fails if the manifest falls behind the last
  release, which means the `chore(release):` commit did not land. Re-run the release; semantic-release is
  idempotent about a version it has already published.
- **A release was published with a bad APK.** Fix forward with a patch release. Do not move a tag — the
  ruleset blocks it, and anyone who already downloaded has the old bytes.
