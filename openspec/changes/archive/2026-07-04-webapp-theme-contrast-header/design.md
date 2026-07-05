## Context

The webapp currently uses a mix of custom CSS variables and shadcn theme variables. A `prefers-color-scheme: dark` media query flips custom variables such as `--bg` and `--text`, but shadcn components only react to a `.dark` class on the root element. The result is an inconsistent appearance in OS dark mode: the page background turns dark while cards and buttons remain in their light-mode palette, causing buttons to blend into surfaces. The `next-themes` package is already installed but not wired into the application.

## Goals / Non-Goals

**Goals:**
- Provide a single, reliable theme system driven by `next-themes` with light, dark, and system options.
- Add a minimal app header that contains branding and the theme toggle.
- Establish a coherent two-accent color system (indigo for primary actions, muted rose for destructive actions) with comfortable gray backgrounds.
- Ensure every button variant has enough contrast to be recognized as an interactive element.

**Non-Goals:**
- Redesigning the core recording flow or adding new features beyond theme and contrast improvements.
- Changing the backend API or data persistence.
- Supporting high-contrast or reduced-motion modes (can be added later).

## Decisions

1. **Use `next-themes` as the single source of truth**
   - Rationale: It handles system preference detection, manual override, persistence in `localStorage`, and applying/removing the `.dark` class. This removes the conflicting media-query approach.
   - Alternative considered: A custom React context. Rejected because `next-themes` is already a dependency and covers edge cases like hydration mismatches and flash-of-wrong-theme.

2. **Theme variables live in `index.css` under `:root` and `.dark`**
   - Rationale: shadcn components read `--background`, `--foreground`, `--primary`, etc. Custom app styles can continue to use `--bg`, `--text`, and `--text-h` as long as both sets are defined consistently for each theme.
   - The `prefers-color-scheme` media query will be removed; `.dark` becomes the only dark-state signal.

3. **Primary accent: indigo; destructive accent: muted rose**
   - Rationale: Indigo sits between blue (trust/calm) and purple (creative/warm), fitting a language-learning context. A muted rose keeps destructive actions noticeable without being aggressive.
   - Alternative considered: A single purple accent for everything. Rejected because the user wants two accents to differentiate action types.

4. **Header is part of `App.tsx`**
   - Rationale: The app is currently a single-page tool with no routing. A small header inside `App.tsx` is the simplest place for the toggle. If navigation grows later, it can be promoted to its own component.

5. **Button variants map to semantic roles**
   - `default` → indigo fill, white text (primary forward actions: Start, Resume, Send).
   - `secondary` → gray surface, high-contrast text (secondary actions: Pause).
   - `outline` → transparent background with visible border (tertiary actions: Discard, Cancel).
   - `destructive` → muted rose tint (Stop, Discard confirm).
   - Rationale: Every variant must be visible against the card surface. The previous outline-on-white-card problem disappears once themes are unified.

## Risks / Trade-offs

- **[Risk]** Existing users with OS dark mode may see a different look after the update.  
  → **Mitigation**: This is intentional; the new appearance is the desired behavior. No data or functionality changes.
- **[Risk]** Tailwind 4's `@theme inline` syntax requires variables to be declared carefully.  
  → **Mitigation**: Keep variable overrides inside `:root` and `.dark` blocks and test both themes with `npm run dev`.
- **[Risk]** Theme flash on initial load if `next-themes` is not configured with `disableTransitionOnChange`.  
  → **Mitigation**: Enable `disableTransitionOnChange` and set `defaultTheme="system"`.

## Migration Plan

No deployment or data migration is required. The change is confined to the webapp's CSS and React components.

## Open Questions

- Should the theme toggle be a three-state dropdown (light / dark / system) or a two-state button with system as the default? (Default recommendation: dropdown for clarity.)
- Should the header include any links or remain branding-only for now? (Default recommendation: branding-only.)
