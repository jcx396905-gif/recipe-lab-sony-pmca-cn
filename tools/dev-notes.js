#!/usr/bin/env node
// Prints the release notes for a development build, e.g.
//
//   node tools/dev-notes.js 1.2.0-dev.9
//
// The notes cover every commit since the last v* tag and are produced by the same
// @semantic-release/release-notes-generator and preset config that write the notes on a
// real release (see .releaserc.js), so a dev prerelease reads exactly like a release.
//
// Needs: npm ci. Used by .github/workflows/dev-build.yml.
import { execFileSync } from 'node:child_process';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { generateNotes } from '@semantic-release/release-notes-generator';
import config from '../.releaserc.js';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const git = (...args) => execFileSync('git', args, { cwd: ROOT, encoding: 'utf8' }).trim();
// git describe exits non-zero when nothing matches, which is the first-release case.
const gitOrEmpty = (...args) => {
  try {
    return execFileSync('git', args, { cwd: ROOT, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
  } catch {
    return '';
  }
};
const fail = (message) => {
  console.error(`dev-notes: ${message}`);
  process.exit(1);
};

const version = process.argv[2] || process.env.VERSION_NAME;
if (!version) fail('no version given (argv[2] or VERSION_NAME), e.g. 1.2.0-dev.9');

// The rolling `dev` tag is not a release, hence the v* match.
const from = gitOrEmpty('describe', '--tags', '--abbrev=0', '--match', 'v*');
const to = git('rev-parse', 'HEAD');

const generator = config.plugins.find(
  (plugin) => (Array.isArray(plugin) ? plugin[0] : plugin) === '@semantic-release/release-notes-generator'
);
if (!generator) fail('.releaserc.js has no @semantic-release/release-notes-generator plugin');

// %x1f separates the fields of a commit, %x1e the commits.
const commits = git('log', '--format=%H%x1f%B%x1f%cI%x1e', ...(from ? [`${from}..${to}`] : [to]))
  .split('\x1e')
  .map((record) => record.trim())
  .filter(Boolean)
  .map((record) => {
    const [hash, message, committerDate] = record.split('\x1f');
    return { hash, message: message.trim(), gitTags: '', committerDate: new Date(committerDate) };
  });

const notes = await generateNotes(Array.isArray(generator) ? generator[1] : {}, {
  cwd: ROOT,
  env: process.env,
  options: { repositoryUrl: git('remote', 'get-url', 'origin') },
  // No tag yet for what is being built: the compare link points at the commit instead.
  lastRelease: from ? { gitTag: from, version: from.replace(/^v/, '') } : {},
  nextRelease: { version, gitTag: to, channel: 'dev' },
  commits,
  logger: { log: () => {}, error: () => {} },
});

process.stdout.write(`${notes.trim()}\n`);
