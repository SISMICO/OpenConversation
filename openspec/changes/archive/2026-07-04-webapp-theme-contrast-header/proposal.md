## Why

The current webapp layout feels too dark and flat, with buttons that blend into the card background and lack clear affordance. Improving contrast, introducing a coherent accent color, and letting users choose between light, dark, or system-aligned themes will make the app more comfortable, accessible, and visually clear.

## What Changes

- Add a light / dark / system theme toggle wired through `next-themes`, replacing the mixed `prefers-color-scheme` media query and `.dark` class handling.
- Introduce a simple app header that shows the app name and hosts the theme toggle.
- Redesign the color palette for both themes using comfortable grays instead of pure black, with indigo as the primary accent and a muted rose for destructive actions.
- Update button styling so every button looks like a button: primary actions use an indigo fill, secondary actions use a visible gray surface, and destructive actions use a muted rose tint.
- Adjust cards, borders, and text colors to create clear separation between page, card, and interactive surfaces.

## Capabilities

### New Capabilities
- `theme-toggle`: Allow users to switch between light, dark, and system-aligned themes, with the chosen preference persisted and applied on load.
- `ui-contrast`: Provide consistent visual hierarchy and contrast across the app layout, header, cards, and buttons using a unified indigo/rose accent palette.

### Modified Capabilities
<!-- No existing capability requirements are changing; this is a pure UI/UX refinement. -->

## Impact

- `webapp/src/main.tsx`: wrap the app in a theme provider.
- `webapp/src/App.tsx`: add a header component and update layout spacing.
- `webapp/src/index.css`: unify theme variables and remove the conflicting `prefers-color-scheme` media query.
- `webapp/src/components/ui/button.tsx`: adjust variants for stronger affordance and accent colors.
- `webapp/src/components/`: new `Header` and `ThemeToggle` components.
- No backend or API changes.
