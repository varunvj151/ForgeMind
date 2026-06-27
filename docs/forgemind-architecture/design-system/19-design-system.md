# ForgeMind — Design System

## Table of Contents
1. Overview
2. Typography
3. Spacing
4. Colors
5. Icons
6. Dark Mode
7. Accessibility
8. Animations
9. Component Standards
10. Implementation Notes
11. Future Considerations

---

## 1. Overview
The design system is the token layer consumed by every component in `18-components.md` and layout in `16-ui-architecture.md`. Tokens are defined once in `tailwind.config.ts` and CSS variables, never hardcoded in component styles.

---

## 2. Typography

| Token | Font | Size | Weight | Usage |
|---|---|---|---|---|
| `text-display` | Inter | 36px | 700 | Landing page hero |
| `text-h1` | Inter | 28px | 700 | Page titles |
| `text-h2` | Inter | 22px | 600 | Section headers |
| `text-h3` | Inter | 18px | 600 | Card titles |
| `text-body` | Inter | 14px | 400 | Default body text |
| `text-small` | Inter | 12px | 400 | Captions, metadata |
| `text-mono` | JetBrains Mono | 13px | 400 | Code, terminal, Monaco editor |

---

## 3. Spacing

Tailwind's default 4px base scale is used unmodified: `1 = 4px, 2 = 8px, 4 = 16px, 6 = 24px, 8 = 32px, 12 = 48px`. Component-level rule: padding inside cards/panels = `4` (16px); gaps between major layout regions = `6` (24px); gaps between inline elements (icon + label) = `2` (8px).

---

## 4. Colors

| Token | Light | Dark | Usage |
|---|---|---|---|
| `--background` | `#FFFFFF` | `#0B0E14` | App background |
| `--surface` | `#F6F7F9` | `#151922` | Cards, panels |
| `--border` | `#E5E7EB` | `#262B36` | Dividers, card borders |
| `--foreground` | `#0F172A` | `#E5E7EB` | Primary text |
| `--muted-foreground` | `#64748B` | `#8B93A3` | Secondary text |
| `--primary` | `#6366F1` (indigo) | `#818CF8` | Primary actions, links |
| `--success` | `#16A34A` | `#22C55E` | Success states, READY status |
| `--warning` | `#D97706` | `#F59E0B` | GENERATING status, warnings |
| `--destructive` | `#DC2626` | `#EF4444` | Errors, delete actions |
| `--info` | `#0284C7` | `#38BDF8` | Informational banners |

Status-to-color mapping (consistent with `projects.status` enum, `03-database.md`):

| Status | Color Token |
|---|---|
| DRAFT | `--muted-foreground` |
| GENERATING | `--warning` |
| READY | `--success` |
| DEPLOYED | `--primary` |

---

## 5. Icons
- **Library:** `lucide-react` exclusively — no mixing icon sets, to keep visual weight consistent.
- Standard sizes: `16px` (inline with text), `20px` (buttons/toolbars), `24px` (empty states, headers).
- Status icons map 1:1 with the status color tokens above (e.g., `CheckCircle` for READY, `Loader2` spinning for GENERATING).

---

## 6. Dark Mode
- Implemented via Tailwind's `class` strategy (`dark:` variants), toggled by a `ThemeProvider` storing preference in client state (Zustand) and respecting `prefers-color-scheme` on first load.
- All color tokens are CSS variables redefined under `.dark`, so components never branch on theme in JS — they simply reference the token.
- Monaco editor theme switches in lockstep with app theme (`vs-dark` / custom light theme) — see `33-code-editor.md`.

---

## 7. Accessibility
- All interactive components meet WCAG 2.1 AA contrast ratios using the token palette above (verified token-by-token, not per-instance).
- Every icon-only button has an `aria-label`; every form field has an associated `<label>` via `FormField` (`18-components.md`).
- Focus rings use `--primary` at full opacity with a 2px offset; never removed via `outline-none` without a replacement focus style.
- Keyboard navigation: Command Palette (Cmd/Ctrl+K), file tree arrow-key navigation, Monaco's native keyboard support.
- Live regions (`aria-live="polite"`) wrap streaming AI output so screen readers announce updates without spamming on every chunk (debounced).

---

## 8. Animations
- **Library:** Framer Motion, per `11-tech-stack.md`.
- Standard durations: `150ms` (hover/press feedback), `250ms` (panel open/close), `400ms` (page/route transitions).
- Easing: `ease-out` for entrances, `ease-in` for exits.
- Motion respects `prefers-reduced-motion`: all non-essential animations (page transitions, hover scale) are disabled, while functional state changes (loading spinners) remain.
- Streaming text (`agent.output`) appends without animating the *existing* text — only new chunks fade in (~100ms) to avoid layout jank.

---

## 9. Component Standards
- Every component variant set (button variants, card variants) is finite and enumerated in TypeScript types — no ad-hoc `className` overrides for variant-like behavior.
- Components never hardcode color or spacing values; only token references (`bg-primary`, `p-4`, etc.).
- Loading/empty/error states are first-class variants of a component, not separate ad-hoc markup (see `<QueryBoundary>` pattern, `17-pages.md`).

---

## 10. Implementation Notes
- Tokens are defined in `frontend/src/styles/tokens.css` (CSS variables) and mapped into `tailwind.config.ts` `theme.extend.colors`.
- A single `ThemeProvider` at the `RootLayout` level (`16-ui-architecture.md`) is the only place dark/light mode logic lives.

## 11. Future Considerations
- Multi-brand theming (white-label) if ForgeMind is offered as an embeddable platform (`48-roadmap.md`).
- Token sync to Figma via a design-tokens plugin once a dedicated design function exists.
