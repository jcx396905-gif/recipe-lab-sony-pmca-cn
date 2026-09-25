# Documentation

Guidelines for working on Recipe Lab. The [main README](../README.md) is for people who
just want to use the app on a camera.

| | |
|---|---|
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | How to develop: branch model, commit format, PR flow, issues and milestones, and how to test a change on the camera. Start here. |
| **[RELEASING.md](RELEASING.md)** | How to release: the create-release button, what decides the version number, hotfixes, and what to do when something goes wrong. |
| **[SAMPLES.md](SAMPLES.md)** | Every recipe shot on the same subject at the same exposure, straight out of the camera — what the looks actually are, and which scene traits the frames do not cover. |
| **[DEVELOPMENT.md](DEVELOPMENT.md)** | The reverse-engineering reference: source layout, the settings-store ID map, live-preview parameters, key scan codes, how to build, and the versioning formula. |

`CLAUDE.md` stays in the repository root on purpose — it is configuration read by Claude
Code from the working directory, not documentation, so moving it here would stop it being
picked up.

## The short version

```
branch off main  ──▶  PR  ──▶  squash merge
                       │
                       └── the PR title IS the commit, and
                           decides the next version number

push to main  ──▶  dev-build  ──▶  rolling `dev` prerelease APK

create-release (manual)  ──▶  semantic-release on main
                              version, build, tag main's tip, publish
```

Nobody picks a version number: `fix:` makes a patch, `feat:` a minor, `!` a major, and
`chore:`/`docs:` release nothing at all.
