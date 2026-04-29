---
name: Cybernetic Garden
colors:
  surface: '#0e1512'
  surface-dim: '#0e1512'
  surface-bright: '#333b38'
  surface-container-lowest: '#09100d'
  surface-container-low: '#161d1b'
  surface-container: '#1a211f'
  surface-container-high: '#242c29'
  surface-container-highest: '#2f3633'
  on-surface: '#dde4e0'
  on-surface-variant: '#bacac3'
  inverse-surface: '#dde4e0'
  inverse-on-surface: '#2b322f'
  outline: '#85948e'
  outline-variant: '#3c4a45'
  surface-tint: '#38debb'
  primary: '#ffffff'
  on-primary: '#00382d'
  primary-container: '#5ffbd6'
  on-primary-container: '#00725e'
  inverse-primary: '#006b58'
  secondary: '#d7baff'
  on-secondary: '#411478'
  secondary-container: '#593090'
  on-secondary-container: '#caa4ff'
  tertiary: '#ffffff'
  on-tertiary: '#003912'
  tertiary-container: '#69ff88'
  on-tertiary-container: '#00752e'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#5ffbd6'
  primary-fixed-dim: '#38debb'
  on-primary-fixed: '#002019'
  on-primary-fixed-variant: '#005142'
  secondary-fixed: '#eddcff'
  secondary-fixed-dim: '#d7baff'
  on-secondary-fixed: '#290055'
  on-secondary-fixed-variant: '#593090'
  tertiary-fixed: '#69ff88'
  tertiary-fixed-dim: '#31e368'
  on-tertiary-fixed: '#002108'
  on-tertiary-fixed-variant: '#00531e'
  background: '#0e1512'
  on-background: '#dde4e0'
  surface-variant: '#2f3633'
typography:
  h1:
    fontFamily: Space Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  h2:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
  h3:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  code:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 48px
  gutter: 24px
  margin: 32px
  max_width: 1200px
---

## Brand & Style

The design system is centered around the concept of a "living terminal"—a fusion of high-performance computing aesthetics and the organic growth of a digital garden. It targets an audience of developers, researchers, and tech enthusiasts who value technical precision and intellectual depth.

The visual style is a refined **Glassmorphism**. It utilizes multi-layered translucency to suggest depth and complexity without clutter. Surfaces appear as frosted panes of glass suspended in a deep space, reflecting a futuristic, sophisticated, and serene atmosphere. The emotional response should be one of "calm focus" and "technological wonder."

## Colors

The palette is optimized for a high-fidelity dark mode, using high-chroma accents against a deep, low-light abyss.

- **Primary (Cyan):** Used for primary actions, links, and high-priority data visualization.
- **Secondary (Purple):** Used for decorative elements, secondary categories, and logic-based highlights.
- **Tertiary (Emerald):** Reserved strictly for success states, active system statuses, and "blooming" nodes in the digital garden.
- **Neutral/Background:** The deep blue base provides a stable foundation for light-refracting glass layers.

For the Light mode variant, invert the background to a soft cool gray (#F0F4F8) while maintaining the accents, but increase the opacity of the glass layers to ensure legibility and contrast.

## Typography

This design system uses a dual-font approach to balance human-centric reading with technical rigor.

- **Headlines & Labels:** Uses **Space Grotesk** for its geometric, futuristic personality. This font carries the brand's voice in both English and numerical data.
- **Body Text:** Uses **Inter** for maximum readability, particularly for Simplified Chinese characters which require clarity at smaller sizes.
- **Technical Details:** **JetBrains Mono** is mandatory for all code snippets, terminal outputs, and metadata tags to maintain the "Computer Science" aesthetic.

For Chinese text, ensure a minimum line height of 1.6 to prevent visual crowding of complex glyphs. Technical English terms within Chinese sentences should inherit the font-weight of the surrounding text but may use the monospaced font if they refer to variables or specific code entities.

## Layout & Spacing

The layout follows a **Fixed Grid** model on desktop to mimic a controlled dashboard environment, transitioning to a fluid single column on mobile. 

The rhythm is based on a 4px baseline. Components should be grouped using generous whitespace to allow the glass background blurs to feel airy rather than cluttered. Terminal panels and content cards should align to a 12-column grid. On the digital garden map, spacing is organic and node-based, but UI overlays must remain anchored to the viewport edges with `xl` margins.

## Elevation & Depth

Depth is communicated through **Backdrop Blurs** and **Z-axis Layering** rather than traditional heavy shadows.

1.  **Level 0 (Base):** Deep blue solid background.
2.  **Level 1 (Panels):** `backdrop-filter: blur(12px)` with a 15% white border.
3.  **Level 2 (Popovers/Modals):** `backdrop-filter: blur(24px)` with a 25% white border and a subtle cyan-tinted ambient glow (`box-shadow: 0 20px 40px rgba(100, 255, 218, 0.1)`).

The "active" state of a component is indicated by an increase in border opacity and a subtle inner glow, making the element appear as if it is being backlit.

## Shapes

The design system employs a **Rounded** shape language to soften the futuristic aesthetic, making the "garden" feel approachable. 

- **Primary Containers:** 16px (1rem) corner radius for cards and terminal panels.
- **Interactive Elements:** 8px (0.5rem) for buttons and input fields.
- **Status Pills:** Fully rounded (pill-shaped) to distinguish them from structural elements.
- **Borders:** All glass elements must feature a 1px solid border to define their edges against the blur.

## Components

- **Terminal Panels:** Use a dark, semi-transparent header bar with three decorative window controls (red, yellow, green) on the top-left. Content inside uses the Monospace font.
- **Node Maps:** Interactive circles connected by thin, 1px lines. Active nodes should pulse with a Cyan glow. Use "Organic" layout positioning.
- **Buttons:**
    - *Primary:* Solid Cyan background with dark text, 8px radius.
    - *Ghost:* 1px Cyan border, transparent background, becomes solid on hover.
- **Progress Bars:** Thin (4px) tracks. The "filled" portion should be a gradient from Cyan to Purple, with a small glow effect at the leading edge.
- **Cards:** Glassmorphic background, 16px radius. Titles in Space Grotesk. Hovering should slightly increase the backdrop-blur intensity.
- **Language Switchers:** Small, pill-shaped toggles using Monospace font for "ZH" and "EN" labels.