# MiaomiaoC Student Visual System Design

## Context

MiaomiaoC is a school-oriented online computer-based exam system for C language practice, mock exams, exam records, chat, and VIP learning features. The current Vue 3 frontend already has the required student workflows, but the visual style is inconsistent across login, dashboard, chat, practice, exam record, and VIP surfaces.

The selected direction is to unify the student-facing frontend into a light, technology-inspired study workspace based on the provided reference images. The system should feel polished and modern while remaining practical for repeated school exam preparation.

## Goals

- Create one cohesive student visual system across login, student home, chat center, question practice, exam records, and VIP pages.
- Keep the product positioning clear: `机试在线考试系统`, a school C language online practice and mock exam platform.
- Preserve existing frontend business logic, routes, authentication, API modules, WebSocket chat behavior, and CodeMirror integration.
- Add purposeful dynamic effects: light motion for study dashboard pages and stronger code/run feedback for machine-exam pages.
- Improve responsive behavior for desktop, tablet, and mobile without allowing text overlap or container overflow.

## Non-Goals

- No backend API changes.
- No authentication or permission model changes.
- No new large UI framework.
- No invented backend capabilities. Missing API data should appear as clear default, empty, or pending states.
- No redesign of teacher or admin workflows unless shared styles naturally improve them.

## Selected Approach

Implement a unified student visual system.

Login, student home, chat center, question practice, exam records, and VIP pages will share a light blue-white workspace language, soft card surfaces, restrained blue/purple accents, and consistent navigation. Dashboard-oriented pages will use lightweight motion. Practice and exam pages will preserve a serious exam feel and add stronger code execution, submit, and result feedback.

## Information Architecture

### Login

Use a two-column hero layout.

- Left side: brand mark, `MiaomiaoC 机试在线考试系统`, short positioning text, feature highlights for question bank, intelligent judging, data analysis, and online communication.
- Center visual: floating C mark, code symbols, soft rings, and small animated particles.
- Right side: glass-like login panel with username, password, role selector, login, register, and forgot-password actions.

Existing login validation and role-based redirect behavior stay unchanged.

### Student Home

Use the main student workspace.

- Fixed left navigation with brand, user avatar, role/VIP badge, and student routes.
- Top bar with search, user status, and notification/settings entry points where appropriate.
- Welcome hero with school exam platform language and a dynamic C/code visual.
- Today task panel with progress ring and checklist-style learning tasks.
- Student profile summary using available user data.
- Quick actions for continuing exams, question practice, wrong questions, and chat.
- Study statistics cards for exam count, pass rate, notes, and completed questions.
- Calendar/streak and recent achievement sections when data is available or can be represented as default states.

### Chat Center

Use a three-column layout inspired by the reference chat page.

- Left: room/group list, recent contacts, search, unread badges, pinned/muted room indicators.
- Center: active group/private chat, header actions, welcome banner, message stream, role badges, attachment/action bar, and send control.
- Right: group information, members, announcement, and settings.

Existing WebSocket and room/contact logic stay in place. The redesign should mainly restructure presentation and interaction states.

### Question Practice and Code Page

Use a split machine-exam workspace.

- Left: question title, difficulty/tags, statement, input/output format, samples, hints, and test case list.
- Right: CodeMirror editor, language selector where applicable, run/submit/reset/format actions, result panel, metrics, related knowledge points, and hints.

Practice/exam motion should be more technical: running-state pulse, output panel slide-in, test case rows transitioning from pending to passed/failed, and animated pass-rate/metric changes.

### Exam Records

Use a data dashboard layout.

- Top summary metrics: total exam count, average score, best score, pass rate.
- Filterable exam record table with status badges and detail actions.
- Detail section for the selected record with score ring, correctness, ranking/status if available, and exam metadata.
- Lightweight score trend chart implemented with CSS/SVG or existing simple markup rather than adding a chart dependency.

### VIP Page

Use a membership center layout.

- Hero section for VIP benefits with crown/C visual treatment.
- Benefit cards for question bank, VIP exams, group privileges, intelligent analysis, and priority support.
- Plan cards for monthly, seasonal, and annual membership using existing or current page data.
- VIP key exchange panel with strong button feedback.
- FAQ accordion and feature comparison table.

## Visual System

### Color

- Base: white, cool off-white, and pale blue backgrounds.
- Primary: saturated blue for main actions and active navigation.
- Secondary: limited blue-purple gradient for hero visuals, selected cards, and major call-to-action buttons.
- Success: green for passed tests, completed tasks, and correct answers.
- Warning/VIP: warm gold/orange for VIP and membership emphasis.
- Danger: red for failed tests, violations, and destructive actions.

The palette should avoid becoming a one-note purple/blue gradient. Blue and purple are accents; content surfaces remain mostly white and pale blue.

### Typography

- Use the existing system font stack for Chinese readability and performance.
- Increase hierarchy through size, weight, spacing, and color rather than decorative type.
- Keep large display text only in login and workspace hero sections.
- Use compact labels and table text in operational panels.

### Components

- Cards use restrained radius and clear borders.
- Repeated dashboards use consistent metric cards, progress rings, tags, quick action cards, and status badges.
- Navigation items have icon, label, active state, hover state, and keyboard focus state.
- Buttons include icons for concrete actions where an Element Plus icon exists.
- Tables, forms, and dialogs keep Element Plus behavior but adopt shared visual tokens.

## Motion Design

The approved motion direction is balanced.

Dashboard pages:

- Page fade/slide on entry.
- Cards lift slightly on hover.
- Progress rings draw in on mount.
- Metric values count up on first render when practical.
- Quick action cards use short transform/opacity transitions.

Practice and exam pages:

- Code editor action buttons show distinct loading/submitting states.
- Run result panels slide/fade into view.
- Test case rows animate into passed/failed state.
- Success/failure badges pulse briefly, then settle.
- Keep all animations under user control through `prefers-reduced-motion`.

## Component Boundaries

Create or refine shared frontend pieces where they reduce duplication:

- `StudentShell`: student sidebar, top bar, responsive layout, user area, route navigation.
- `BrandMark`: shared MiaomiaoC mark and product title.
- `SoftCard`: shared card surface styling.
- `MetricCard`: icon, label, value, trend, and loading/default state.
- `ProgressRing`: reusable SVG/CSS ring for tasks, pass rate, and scores.
- `QuickActionCard`: shortcut entry with icon, title, description, and action.
- `ExamCodePanel` and `ResultPanel`: reusable visual pieces for code running and result feedback where current pages allow it.

Existing page files may keep local business logic, but visual structure should move into shared components when a component has at least two real consumers.

## Data and State Handling

- Prefer existing API modules and store data.
- Show unavailable values as `--`, `暂无数据`, or clearly labeled default states.
- Do not imply real analytics if the backend does not provide them.
- Loading states should be visible for async pages.
- Empty states should provide a clear next action, such as starting practice or returning to the question list.
- Error states should preserve existing Element Plus message behavior and add inline feedback where useful.

## Responsive Behavior

- Desktop: fixed sidebar, top bar, multi-column dashboard grids, and right-side detail panels.
- Tablet: reduce secondary right panels, use two-column grids, and keep primary actions visible.
- Mobile: collapse sidebar into a compact top/drawer pattern, make content single-column, and keep code/practice actions reachable.
- Fixed-format elements such as progress rings, icon buttons, and code panels need stable dimensions to prevent layout jumps.
- Text must wrap or clamp within cards and buttons without overlap.

## Accessibility and Usability

- Preserve semantic buttons, inputs, forms, and router navigation.
- Ensure visible focus states for buttons, nav items, inputs, and selectable cards.
- Maintain sufficient contrast for labels, statuses, and disabled states.
- Avoid motion that blocks input or hides important exam feedback.
- Support `prefers-reduced-motion`.

## Verification Plan

After implementation:

- Run `npm run build` in `frontend/`.
- Start the Vite dev server and inspect key routes in a browser.
- Verify desktop and mobile layouts for:
  - `/login`
  - `/exam`
  - `/chat`
  - `/question/questionList`
  - `/question/practiceQuestion/:id`
  - exam record surface inside the student workflow
  - VIP surface inside the student workflow
- Check that login, navigation, role selection, chat send flow, question run/submit controls, and VIP key exchange remain interactive.
- Confirm text does not overlap, dynamic effects render, and reduced-motion rules disable nonessential animation.

## Risks and Mitigations

- Some referenced pages may currently be embedded inside a single large student home file. Mitigation: refactor only enough to support the visual system and avoid unrelated business rewrites.
- API coverage may not match all reference widgets. Mitigation: use honest default/empty states instead of fake live analytics.
- The existing worktree may contain unrelated changes. Mitigation: keep implementation scoped to `frontend/` and stage only files touched for this visual system.
- CodeMirror styling can regress editor usability. Mitigation: preserve editor initialization and only adjust surrounding chrome or CodeMirror theme rules carefully.
