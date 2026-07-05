## 1. Theme Provider Setup

- [x] 1.1 Wrap `App` in `ThemeProvider` from `next-themes` inside `main.tsx` with `defaultTheme="system"`, `enableSystem={true}`, and `disableTransitionOnChange`.
- [x] 1.2 Remove the `prefers-color-scheme: dark` media query from `index.css` so the `.dark` class is the only dark-theme signal.

## 2. Color Palette and CSS Variables

- [x] 2.1 Update `:root` light-theme shadcn variables in `index.css` to use a clean gray page background, white cards, indigo primary, and muted rose destructive.
- [x] 2.2 Update `.dark` variables in `index.css` to use a near-black gray page, slightly lifted card surface, indigo primary, and muted rose destructive.
- [x] 2.3 Align custom app variables (`--text`, `--text-h`, `--bg`, `--border`, `--code-bg`, `--accent`, etc.) with the corresponding shadcn palette in both themes.

## 3. Button Styling

- [x] 3.1 Update `components/ui/button.tsx` so the `default` variant uses an indigo fill with white text in both themes.
- [x] 3.2 Update the `secondary` variant to use a visible gray surface with high-contrast text.
- [x] 3.3 Update the `outline` variant to have a clearly visible border against the card background.
- [x] 3.4 Update the `destructive` variant to use a muted rose tint that remains readable in both themes.

## 4. Header and Theme Toggle Components

- [x] 4.1 Create `components/Header.tsx` with the app name "OpenConversation".
- [x] 4.2 Create `components/ThemeToggle.tsx` using `next-themes` `useTheme` hook with options for light, dark, and system.
- [x] 4.3 Add the `ThemeToggle` to the `Header` and position it on the right.

## 5. App Layout Integration

- [x] 5.1 Import and render `Header` at the top of `App.tsx`.
- [x] 5.2 Adjust `App.tsx` layout spacing so the header sits above the existing main content without crowding.
- [x] 5.3 Verify button variants used in `App.tsx` (Start, Pause, Stop, Send, Discard) map to the intended semantic roles.

## 6. Verification

- [x] 6.1 Run `npm run dev` and visually inspect light, dark, and system themes.
- [x] 6.2 Run `npm run lint` and `npm test` and fix any failures.
- [x] 6.3 Confirm theme preference persists across page reloads.
