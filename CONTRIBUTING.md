# Contributing

This repository is a shared Program 3 codebase for group collaboration.

## Ground Rules

- Keep behavior correct first; optimize second.
- Do not commit generated `.class` files.
- Keep command-line interfaces stable unless the group agrees on a change.
- Document any meaningful behavior or performance changes in `README.md`.

## Local Workflow

1. Pull latest `main`.
2. Compile:
   - `javac com/tryright/*.java`
3. Run at least one correctness test from `test/`.
4. Open a commit with a clear message describing why the change was made.

## Suggested Review Checklist

- Correctness is unchanged or improved.
- Error handling still matches assignment expectations.
- No regressions in thread/process variants.
- Code style and comments remain clear and consistent.
