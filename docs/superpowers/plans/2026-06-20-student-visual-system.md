# Student Visual System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the approved MiaomiaoC student-facing visual system across login, student workspace, chat, question practice, exam records, and VIP surfaces.

**Architecture:** Keep the existing Vue 3 routes, API modules, auth flow, WebSocket chat flow, and CodeMirror setup. Add a small shared student UI layer for brand, cards, metrics, rings, and quick actions, then migrate page visuals incrementally without changing backend contracts.

**Tech Stack:** Vue 3, Vite, Element Plus, Pinia, Vue Router, CodeMirror 6, existing CSS variables in `frontend/src/assets/main.css`.

---

## File Structure

- Create `frontend/src/components/student/BrandMark.vue`: reusable MiaomiaoC logo/title block.
- Create `frontend/src/components/student/SoftCard.vue`: simple visual card wrapper used by dashboard sections.
- Create `frontend/src/components/student/MetricCard.vue`: icon, label, value, trend, and color variant card.
- Create `frontend/src/components/student/ProgressRing.vue`: SVG progress ring for task, score, and pass-rate displays.
- Create `frontend/src/components/student/QuickActionCard.vue`: reusable shortcut card for practice, exams, wrong questions, and chat.
- Create `frontend/src/assets/student-system.css`: shared student visual tokens, utility classes, motion, responsive shell rules, and reduced-motion overrides.
- Modify `frontend/src/assets/main.css`: import `student-system.css` and align app-level color tokens with the approved palette.
- Modify `frontend/src/views/login/LoginPage.vue`: rebuild the login surface into a two-column brand + form experience while preserving script logic.
- Modify `frontend/src/views/exam/HomePage.vue`: apply the shared shell style and refresh personal center, exam records, online exams, question bank, wrong questions, and VIP surfaces inside the existing menu model.
- Modify `frontend/src/views/question/QuestionListPage.vue`: restyle question list filters, table, empty state, and action affordances.
- Modify `frontend/src/views/question/PracticeQuestionPage.vue`: restyle the practice/code split view, action bar, run result, syntax check, and responsive layout while preserving CodeMirror initialization.
- Modify `frontend/src/views/chat/ChatHub.vue`: align chat hub with the student visual system and add the reference-inspired right information rail using existing room data.

## Scope Notes

- Do not touch `backend/`.
- Do not change API request shapes.
- Do not add a chart library.
- Keep `.superpowers/` uncommitted.
- Keep unrelated existing worktree changes untouched.

---

### Task 1: Shared Student Visual Layer

**Files:**
- Create: `frontend/src/components/student/BrandMark.vue`
- Create: `frontend/src/components/student/SoftCard.vue`
- Create: `frontend/src/components/student/MetricCard.vue`
- Create: `frontend/src/components/student/ProgressRing.vue`
- Create: `frontend/src/components/student/QuickActionCard.vue`
- Create: `frontend/src/assets/student-system.css`
- Modify: `frontend/src/assets/main.css`

- [ ] **Step 1: Add shared component directory**

Run:
```bash
mkdir -p frontend/src/components/student
```

Expected: directory exists and contains no generated files yet.

- [ ] **Step 2: Create `BrandMark.vue`**

Implement a component with props `title`, `subtitle`, and `compact`. It renders a CSS C mark, product title, and optional subtitle. Use it in pages that need brand identity.

- [ ] **Step 3: Create `SoftCard.vue`**

Implement a wrapper component with props `as`, `padding`, and `interactive`. It renders a dynamic component with classes `soft-card`, `soft-card--interactive`, and `soft-card--pad-*`.

- [ ] **Step 4: Create `MetricCard.vue`**

Implement a component with props `icon`, `label`, `value`, `hint`, `trend`, and `tone`. It renders stable metric card markup and uses slots for icons when a caller needs an Element Plus icon.

- [ ] **Step 5: Create `ProgressRing.vue`**

Implement an SVG ring with props `value`, `max`, `size`, `stroke`, `label`, and `caption`. Clamp progress to 0-100 and expose CSS variables for stroke offset.

- [ ] **Step 6: Create `QuickActionCard.vue`**

Implement a button-like card with props `title`, `description`, `tone`, and `disabled`. Emit `select` on click and render an icon slot.

- [ ] **Step 7: Add shared CSS**

Create `frontend/src/assets/student-system.css` with:
- layout classes for `.student-shell`, `.student-sidebar`, `.student-main`, `.student-topbar`, `.student-grid`
- card classes for `.soft-card`, `.metric-card`, `.quick-action-card`
- brand mark classes
- motion keyframes for fade/slide, card lift, ring draw, and result slide-in
- `@media (prefers-reduced-motion: reduce)` overrides
- responsive breakpoints at 1180px, 900px, and 680px

- [ ] **Step 8: Import shared CSS**

Modify `frontend/src/assets/main.css` to import `./student-system.css` after `base.css`, and extend CSS variables for primary, gradient, success, warning/VIP, danger, surface, border, text, and shadows.

- [ ] **Step 9: Run build**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes with no missing component or CSS import errors.

- [ ] **Step 10: Commit Task 1**

Run:
```bash
git add frontend/src/components/student frontend/src/assets/main.css frontend/src/assets/student-system.css
git commit -m "feat: add student visual system components"
```

---

### Task 2: Login Page Redesign

**Files:**
- Modify: `frontend/src/views/login/LoginPage.vue`
- Use: `frontend/src/components/student/BrandMark.vue`

- [ ] **Step 1: Preserve script behavior**

Before editing, confirm that `handleLogin`, `getDefaultRoute`, `handleRegister`, and `handleForgotPassword` remain unchanged except imports needed for shared visual components.

- [ ] **Step 2: Replace the login template structure**

Use a two-column `.login-stage` layout:
- left side: `BrandMark`, hero headline `机试在线考试系统`, positioning copy, four feature rows, and floating C/code visual
- right side: login panel containing the existing `el-form`, username input, password input, role selector, login button, register link, and forgot-password link

- [ ] **Step 3: Replace scoped login styles**

Use a full-viewport light background with soft grid texture, floating visual elements, glass-like form panel, responsive collapse to single column under 900px, and role buttons that do not change size when selected.

- [ ] **Step 4: Verify login interactions**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes and no template binding errors.

- [ ] **Step 5: Commit Task 2**

Run:
```bash
git add frontend/src/views/login/LoginPage.vue
git commit -m "feat: redesign student login experience"
```

---

### Task 3: Student Workspace and Dashboard Refresh

**Files:**
- Modify: `frontend/src/views/exam/HomePage.vue`
- Use: shared student components from `frontend/src/components/student/`

- [ ] **Step 1: Add shared component imports**

Import `BrandMark`, `SoftCard`, `MetricCard`, `ProgressRing`, and `QuickActionCard` in `HomePage.vue`. Keep existing API imports and state declarations.

- [ ] **Step 2: Refresh shell markup**

Replace the outer sidebar/header wrappers with shared class structure:
- `.student-shell`
- `.student-sidebar`
- `.student-main`
- `.student-topbar`
- `.student-content`

Keep `currentMenu`, `handleMenuSelect`, `handleToExamRecord`, `handleToOnlineExam`, `handleToQuestionList`, `handleToVipUpgrade`, `handleToChat`, and `handleLogout` behavior intact.

- [ ] **Step 3: Build personal center dashboard**

For `currentMenu === '1'`, add:
- welcome hero
- profile information card using existing `userInfo`
- today task panel with `ProgressRing`
- quick actions that call existing navigation handlers
- metric grid using existing `stats`
- calendar/streak and achievement surfaces with honest default states

- [ ] **Step 4: Restyle exam records**

For `currentMenu === '2'`, keep existing record loading and detail dialog behavior. Add summary metric cards, table wrapper styling, score ring area for selected records, and status badges using existing fields from `examRecordList`.

- [ ] **Step 5: Restyle online exams and question bank**

For `currentMenu === '4'` and `currentMenu === '5'`, keep current load functions and route pushes. Convert cards/lists to soft card grids, keep VIP gating behavior, and keep wrong-question tab actions.

- [ ] **Step 6: Restyle VIP surface**

For `currentMenu === '6'`, align the existing VIP hero, redeem form, and plan cards with the approved reference. Preserve `loadVipPlans` and `handleRedeemVipKey`.

- [ ] **Step 7: Remove duplicated obsolete CSS**

Delete repeated `.wrong-container`, `.wrong-list-row`, `.question-bank`, `.stats-grid`, and legacy shell blocks that are replaced by shared classes. Keep selectors still referenced by templates.

- [ ] **Step 8: Run build**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes with no missing imports, no undefined template components, and no syntax errors in `HomePage.vue`.

- [ ] **Step 9: Commit Task 3**

Run:
```bash
git add frontend/src/views/exam/HomePage.vue
git commit -m "feat: refresh student workspace"
```

---

### Task 4: Question List and Practice Workspace

**Files:**
- Modify: `frontend/src/views/question/QuestionListPage.vue`
- Modify: `frontend/src/views/question/PracticeQuestionPage.vue`

- [ ] **Step 1: Refresh question list layout**

Wrap the question list page with the student visual background, soft header, filter chips, search input, table card, and empty state. Preserve `loadBankQuestionList`, filtering, pagination, and `handlePractice`.

- [ ] **Step 2: Verify question list route behavior**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes and `QuestionListPage.vue` has no template errors.

- [ ] **Step 3: Refresh practice page top and split layout**

In `PracticeQuestionPage.vue`, keep the existing script and CodeMirror setup. Restyle:
- top bar
- question statement panel
- sample blocks
- code editor chrome
- action buttons
- syntax check panel
- run result panel

- [ ] **Step 4: Add machine-exam motion**

Use CSS classes for result panel slide-in, syntax warning entrance, loading pulse on action groups, and stable responsive panel dimensions.

- [ ] **Step 5: Run build**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes and CodeMirror imports remain unchanged.

- [ ] **Step 6: Commit Task 4**

Run:
```bash
git add frontend/src/views/question/QuestionListPage.vue frontend/src/views/question/PracticeQuestionPage.vue
git commit -m "feat: polish question practice workspace"
```

---

### Task 5: Chat Center Visual Alignment

**Files:**
- Modify: `frontend/src/views/chat/ChatHub.vue`

- [ ] **Step 1: Preserve chat data flow**

Before editing, identify and keep existing functions for room loading, contact search, selecting rooms, sending messages, pinning, muting, creating rooms, and joining rooms.

- [ ] **Step 2: Refresh chat layout**

Convert the page to the approved three-zone layout:
- left rail: create/join/search, room groups, contacts
- center: active chat header, welcome banner, message stream, input toolbar
- right rail: room summary, member preview, announcement, and settings using existing room fields and static unavailable states where data is absent

- [ ] **Step 3: Restyle messages**

Use the shared palette for peer/self bubbles, role badges, unread badges, pinned/muted icons, empty state, and connection status. Keep `sendMessage` and Enter-to-send behavior unchanged.

- [ ] **Step 4: Add responsive fallback**

Hide the right rail first under 1180px, hide the left rail under 760px, and keep the active chat usable on small screens.

- [ ] **Step 5: Run build**

Run:
```bash
cd frontend && npm run build
```

Expected: build passes and chat template bindings remain valid.

- [ ] **Step 6: Commit Task 5**

Run:
```bash
git add frontend/src/views/chat/ChatHub.vue
git commit -m "feat: align chat center visual system"
```

---

### Task 6: Browser Verification and Final Cleanup

**Files:**
- Review: all modified frontend files

- [ ] **Step 1: Run production build**

Run:
```bash
cd frontend && npm run build
```

Expected: Vite build exits successfully.

- [ ] **Step 2: Start local dev server**

Run:
```bash
cd frontend && npm run dev -- --host 127.0.0.1
```

Expected: Vite prints a local URL, usually `http://127.0.0.1:5173/`.

- [ ] **Step 3: Inspect key desktop routes**

Open the dev server and verify:
- `/login`
- `/exam`
- `/chat`
- `/question/questionList`

Expected: page loads, no blank screens, visible design system alignment, and no obvious text overlap.

- [ ] **Step 4: Inspect mobile viewport**

Use a narrow viewport around 390px wide and verify:
- login collapses to one column
- student sidebar does not cover content
- dashboard cards become single column
- chat active conversation remains usable
- practice page panels stack without losing run/submit buttons

- [ ] **Step 5: Check repository status**

Run:
```bash
git status --short
```

Expected: only intentional frontend changes are committed or staged; `.superpowers/` remains untracked.

- [ ] **Step 6: Final commit if cleanup changes exist**

If verification required small CSS or markup fixes, commit them:
```bash
git add frontend
git commit -m "fix: refine student visual responsiveness"
```

---

## Self-Review

- Spec coverage: login, student home, chat, question practice, exam records, VIP, motion, responsive behavior, data honesty, accessibility, and build verification are each covered by a task.
- Placeholder scan: this plan contains no unresolved design decisions or deferred implementation markers.
- Type consistency: component names and file paths are consistent across tasks.
