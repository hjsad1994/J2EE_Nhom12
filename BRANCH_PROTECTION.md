# Branch Protection Setup Guide

This guide explains how to configure branch protection rules in GitHub to require PR reviews and CI checks before merging.

## Prerequisites

- You must be a repository administrator
- Repository: `hjsad1994/J2EE_Nhom12`

## Step-by-Step Instructions

### 1. Navigate to Branch Protection Settings

1. Go to your repository on GitHub: https://github.com/hjsad1994/J2EE_Nhom12
2. Click **Settings** (top right of the repository)
3. In the left sidebar, click **Branches** under "Code and automation"

### 2. Add Branch Protection Rule

1. Click **Add branch protection rule** (or **Add rule**)
2. In "Branch name pattern", enter: `main`

### 3. Configure Protection Settings

Enable the following options:

#### Required Reviews

- [x] **Require a pull request before merging**
  - [x] **Require approvals**: Set to `1`
  - [x] **Dismiss stale pull request approvals when new commits are pushed**
  - [x] **Require review from Code Owners**

#### Required Status Checks

- [x] **Require status checks to pass before merging**
  - [x] **Require branches to be up to date before merging**
  - Search and add these status checks:
    - `Build & Test` (Backend CI)
    - `Lint, Typecheck, Test & Build` (Frontend CI)

#### Additional Settings (Recommended)

- [x] **Require conversation resolution before merging**
- [x] **Do not allow bypassing the above settings**
- [ ] **Allow force pushes** (keep unchecked)
- [ ] **Allow deletions** (keep unchecked)

### 4. Save Changes

Click **Create** (or **Save changes**) at the bottom of the page.

## Verification

After setup, verify the protection is working:

1. Create a test branch
2. Make a small change
3. Open a PR to `main`
4. Verify that:
   - CI checks run automatically
   - "Review required" badge appears
   - Merge button is disabled until review is approved and CI passes

## CODEOWNERS Integration

The `CODEOWNERS` file (`.github/CODEOWNERS`) automatically requests reviews from:

- `@hjsad1994` for all file changes

This integrates with the "Require review from Code Owners" setting above.

## CI Workflows

| Workflow    | File                                | Triggers          | Checks                              |
| ----------- | ----------------------------------- | ----------------- | ----------------------------------- |
| Backend CI  | `.github/workflows/backend-ci.yml`  | Push/PR to `main` | Compile, Test, Package (Maven)      |
| Frontend CI | `.github/workflows/frontend-ci.yml` | Push/PR to `main` | Lint, Typecheck, Test, Build (Vite) |

## Troubleshooting

### Status checks not appearing

- Wait for the first PR to trigger CI
- Status checks only appear after they've run at least once
- Check the **Actions** tab for workflow runs

### CODEOWNERS not working

- Ensure the file is at `.github/CODEOWNERS`
- Verify the username is correct: `@hjsad1994`
- Check that "Require review from Code Owners" is enabled in branch protection

### CI failing

- Check the **Actions** tab for detailed logs
- **Backend**: Ensure Java 25 (Temurin) is available and Maven wrapper has execute permission
- **Frontend**: Ensure `package-lock.json` is committed and dependencies are up to date
- Run CI commands locally to debug:

  ```bash
  # Backend
  cd nhom12 && ./mvnw -B verify

  # Frontend
  cd frontend && npm ci && npm run lint && npm run typecheck && npm run test && npm run build
  ```

### Stale CI runs

- Concurrent runs on the same branch are automatically cancelled
- Only the latest push triggers a new CI run

## Quick Reference

| Setting                | Value                                           |
| ---------------------- | ----------------------------------------------- |
| Protected branch       | `main`                                          |
| Required approvals     | 1                                               |
| Code owner             | `@hjsad1994`                                    |
| Required status checks | `Build & Test`, `Lint, Typecheck, Test & Build` |
| Force pushes           | Disabled                                        |
| Branch deletions       | Disabled                                        |
