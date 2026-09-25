# Contributing

How work moves through this repo. CI enforces most of it, so reading this saves you a red build.

- [Branches](#branches)
- [Commits](#commits)
- [Pull requests](#pull-requests)
- [Issues and milestones](#issues-and-milestones)
- [Testing](#testing)
- [Releases](#releases)

## Branches

| Branch | Role |
|---|---|
| `main` | Default branch, and the only long-lived one. Integration *and* releases. Every push builds a `-dev.N` APK attached to the rolling [`dev` prerelease](https://github.com/voxivoid/recipe-lab-sony-pmca/releases/tag/dev), whose notes list every change since the last release. Never commit here directly — go through a PR. |
| work branches | One per change, cut from `main`, squash-merged back. |

```
main   ──●──●──●──●──●──●──●──●──●──●──●──   every push builds a dev APK
         \      /   \     /        ▲
work   feat/12-…   fix/19-…        └── v1.2.0, a tag on main — not a branch
```

A release is a **tag on `main`**, not a branch and not a merge. `main` therefore carries
unreleased work between releases, and `create-release` ships whatever is on it at the moment
you run it.

Naming — the prefix is the commit type, so the branch says what kind of change it carries:

```
feat/<issue>-<slug>        feat/123-brand-jump-top-dial
fix/<issue>-<slug>         fix/131-wb-finetune-sign
docs/ refactor/ chore/ build/ ci/ perf/ test/   same shape
```

An issue number is optional; drop it and use `<type>/<slug>` when there is no issue. There is no
`release/*` branch and no `hotfix/*` branch — a hotfix is an ordinary `fix/` branch, released by
running **create-release** once it lands.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). Checked by the `commit-lint` job.

```
feat(browser): jump to a brand with the top dial

The picker had no fast path past 77 recipes.

Closes #123
```

**Types:** `feat` `fix` `docs` `refactor` `perf` `test` `build` `ci` `chore` `revert`.
A breaking change takes `!` after the scope and a `BREAKING CHANGE:` footer.

**Scopes** (optional — plenty of `docs:` commits carry none):

| scope | covers |
|---|---|
| `ui` | panel, chips, badges, toast, layout |
| `input` | key handling, wheel and dial, scan codes |
| `browser` | the brand/recipe picker |
| `recipes` | the recipe table itself |
| `tools` | the snapshot/diff tool, `tools/` scripts |
| `build` | `build.cmd`, `build.sh`, NDK, toolchain |
| `ci` | `.github/workflows` |
| `docs` | documentation |
| `deps` | the `jni/platform` submodule |
| `release` | `chore(release): x.y.z` only |

**Issue linkage — every commit carries its issue.** Put it in a footer, never in the subject:

- `Closes #123` when the commit finishes the issue. On a branch with several commits, the last one closes.
- `Refs #123` when it is one step of several.

The number lives in the branch name, so you never have to look it up:

```bash
git branch --show-current | sed -nE 's|^[a-z]+/([0-9]+)-.*|\1|p'
```

`commit-lint` warns (it does not fail) about any commit on the branch that omits the reference — release
commits legitimately have none.

Keep it out of the subject: GitHub appends the **PR** number there automatically on squash merge, so the
commit on `main` reads `feat(browser): jump to a brand with the top dial (#45)`. Two bare `#N` in one
subject would be ambiguous, since issues and PRs share a number space.

## Pull requests

Working in Claude Code? `/commit-and-pr` walks the whole flow below — branch, gates, commit
message, push, PR — and stops before merging.

**The PR title becomes the commit message — and the release.** A squash merge leaves only the title, so it
is the string semantic-release reads to decide the next version. A PR titled `chore:` releases nothing
however large its diff; `fix:` makes a patch, `feat:` a minor, `!` a major. The `pr-title` check exists for
exactly this reason.

- work branch → `main`: **squash merge**. One commit per change; your WIP never surfaces.

Checks on a PR: `build`, `test`, `version-consistency`, `commit-lint`, `pr-title`.

> **They are not enforced as merge gates, and that is deliberate.** `main` is protected only against
> deletion and non-fast-forward pushes. It cannot require pull requests or passing checks, because
> semantic-release pushes the `chore(release): X.Y.Z [skip ci]` bump straight to `main` — a rule
> requiring a PR would reject it, and `[skip ci]` means no status check can ever pass for it. So the
> checks tell you whether a PR is safe; they do not stop you merging it anyway. Read them.

Rebase a PR that has fallen behind, so its checks reflect the tip and the squashed commit stays honest:

```sh
git fetch origin
git rebase origin/main
git push --force-with-lease
```

GitHub's **Update branch** button does the same job by merging `main` in; it is fine when a rebase
would be painful, since the merge only ever squashes down to one commit anyway.

> [.github/CODEOWNERS](../.github/CODEOWNERS) still marks who owns what, but with no `pull_request`
> rule on `main` a review is a convention rather than a gate. GitHub does not let you approve your own
> pull request anyway, so while `@voxivoid` is the only code owner there is nobody to enforce it against.
> Adding a second code owner, and the rule to go with it, is what would make review bite.

## Issues and milestones

- An issue is optional. Open one when the work benefits from being tracked; never open one just to have a number.
- **Milestones are versions** (`v1.1.0`, `v1.2.0`) plus a permanent `Backlog`. The release workflow closes a
  milestone when its tag ships.
- Labels: `type: …` mirrors the commit type, `scope: …` mirrors the commit scope, plus `priority: p1|p2|p3`
  and `status: blocked|needs-triage`.
- **`needs-on-camera-verification`** is the important one. Nothing about recipes, settings-store IDs or live
  preview can be validated by CI. A green build is not evidence the change works.

## Testing

`./tools/test.sh` runs the unit tests — the `test` CI job, and the first step of every `main` build
and of a release — against a bare JDK 17 in a few seconds. They cover
what the app decides without the camera: the recipe table, how each value is encoded in the settings store, the
bytes ENTER writes, the live-preview parameters, chip navigation and the overlay text
([details](DEVELOPMENT.md#unit-tests)). Logic of that kind goes into `Params.java`, `Recipes.java` or `Favourites.java` with a
test next to it; `MainActivity` and the views cannot be tested off the camera.

### On the camera

A green build only proves it compiles, and a green `test` only that the app would write the bytes it means to
— not that the camera means the same thing by them. Before asking for a merge:

1. Grab the APK — your PR's workflow artifact, or `RecipeLab-dev.apk` from the `dev` prerelease.
2. Install it: `pmca-console.py install -d native -f RecipeLab.apk` (or PMCA-GUI → Install App).
3. Exercise the change, then **power-cycle the camera** and confirm the setting survived. The store is
   written through the backup driver; a look that vanishes after a power cycle was never really stored.
4. Say in the PR what you tested and on which body/firmware.

Never commit an APK or a keystore. Both are gitignored; releases carry the binaries.

## Releases

**Actions → create-release → Run workflow** (`-f dry_run=true` to just see what would ship). semantic-release
reads the commits on `main`, decides the version, builds, tags `main`'s tip and publishes, then commits the
manifest bump back. Nothing is merged and no branch moves. Nobody picks a version number.
Details: **[RELEASING.md](RELEASING.md)**.

Handy aliases:

```bash
git config alias.releases 'log --first-parent --oneline main'
git config alias.since-release '!git log $(git describe --tags --abbrev=0)..HEAD --no-merges --oneline'
```
