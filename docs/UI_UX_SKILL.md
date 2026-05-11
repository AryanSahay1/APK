---
name: uiux-design-master
description: >
  Comprehensive UI/UX design intelligence for Cursor AI. Use this skill whenever
  designing, building, reviewing, or improving any interface — websites, apps,
  dashboards, landing pages, components, or design systems. Covers ALL design
  disciplines: visual design, interaction design, motion & animations, 3D UI,
  cursor/pointer effects, micro-interactions, typography, color systems,
  accessibility, design tokens, glassmorphism, neumorphism, brutalism, bento
  grids, parallax, scroll-driven animations, skeleton loaders, page transitions,
  Lottie, Rive, GSAP, Framer Motion, Three.js UI, WebGL surfaces, CSS 3D
  transforms, spring physics, AR/VR UX, and AI-assisted design patterns.
  Trigger on: "design a UI", "make it look better", "add animations", "create
  3D effect", "cursor effect", "smooth transitions", "design system", "component
  library", "dark mode", "responsive layout", "glassmorphism", "parallax",
  "micro-interactions", "loading states", "hover effects", "scroll animations".
---

# UI/UX Design Master Skill

> Full-spectrum design intelligence. This skill is the single source of truth
> for all UI/UX decisions — from pixel-level polish to system-level architecture.
> Read the relevant section before generating or reviewing any interface code.

---

## Table of Contents

1. [Skill Activation Rules](#1-skill-activation-rules)
2. [Visual Design Fundamentals](#2-visual-design-fundamentals)
3. [Color Systems](#3-color-systems)
4. [Typography](#4-typography)
5. [Layout & Spacing](#5-layout--spacing)
6. [Design Styles Catalog](#6-design-styles-catalog)
7. [Component Patterns](#7-component-patterns)
8. [Interaction Design](#8-interaction-design)
9. [Motion Design & Animations](#9-motion-design--animations)
10. [3D UI Design](#10-3d-ui-design)
11. [Cursor & Pointer Effects](#11-cursor--pointer-effects)
12. [Micro-Interactions](#12-micro-interactions)
13. [Scroll-Driven Design](#13-scroll-driven-design)
14. [Page & Route Transitions](#14-page--route-transitions)
15. [Loading & Skeleton States](#15-loading--skeleton-states)
16. [Accessibility (A11y)](#16-accessibility-a11y)
17. [Responsive & Adaptive Design](#17-responsive--adaptive-design)
18. [Design Systems & Tokens](#18-design-systems--tokens)
19. [Dark Mode](#19-dark-mode)
20. [Data Visualization](#20-data-visualization)
21. [AR/VR & Immersive UX](#21-arvr--immersive-ux)
22. [AI-Assisted Design Patterns](#22-ai-assisted-design-patterns)
23. [Animation Libraries Reference](#23-animation-libraries-reference)
24. [Performance Rules](#24-performance-rules)
25. [Handoff & Implementation](#25-handoff--implementation)

---

## 1. Skill Activation Rules

Invoke this skill automatically for any task involving:

| Trigger phrase | Section to read |
|---|---|
| "design", "UI", "layout", "component" | §2, §5, §7 |
| "color", "palette", "dark mode", "theme" | §3, §19 |
| "font", "typography", "text" | §4 |
| "animation", "motion", "transition" | §9, §14 |
| "3D", "depth", "WebGL", "Three.js" | §10 |
| "cursor", "pointer", "hover", "trail" | §11 |
| "micro-interaction", "feedback", "click" | §12 |
| "scroll", "parallax", "sticky", "reveal" | §13 |
| "loading", "skeleton", "spinner" | §15 |
| "accessible", "WCAG", "screen reader" | §16 |
| "mobile", "responsive", "breakpoint" | §17 |
| "design token", "design system", "Figma" | §18 |
| "chart", "graph", "dashboard" | §20 |
| "AR", "VR", "spatial", "immersive" | §21 |
| "glassmorphism", "neumorphism", "brutalism" | §6 |
| "GSAP", "Framer Motion", "Lottie", "Rive" | §23 |

---

## 2. Visual Design Fundamentals

### Hierarchy (The Big 3)
1. **Size** — Larger = more important. Establish 3 levels: hero, body, caption.
2. **Contrast** — Minimum 4.5:1 for body text (WCAG AA). Use contrast as a tool, not decoration.
3. **Proximity** — Elements that belong together live within 8px of each other; unrelated elements have 24px+ gaps.

### Visual Weight Rules
- One primary CTA per screen. Never two equal-weight buttons side by side.
- F-pattern and Z-pattern reading — place key content along these natural eye paths.
- Whitespace is not empty — it guides, rests, and emphasizes.
- Odd numbers of objects create more visual tension and interest than even.

### Gestalt Principles in UI
- **Closure**: Use borders or backgrounds to imply grouping without full outlines.
- **Continuation**: Horizontal scrollers should bleed slightly off-edge to signal more content.
- **Similarity**: Consistent icon style, stroke weight (1.5px or 2px, never mixed).
- **Figure/Ground**: High-contrast modals, toasts, and popovers must feel lifted.

### Depth & Elevation Model (4-tier)
```
Level 0 — Base surface (background)
Level 1 — Cards, list items (shadow: 0 1px 3px rgba(0,0,0,0.08))
Level 2 — Dropdowns, tooltips (shadow: 0 4px 16px rgba(0,0,0,0.12))
Level 3 — Modals, drawers (shadow: 0 24px 48px rgba(0,0,0,0.24))
Level 4 — Fullscreen overlays (backdrop blur: 12px)
```

---

## 3. Color Systems

### Palette Architecture
Every UI needs exactly these roles:
- **Brand/Primary**: Main actions, links, focus rings
- **Secondary**: Supporting actions, tags, chips
- **Neutral**: Text, borders, backgrounds (9 shades: 50–950)
- **Semantic**: success (#22c55e), warning (#f59e0b), error (#ef4444), info (#3b82f6)
- **Surface**: Page bg, card bg, elevated bg (distinct in dark mode)

### Color Scales (Per Hue)
Generate a 10-step scale: 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950.
- Use **400–600** for interactive elements in light mode
- Use **300–400** for interactive elements in dark mode (lighter for contrast)
- Use **50–100** for subtle backgrounds, 950 for deep backgrounds in dark mode

### Contrast Enforcement
```
Body text on white bg:    ≥ 4.5:1  (WCAG AA)
Large text (18px+):       ≥ 3:1
Interactive elements:     ≥ 3:1 for non-text UI
Ideal target:             7:1+ (WCAG AAA)
```

### Popular Palettes by Product Type
| Product | Primary | Accent |
|---|---|---|
| SaaS / B2B | Indigo-600 or Violet-600 | Cyan-500 |
| FinTech | Blue-700 or Slate-800 | Emerald-500 |
| Health / Wellness | Teal-600 or Green-600 | Amber-400 |
| E-Commerce | Rose-500 or Orange-500 | Yellow-400 |
| Creative / Portfolio | Purple-600 | Pink-500 |
| AI Products | Violet-600 | Sky-400 |
| Dev Tools | Zinc-900 (dark default) | Lime-400 |

### Gradient Patterns
```css
/* Mesh gradient — modern SaaS hero */
background: radial-gradient(at 40% 20%, hsl(228,96%,65%) 0px, transparent 50%),
            radial-gradient(at 80% 0%, hsl(189,100%,56%) 0px, transparent 50%),
            radial-gradient(at 0% 50%, hsl(270,100%,76%) 0px, transparent 50%);

/* Subtle card tint */
background: linear-gradient(135deg, hsl(var(--card)) 0%, hsl(var(--card) / 0.6) 100%);

/* Aurora border effect */
background: conic-gradient(from 180deg at 50% 50%, #6366f1, #a855f7, #ec4899, #6366f1);
```

---

## 4. Typography

### Type Scale (Major Third — ratio 1.25)
```
xs:   12px / 0.75rem
sm:   14px / 0.875rem
base: 16px / 1rem
lg:   18px / 1.125rem
xl:   20px / 1.25rem
2xl:  24px / 1.5rem
3xl:  30px / 1.875rem
4xl:  36px / 2.25rem
5xl:  48px / 3rem
6xl:  60px / 3.75rem
7xl:  72px / 4.5rem
```

### Line Height Rules
- **Body text**: 1.5–1.6 (comfortable reading)
- **Headings**: 1.1–1.25 (tight, impactful)
- **Code/monospace**: 1.6–1.7
- **UI labels/buttons**: 1.0–1.2

### Font Pairing Playbook
| Style | Heading | Body |
|---|---|---|
| Modern SaaS | Inter, Cal Sans | Inter |
| Editorial | Fraunces, Playfair | DM Sans, Source Serif |
| Tech/Dev | JetBrains Mono | Geist, Inter |
| Luxury | Cormorant Garamond | Jost |
| Playful | Syne | Nunito |
| Minimalist | Geist | Geist |
| Bold/Impact | Clash Display | Satoshi |

### Typography Rules
- **Never** use more than 2 typefaces in one product
- **Max line length**: 60–75 characters (optimal reading rhythm)
- **Letter-spacing**: -0.02em to -0.04em for large headings; 0.02em–0.08em for ALL CAPS labels
- **Font-weight jumps**: Skip at least one weight (400→700, not 400→500 for contrast)
- **Variable fonts** preferred for animations and responsive scaling

---

## 5. Layout & Spacing

### Spacing Scale (4px base unit)
```
1 = 4px   (hairline gaps)
2 = 8px   (tight padding, icon gaps)
3 = 12px  (form field padding)
4 = 16px  (standard padding)
5 = 20px  (card padding small)
6 = 24px  (card padding standard)
8 = 32px  (section gaps)
10 = 40px
12 = 48px (large section padding)
16 = 64px (section breaks)
20 = 80px (hero padding)
24 = 96px (large hero)
32 = 128px
```

### Grid Systems
```
Mobile:  4 columns, 16px gutter, 16px margin
Tablet:  8 columns, 24px gutter, 32px margin
Desktop: 12 columns, 24–32px gutter, auto margin
Wide:    12–16 columns, 32px gutter, max-width 1440px
```

### Container Widths
```css
--container-sm:   640px;   /* prose, blog */
--container-md:   768px;   /* forms, focused content */
--container-lg:   1024px;  /* standard page */
--container-xl:   1280px;  /* dashboard */
--container-2xl:  1536px;  /* wide layout */
```

### Common Layout Patterns
- **Holy Grail**: `grid-template: auto 1fr auto / auto 1fr auto`
- **Sidebar + Content**: `grid-template-columns: 240px 1fr` (collapsible on mobile)
- **Card Grid**: `grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`
- **Masonry**: CSS `columns` property or JS masonry library
- **Bento Grid**: Asymmetric CSS grid with `grid-column: span N` variation

---

## 6. Design Styles Catalog

### Glassmorphism
```css
.glass {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
}
/* Dark glass */
.glass-dark {
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
```
**When to use**: Navigation bars floating over imagery, hero cards, modals over gradient backgrounds, dashboard widgets.  
**Avoid when**: Background lacks sufficient contrast variation; causes readability issues.

### Neumorphism
```css
.neumorphic {
  background: #e0e5ec;
  border-radius: 20px;
  box-shadow: 8px 8px 16px #b8bec7, -8px -8px 16px #ffffff;
}
.neumorphic-pressed {
  box-shadow: inset 4px 4px 8px #b8bec7, inset -4px -4px 8px #ffffff;
}
```
**When to use**: Interfaces with minimal color, tactile dashboards, wellness/medical apps.  
**Avoid when**: Low vision users; fails contrast checks without careful tuning.

### Brutalism
```css
.brutalist-card {
  border: 3px solid #000;
  border-radius: 0;
  box-shadow: 6px 6px 0px #000;
  background: #fff;
  transition: box-shadow 0.1s, transform 0.1s;
}
.brutalist-card:hover {
  transform: translate(-3px, -3px);
  box-shadow: 9px 9px 0px #000;
}
```
**When to use**: Creative portfolios, editorial sites, developer tools targeting hackers.

### Bento Grid
```css
.bento {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 200px;
  gap: 16px;
}
.bento-wide  { grid-column: span 2; }
.bento-tall  { grid-row: span 2; }
.bento-large { grid-column: span 2; grid-row: span 2; }
```

### Claymorphism
```css
.clay {
  background: linear-gradient(145deg, #ff9a9e, #fecfef);
  border-radius: 24px;
  box-shadow:
    inset 0 -4px 8px rgba(0,0,0,0.15),
    inset 0 4px 8px rgba(255,255,255,0.5),
    0 12px 32px rgba(255,154,158,0.4);
}
```
**When to use**: Consumer apps, kids/education, lifestyle brands.

### Flat Design 2.0
Clean, minimal, purposeful shadows only for elevation. No gradients except for brand moments. Strong typographic hierarchy.

### Skeuomorphism (Modern Revival)
Subtle real-world textures, physical metaphors for toggles and sliders. Popular in audio/music apps and premium products.

---

## 7. Component Patterns

### Buttons
```
Sizes:        xs(28px), sm(32px), md(40px), lg(48px), xl(56px)
Radius:       4px (squared), 8px (default), 9999px (pill)
States:       default, hover, active/pressed, focus, disabled, loading
Icon padding: reduce horizontal padding by 4px each side when icon present
Min width:    88px for primary actions (prevent orphan text)
```

```css
/* Loading state — animated spinner inline */
.btn-loading {
  pointer-events: none;
  opacity: 0.7;
  gap: 8px;
}
.btn-loading::after {
  content: '';
  width: 14px; height: 14px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
```

### Cards
- Padding: 20px (compact), 24px (standard), 32px (spacious)
- Hover state: lift with `transform: translateY(-4px)` + deeper shadow
- Image aspect ratios: 16:9 (wide), 4:3 (standard), 1:1 (square avatar/product)

### Forms
- Label above input (never placeholder-only)
- Error state: red border (#ef4444) + icon + inline message below
- Success state: green border + checkmark (only after submit or on blur)
- Floating labels for dense data-entry forms
- Group related fields with 8px gap; separate groups with 24px

### Navigation
- **Top nav**: sticky, max 7 items, CTA button right-aligned
- **Side nav**: icons + labels, collapsible to icons-only, 240px expanded / 64px collapsed
- **Mobile**: bottom nav (4–5 items max) or hamburger drawer
- **Breadcrumbs**: always on pages ≥3 levels deep
- **Active state**: left border accent (sidebar) or bottom border (top nav)

### Modals / Dialogs
- Max width: 480px (small), 640px (medium), 800px (large), 90vw (responsive)
- Backdrop: `rgba(0,0,0,0.5)` with `backdrop-filter: blur(4px)`
- Enter: `scale(0.95) opacity(0) → scale(1) opacity(1)`, 200ms ease-out
- Exit: `scale(0.95) opacity(0)`, 150ms ease-in (faster exits feel snappier)
- Always trap focus; close on Escape and backdrop click

### Tables
- Sticky header on scroll (position: sticky; top: 0)
- Row hover: subtle bg highlight (`bg-muted/50`)
- Sortable columns: sort icon visible on hover, active sort shows direction
- Loading state: skeleton rows with shimmer animation
- Empty state: centered icon + message + optional CTA

### Toast / Notifications
- Position: bottom-right (desktop), top-center (mobile)
- Auto-dismiss: success 3s, info 4s, error persists until dismissed
- Stack newest on top; animate in from edge
- Max 3 visible at once; queue overflow

---

## 8. Interaction Design

### The 7 UX Motion Principles (after UX in Motion)
1. **Easing** — Nothing moves at constant speed in real life. Always use easing curves.
2. **Offset & Delay** — Stagger related elements (40–80ms between each) for visual delight.
3. **Parenting** — Child elements respond to parent motion (scale, position, opacity).
4. **Transformation** — Objects morph between states; never jump discontinuously.
5. **Value Change** — Numbers, counters, progress bars animate, they don't snap.
6. **Masking** — Reveal through mask shapes rather than opacity fade for dramatic entrances.
7. **Overlay** — Layered content slides/scales in over parent context.

### Interaction States (Every Interactive Element Needs All 5)
```
Default  → Rest state, initial render
Hover    → Mouse over (desktop only, not for mobile-first)
Active   → Click/tap held down (scale: 0.97, brightness: 0.95)
Focus    → Keyboard navigation (2px ring, 2px offset, brand color)
Disabled → opacity: 0.5, cursor: not-allowed, no pointer-events
```

### Touch Targets
- Minimum: 44×44px (iOS HIG), 48×48dp (Material Design)
- Recommended: 48×48px minimum clickable area
- Spacing between targets: ≥8px

### Gesture Support (Mobile)
- Swipe to dismiss (cards, notifications, drawers)
- Long press for context menus
- Pinch-to-zoom for maps and images
- Pull-to-refresh with spring physics

---

## 9. Motion Design & Animations

### The Golden Rules
1. **Purposeful**: Every animation must communicate meaning — state change, hierarchy, feedback, spatial relationship.
2. **Performance**: Animate ONLY `transform` and `opacity`. Never animate `width`, `height`, `top`, `left`, `margin`.
3. **Duration**: UI transitions 150–400ms. Micro-interactions 80–200ms. Complex choreography 400–700ms. Never exceed 700ms.
4. **Reduced Motion**: Always provide `prefers-reduced-motion: reduce` alternatives.

### Easing Curve Reference
```css
/* Standard easing */
--ease-in:        cubic-bezier(0.4, 0, 1, 1);
--ease-out:       cubic-bezier(0, 0, 0.2, 1);     /* most UI interactions */
--ease-in-out:    cubic-bezier(0.4, 0, 0.2, 1);    /* repositioning */

/* Expressive */
--ease-spring:    cubic-bezier(0.34, 1.56, 0.64, 1);  /* slight overshoot */
--ease-bounce:    cubic-bezier(0.68, -0.55, 0.27, 1.55);
--ease-elastic:   cubic-bezier(0.5, -0.5, 0.5, 1.5);

/* Enter/Exit pair */
--ease-enter:     cubic-bezier(0, 0, 0.2, 1);     /* decelerate in */
--ease-exit:      cubic-bezier(0.4, 0, 1, 1);     /* accelerate out */
```

### Duration Scale
```
instant:    0ms    (no animation, immediate feedback)
fast:       80ms   (hover bg, color changes)
normal:     150ms  (icon swaps, subtle fades)
moderate:   250ms  (dropdown open, tooltip)
slow:       350ms  (modal entry, page element reveal)
deliberate: 500ms  (page transitions, complex reveals)
expressive: 700ms  (hero animations, onboarding sequences)
```

### Animation Patterns Library

**Fade In**
```css
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}
```

**Slide + Fade (List Item Enter)**
```css
@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
/* Stagger with nth-child delay: nth-child(n) { animation-delay: calc(n * 60ms) } */
```

**Scale In (Modal / Popover)**
```css
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.92); }
  to   { opacity: 1; transform: scale(1); }
}
```

**Shimmer (Skeleton Loading)**
```css
@keyframes shimmer {
  from { background-position: -200% 0; }
  to   { background-position: 200% 0; }
}
.skeleton {
  background: linear-gradient(90deg,
    hsl(var(--muted)) 25%,
    hsl(var(--muted) / 0.5) 50%,
    hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
```

**Typing Cursor**
```css
@keyframes blink { 50% { opacity: 0; } }
.cursor::after {
  content: '|';
  animation: blink 1s step-end infinite;
}
```

**Pulse (Notification Badge)**
```css
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.15); opacity: 0.8; }
}
```

**Orbit / Spin (Loaders)**
```css
@keyframes spin {
  to { transform: rotate(360deg); }
}
.spinner {
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
```

**Number Counter (Value Animation)**
```js
// Animate a number from 0 to target in duration ms
function animateCount(el, target, duration = 1000) {
  const start = performance.now();
  const update = (now) => {
    const t = Math.min((now - start) / duration, 1);
    const ease = 1 - Math.pow(1 - t, 3); // ease-out cubic
    el.textContent = Math.round(ease * target).toLocaleString();
    if (t < 1) requestAnimationFrame(update);
  };
  requestAnimationFrame(update);
}
```

### Screen Transitions (Choreography)
```
Exit (outgoing screen):  fade out + slight scale down (0.97) — 150ms ease-in
Enter (incoming screen): fade in + slide from direction — 250ms ease-out
Shared element:          FLIP technique — natural physical movement between screens
```

---

## 10. 3D UI Design

### CSS 3D Transforms
```css
/* 3D Card Flip */
.card-flip-container {
  perspective: 1200px;
}
.card-flip {
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.card-flip:hover { transform: rotateY(180deg); }
.card-front, .card-back { backface-visibility: hidden; }
.card-back { transform: rotateY(180deg); }

/* 3D Layered Float */
.hero-3d {
  transform: perspective(1000px) rotateX(8deg) rotateY(-6deg);
  transition: transform 0.4s ease-out;
}
.hero-3d:hover {
  transform: perspective(1000px) rotateX(0deg) rotateY(0deg);
}

/* Mouse-tracking tilt (JS) */
el.addEventListener('mousemove', (e) => {
  const rect = el.getBoundingClientRect();
  const x = (e.clientX - rect.left) / rect.width  - 0.5;  // -0.5 to 0.5
  const y = (e.clientY - rect.top)  / rect.height - 0.5;
  el.style.transform =
    `perspective(800px) rotateX(${-y * 14}deg) rotateY(${x * 14}deg)`;
});
el.addEventListener('mouseleave', () => {
  el.style.transform = 'perspective(800px) rotateX(0) rotateY(0)';
});
```

### Three.js UI Integration
```js
// Minimal Three.js background (animated gradient mesh)
import * as THREE from 'three';

const scene    = new THREE.Scene();
const camera   = new THREE.PerspectiveCamera(75, innerWidth/innerHeight, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
renderer.setSize(innerWidth, innerHeight);
renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
document.body.appendChild(renderer.domElement);

// Floating particles
const geo  = new THREE.BufferGeometry();
const count = 800;
const positions = new Float32Array(count * 3);
for (let i = 0; i < count * 3; i++) positions[i] = (Math.random() - 0.5) * 20;
geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
const mat = new THREE.PointsMaterial({ size: 0.02, color: 0x6366f1 });
scene.add(new THREE.Points(geo, mat));

camera.position.z = 5;
renderer.setAnimationLoop(() => {
  scene.rotation.y += 0.0005;
  renderer.render(scene, camera);
});
```

### CSS 3D Scene Patterns
```css
/* Isometric card stack */
.stack { transform-style: preserve-3d; perspective: 800px; }
.stack-item:nth-child(1) { transform: translateZ(0px); }
.stack-item:nth-child(2) { transform: translateZ(-12px) translateY(8px); opacity: 0.7; }
.stack-item:nth-child(3) { transform: translateZ(-24px) translateY(16px); opacity: 0.4; }

/* 3D Floating badge */
@keyframes float3d {
  0%   { transform: perspective(600px) translateZ(0)   rotateY(0deg); }
  50%  { transform: perspective(600px) translateZ(20px) rotateY(8deg); }
  100% { transform: perspective(600px) translateZ(0)   rotateY(0deg); }
}
```

### Spline / React Three Fiber
```jsx
// React Three Fiber — interactive 3D hero
import { Canvas, useFrame } from '@react-three/fiber'
import { Float, Environment, OrbitControls } from '@react-three/drei'

function FloatingObject() {
  return (
    <Float speed={2} rotationIntensity={0.4} floatIntensity={0.8}>
      <mesh>
        <icosahedronGeometry args={[1, 4]} />
        <meshStandardMaterial color="#6366f1" roughness={0.1} metalness={0.8} />
      </mesh>
    </Float>
  )
}

export function Hero3D() {
  return (
    <Canvas camera={{ position: [0, 0, 5] }} style={{ height: '100vh' }}>
      <ambientLight intensity={0.5} />
      <directionalLight position={[10, 10, 5]} intensity={1} />
      <Environment preset="city" />
      <FloatingObject />
      <OrbitControls enableZoom={false} enablePan={false} />
    </Canvas>
  )
}
```

---

## 11. Cursor & Pointer Effects

### Custom Cursor (Pure CSS + JS)
```html
<div class="cursor-dot"></div>
<div class="cursor-ring"></div>
```

```css
.cursor-dot, .cursor-ring {
  position: fixed;
  top: 0; left: 0;
  pointer-events: none;
  z-index: 9999;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  transition: opacity 0.3s;
}
.cursor-dot {
  width: 8px; height: 8px;
  background: hsl(var(--primary));
}
.cursor-ring {
  width: 36px; height: 36px;
  border: 2px solid hsl(var(--primary) / 0.5);
  transition: width 0.2s, height 0.2s, border-color 0.2s;
}
/* Hover state — ring expands over links and buttons */
body:has(a:hover, button:hover) .cursor-ring {
  width: 52px; height: 52px;
  border-color: hsl(var(--primary));
}
```

```js
// Smooth cursor tracking with lag
let cursorX = 0, cursorY = 0;
let ringX = 0, ringY = 0;
const dot  = document.querySelector('.cursor-dot');
const ring = document.querySelector('.cursor-ring');

document.addEventListener('mousemove', e => {
  cursorX = e.clientX; cursorY = e.clientY;
  dot.style.left = cursorX + 'px';
  dot.style.top  = cursorY + 'px';
});

function animateCursor() {
  ringX += (cursorX - ringX) * 0.12;   // lag coefficient (lower = more lag)
  ringY += (cursorY - ringY) * 0.12;
  ring.style.left = ringX + 'px';
  ring.style.top  = ringY + 'px';
  requestAnimationFrame(animateCursor);
}
animateCursor();
```

### Magnetic Button Effect
```js
function magneticButton(el) {
  el.addEventListener('mousemove', (e) => {
    const rect = el.getBoundingClientRect();
    const x = e.clientX - rect.left - rect.width  / 2;
    const y = e.clientY - rect.top  - rect.height / 2;
    el.style.transform = `translate(${x * 0.3}px, ${y * 0.3}px)`;
  });
  el.addEventListener('mouseleave', () => {
    el.style.transform = '';
    el.style.transition = 'transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)';
  });
}
```

### Spotlight / Radial Glow on Hover
```js
const card = document.querySelector('.card');
card.addEventListener('mousemove', (e) => {
  const rect = card.getBoundingClientRect();
  const x = ((e.clientX - rect.left) / rect.width)  * 100;
  const y = ((e.clientY - rect.top)  / rect.height) * 100;
  card.style.setProperty('--mouse-x', `${x}%`);
  card.style.setProperty('--mouse-y', `${y}%`);
});
```
```css
.card::before {
  content: '';
  position: absolute; inset: 0;
  background: radial-gradient(
    300px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    rgba(99, 102, 241, 0.15), transparent
  );
  border-radius: inherit;
  opacity: 0;
  transition: opacity 0.3s;
}
.card:hover::before { opacity: 1; }
```

### Cursor Trail Effect
```js
const trail = Array.from({ length: 20 }, (_, i) => {
  const dot = document.createElement('div');
  dot.className = 'trail-dot';
  dot.style.cssText = `
    position: fixed; pointer-events: none; border-radius: 50%;
    width: ${12 - i * 0.5}px; height: ${12 - i * 0.5}px;
    background: hsl(var(--primary) / ${1 - i / 20});
    z-index: 9998; transform: translate(-50%, -50%);
  `;
  document.body.appendChild(dot);
  return dot;
});
const positions = trail.map(() => ({ x: 0, y: 0 }));
document.addEventListener('mousemove', e => {
  positions[0] = { x: e.clientX, y: e.clientY };
});
function animateTrail() {
  for (let i = trail.length - 1; i > 0; i--) {
    positions[i].x += (positions[i-1].x - positions[i].x) * 0.35;
    positions[i].y += (positions[i-1].y - positions[i].y) * 0.35;
    trail[i].style.left = positions[i].x + 'px';
    trail[i].style.top  = positions[i].y + 'px';
  }
  requestAnimationFrame(animateTrail);
}
animateTrail();
```

---

## 12. Micro-Interactions

### Taxonomy of Micro-Interactions
| Type | Purpose | Duration |
|---|---|---|
| Trigger feedback | Confirm tap/click registered | 80–120ms |
| State transition | Show status change | 150–300ms |
| Progress indicator | Show ongoing work | Looping |
| System status | Communicate result (success/error) | 200–400ms |
| Input validation | Real-time field feedback | 100–200ms |
| Onboarding hint | Guide first-time users | 500–800ms |
| Contextual hint | Surface hidden features | 300–500ms |
| Delight moment | Reward and surprise | 300–600ms |

### Toggle Switch Animation
```css
.toggle { width: 44px; height: 24px; position: relative; }
.toggle input { display: none; }
.toggle-slider {
  position: absolute; inset: 0;
  background: hsl(var(--muted));
  border-radius: 9999px;
  cursor: pointer;
  transition: background 0.2s;
}
.toggle-slider::after {
  content: '';
  position: absolute; left: 2px; top: 2px;
  width: 20px; height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
}
input:checked ~ .toggle-slider { background: hsl(var(--primary)); }
input:checked ~ .toggle-slider::after {
  transform: translateX(20px);
}
```

### Like / Heart Animation (React)
```jsx
function LikeButton() {
  const [liked, setLiked] = useState(false);
  return (
    <button
      onClick={() => setLiked(!liked)}
      style={{
        transform: liked ? 'scale(1)' : 'scale(1)',
        transition: 'transform 0.1s',
      }}
      onMouseDown={e => e.currentTarget.style.transform = 'scale(0.85)'}
      onMouseUp={e => {
        e.currentTarget.style.transform = 'scale(1.25)';
        setTimeout(() => e.currentTarget.style.transform = 'scale(1)', 200);
      }}
    >
      <HeartIcon fill={liked ? '#ef4444' : 'none'} />
    </button>
  );
}
```

### Checkbox Checkmark Draw
```css
@keyframes checkDraw {
  from { stroke-dashoffset: 30; }
  to   { stroke-dashoffset: 0; }
}
.check-svg path {
  stroke-dasharray: 30;
  stroke-dashoffset: 30;
}
input:checked ~ .check-svg path {
  animation: checkDraw 0.25s ease-out forwards;
}
```

### Form Field Validation Shake
```css
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%       { transform: translateX(-6px); }
  40%       { transform: translateX(6px); }
  60%       { transform: translateX(-4px); }
  80%       { transform: translateX(4px); }
}
.input-error { animation: shake 0.4s ease-out; }
```

---

## 13. Scroll-Driven Design

### Intersection Observer (Reveal on Scroll)
```js
const observer = new IntersectionObserver(
  (entries) => entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('is-visible');
      observer.unobserve(entry.target); // animate once
    }
  }),
  { threshold: 0.15, rootMargin: '0px 0px -64px 0px' }
);
document.querySelectorAll('[data-reveal]').forEach(el => observer.observe(el));
```
```css
[data-reveal] {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}
[data-reveal].is-visible {
  opacity: 1;
  transform: translateY(0);
}
```

### CSS Scroll-Driven Animations (Native, 2024+)
```css
@keyframes revealFromLeft {
  from { opacity: 0; transform: translateX(-40px); }
  to   { opacity: 1; transform: translateX(0); }
}
.animated-section {
  animation: revealFromLeft linear both;
  animation-timeline: view();
  animation-range: entry 0% entry 40%;
}
```

### Parallax (CSS + JS)
```css
.parallax-layer {
  will-change: transform;
  transform: translateY(var(--parallax-y, 0));
}
```
```js
window.addEventListener('scroll', () => {
  const scrollY = window.scrollY;
  document.querySelectorAll('.parallax-layer').forEach(el => {
    const speed = parseFloat(el.dataset.speed || '0.5');
    el.style.setProperty('--parallax-y', `${scrollY * speed}px`);
  });
}, { passive: true });
```

### Sticky Progress Bar
```css
.scroll-progress {
  position: fixed; top: 0; left: 0; height: 3px;
  background: hsl(var(--primary));
  transform-origin: left;
  z-index: 100;
}
```
```js
window.addEventListener('scroll', () => {
  const pct = window.scrollY / (document.body.scrollHeight - innerHeight);
  document.querySelector('.scroll-progress').style.transform = `scaleX(${pct})`;
}, { passive: true });
```

---

## 14. Page & Route Transitions

### React / Next.js (Framer Motion)
```jsx
import { AnimatePresence, motion } from 'framer-motion';

const pageVariants = {
  initial:  { opacity: 0, y: 16 },
  animate:  { opacity: 1, y: 0, transition: { duration: 0.25, ease: [0, 0, 0.2, 1] } },
  exit:     { opacity: 0, y: -8, transition: { duration: 0.15 } },
};

export function PageWrapper({ children }) {
  return (
    <AnimatePresence mode="wait">
      <motion.div key={router.pathname} {...pageVariants}>
        {children}
      </motion.div>
    </AnimatePresence>
  );
}
```

### View Transitions API (Native Browser)
```js
// Wrap navigations in startViewTransition
document.querySelector('nav').addEventListener('click', async (e) => {
  if (!document.startViewTransition) return;  // fallback
  const transition = document.startViewTransition(() => {
    loadNewContent();
  });
  await transition.ready;
});
```
```css
::view-transition-old(root) {
  animation: 200ms ease-in fade-out;
}
::view-transition-new(root) {
  animation: 300ms ease-out fade-in;
}
/* Named transitions for shared elements */
.hero-image { view-transition-name: hero; }
```

### GSAP Page Transitions
```js
import gsap from 'gsap';
// Exit
const exitTl = gsap.timeline();
exitTl.to('.page-content', { opacity: 0, y: -20, duration: 0.2, ease: 'power2.in' })
      .to('.page-overlay', { scaleY: 1, transformOrigin: 'top', duration: 0.3, ease: 'power3.inOut' });
// Enter
const enterTl = gsap.timeline();
enterTl.from('.page-content', { opacity: 0, y: 20, duration: 0.3, ease: 'power2.out' })
       .to('.page-overlay', { scaleY: 0, transformOrigin: 'bottom', duration: 0.3 }, '<');
```

---

## 15. Loading & Skeleton States

### Skeleton Variants
```css
/* Text skeleton */
.skel-text {
  height: 1em;
  border-radius: 4px;
  width: 80%;
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted)/0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
/* Avatar skeleton */
.skel-avatar { width: 40px; height: 40px; border-radius: 50%; }
/* Card skeleton — combine text + image skeletons */
```

### Progressive Loading Pattern
```
1. Route shell (nav + page structure) — instant
2. Above-fold content skeleton — < 100ms
3. Critical data fetch + replace skeletons — 200–500ms
4. Below-fold content lazy-loaded on scroll
5. Non-critical animations after TTI
```

### Optimistic UI
```js
// Update UI before server confirms — rollback on error
async function likePost(postId) {
  setLiked(true);      // optimistic
  setCount(c => c + 1);
  try {
    await api.like(postId);
  } catch {
    setLiked(false);   // rollback
    setCount(c => c - 1);
    showToast('Failed to like post', 'error');
  }
}
```

---

## 16. Accessibility (A11y)

### Non-Negotiables
- All interactive elements reachable by keyboard (Tab / Shift+Tab)
- Visible focus indicator (never `outline: none` without replacement)
- Color is never the only means of conveying information
- Text alternatives for all non-decorative images (`alt` attribute)
- ARIA labels on icon-only buttons: `aria-label="Close dialog"`
- `role` attributes for custom interactive elements (sliders, comboboxes)
- Announce dynamic content with `aria-live="polite"` or `"assertive"`

### Focus Ring
```css
:focus-visible {
  outline: 2px solid hsl(var(--primary));
  outline-offset: 2px;
  border-radius: 4px;
}
:focus:not(:focus-visible) { outline: none; }
```

### Reduced Motion
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

### ARIA Quick Reference
```html
<!-- Icon button -->
<button aria-label="Delete item">🗑</button>

<!-- Live region (dynamic updates) -->
<div aria-live="polite" aria-atomic="true" class="sr-only" id="status"></div>

<!-- Modal -->
<div role="dialog" aria-modal="true" aria-labelledby="modal-title">

<!-- Tab panel -->
<div role="tablist">
  <button role="tab" aria-selected="true"  aria-controls="panel-1">Tab 1</button>
  <button role="tab" aria-selected="false" aria-controls="panel-2">Tab 2</button>
</div>
```

---

## 17. Responsive & Adaptive Design

### Breakpoint System
```css
/* Mobile-first (min-width) */
--bp-sm:  640px;   /* large phones */
--bp-md:  768px;   /* tablets */
--bp-lg:  1024px;  /* laptops */
--bp-xl:  1280px;  /* desktops */
--bp-2xl: 1536px;  /* wide screens */
```

### Fluid Typography (clamp)
```css
/* Scales smoothly from 320px to 1280px viewport */
h1    { font-size: clamp(2rem,   5vw, 4.5rem); }
h2    { font-size: clamp(1.5rem, 3vw, 3rem); }
body  { font-size: clamp(0.9rem, 1.2vw, 1rem); }
```

### Fluid Spacing
```css
.section { padding: clamp(48px, 8vw, 120px) clamp(16px, 5vw, 80px); }
```

### Mobile-Specific Rules
- Bottom navigation max 5 items, icons + labels
- Avoid hover-only interactions (no hover on touch)
- Minimum tap target 48×48px
- Avoid horizontal scroll on mobile
- Use `safe-area-inset-*` for notched devices

---

## 18. Design Systems & Tokens

### Token Structure (CSS Custom Properties)
```css
:root {
  /* Primitives */
  --color-violet-500: #8b5cf6;
  --radius-md: 8px;
  --spacing-4: 16px;
  --font-sans: 'Inter', system-ui, sans-serif;

  /* Semantic tokens */
  --color-primary:       hsl(252, 87%, 65%);
  --color-primary-hover: hsl(252, 87%, 58%);
  --color-background:    hsl(0, 0%, 100%);
  --color-surface:       hsl(0, 0%, 98%);
  --color-border:        hsl(220, 13%, 91%);
  --color-text:          hsl(222, 47%, 11%);
  --color-text-muted:    hsl(215, 16%, 47%);

  /* Component tokens */
  --btn-radius:  var(--radius-md);
  --btn-height:  40px;
  --card-radius: 12px;
  --card-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

[data-theme="dark"] {
  --color-background: hsl(222, 47%, 7%);
  --color-surface:    hsl(222, 47%, 11%);
  --color-border:     hsl(217, 33%, 17%);
  --color-text:       hsl(210, 40%, 98%);
  --color-text-muted: hsl(215, 20%, 65%);
}
```

### Component API Design Principles
1. Composable > Monolithic (prefer slot patterns)
2. Props for variants, not per-style overrides
3. Every component has: `size`, `variant`, `disabled`, `className` props
4. Controlled AND uncontrolled patterns
5. Forward refs for all DOM-wrapping components

---

## 19. Dark Mode

### Strategy: Semantic Tokens (Never hardcode dark colors)
```css
/* Use HSL for easy dark mode inversion */
:root {
  --bg:       0 0% 100%;
  --surface:  0 0% 98%;
  --border:   220 13% 91%;
  --text:     222 47% 11%;
}
.dark {
  --bg:       222 47% 7%;
  --surface:  222 47% 11%;
  --border:   217 33% 17%;
  --text:     210 40% 98%;
}
/* Usage */
.card { background: hsl(var(--surface)); border-color: hsl(var(--border)); }
```

### Dark Mode Toggle (React)
```jsx
function ThemeToggle() {
  const [dark, setDark] = useState(
    () => window.matchMedia('(prefers-color-scheme: dark)').matches
  );
  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark);
    localStorage.setItem('theme', dark ? 'dark' : 'light');
  }, [dark]);
  return <button onClick={() => setDark(d => !d)}>{dark ? '☀️' : '🌙'}</button>;
}
```

### Dark Mode Pitfalls
- Don't just invert — dark mode needs its own shadow system (glow instead of drop shadows)
- Images: don't filter; let images remain natural
- Reduce saturation slightly in dark mode (colors appear more vibrant on dark backgrounds)
- Test dark mode contrast independently — light mode passing ≠ dark mode passing

---

## 20. Data Visualization

### Chart Type Decision Tree
```
Comparison over time  → Line chart
Part of whole         → Donut / Pie (≤6 segments)
Category comparison   → Bar / Column chart
Distribution          → Histogram, Box plot
Correlation           → Scatter plot
Progress to goal      → Gauge, Progress ring
Single KPI            → Stat card with trend
Geographic data       → Choropleth map
Hierarchy             → Treemap, Sunburst
Flow / Process        → Sankey diagram
```

### Dashboard Anatomy
- **Hero row**: 3–4 KPI stat cards (large number + trend % + sparkline)
- **Primary chart**: Full-width or 8-col line/bar chart
- **Secondary charts**: 2-col grid of supporting metrics
- **Table**: Paginated data table below the fold
- **Filters**: Top bar (date range, segment) — always sticky

### Chart Color Palette (Sequential + Categorical)
```css
/* Categorical (up to 8 series) */
--chart-1: hsl(252, 87%, 65%);  /* violet */
--chart-2: hsl(201, 96%, 52%);  /* cyan */
--chart-3: hsl(142, 71%, 45%);  /* green */
--chart-4: hsl(38,  92%, 50%);  /* amber */
--chart-5: hsl(346, 87%, 65%);  /* rose */
--chart-6: hsl(21,  90%, 58%);  /* orange */
--chart-7: hsl(271, 91%, 65%);  /* purple */
--chart-8: hsl(162, 73%, 46%);  /* teal */
```

---

## 21. AR/VR & Immersive UX

### Core Spatial Design Principles
- **Comfort zone**: Primary content at 1.5–3m simulated distance
- **Field of view**: Critical info within 60° horizontal, 40° vertical
- **Text legibility**: Minimum 60px at 1m; always use high-contrast
- **Depth cues**: Occlusion, size, atmospheric, shadow, parallax
- **Locomotion**: Prefer teleportation over free movement (reduces discomfort)
- **Affordances**: Physical metaphors — grabbable objects look graspable

### WebXR (Browser AR/VR)
```js
async function initXR() {
  if (!navigator.xr) return;
  const supported = await navigator.xr.isSessionSupported('immersive-ar');
  if (supported) {
    const session = await navigator.xr.requestSession('immersive-ar', {
      requiredFeatures: ['hit-test', 'dom-overlay'],
      domOverlay: { root: document.getElementById('overlay') }
    });
  }
}
```

### Augmented Reality UI Overlay Rules
- Keep DOM overlay minimal — text, buttons only
- Semi-transparent backgrounds for legibility over unknown real-world scenes
- Large tap targets (60×60px minimum in AR)
- Never cover the center of the view with permanent UI

---

## 22. AI-Assisted Design Patterns

### Generative UI Principles
- Show AI thinking with skeleton + streaming text — never blank wait
- Uncertainty: use softer visual weight for AI-generated content
- Editable by default: every AI output has edit/regenerate affordance
- Confidence indicators: subtle border or badge showing model certainty
- Undo as a first-class feature beside every AI action

### AI Interaction Patterns
- **Inline suggestions**: Ghost text, accept with Tab
- **Command palette**: ⌘K shortcut, search-driven, AI actions listed
- **Chat overlay**: Slide-in panel, doesn't replace page context
- **Streaming text**: Typewriter effect for AI responses (build trust)
- **Diff view**: For AI edits — show before/after with accept/reject

### Prompt UX
- Placeholder text should demonstrate capability: "Ask me to redesign this section…"
- Character count or token budget indicator for long contexts
- Auto-resize textarea (up to 5 lines before scroll)
- Keyboard shortcuts: Enter to send, Shift+Enter for newline, Escape to close

---

## 23. Animation Libraries Reference

### GSAP (GreenSock)
```js
// Stagger reveal
gsap.from('.card', { y: 40, opacity: 0, stagger: 0.08, duration: 0.5, ease: 'power2.out' });
// ScrollTrigger
import ScrollTrigger from 'gsap/ScrollTrigger';
gsap.registerPlugin(ScrollTrigger);
gsap.from('.section', {
  y: 60, opacity: 0, duration: 0.8,
  scrollTrigger: { trigger: '.section', start: 'top 80%' }
});
```

### Framer Motion (React)
```jsx
<motion.div
  initial={{ opacity: 0, scale: 0.9 }}
  animate={{ opacity: 1, scale: 1 }}
  exit={{ opacity: 0, scale: 0.9 }}
  transition={{ type: 'spring', stiffness: 260, damping: 20 }}
/>
// Layout animations (FLIP auto)
<motion.div layout layoutId="card-1" />
```

### Lottie (JSON-based Vector Animation)
```js
import lottie from 'lottie-web';
const anim = lottie.loadAnimation({
  container: document.getElementById('lottie'),
  renderer: 'svg',
  loop: false,
  autoplay: true,
  path: '/animations/success.json',
});
// Control: anim.play(), anim.pause(), anim.setSpeed(1.5)
```
**Best for**: Illustration animations, success/error states, onboarding, icon animations.

### Rive (Runtime Interactive Animations)
```html
<canvas id="rive-canvas"></canvas>
<script type="module">
  import { Rive } from 'https://unpkg.com/@rive-app/canvas';
  const r = new Rive({
    src: 'hero.riv',
    canvas: document.getElementById('rive-canvas'),
    autoplay: true,
    stateMachines: 'MainSM',
    onLoad: () => r.resizeDrawingSurfaceToCanvas(),
  });
  // Trigger state: r.stateMachineInputs('MainSM').find(i => i.name === 'hover').value = true;
</script>
```
**Best for**: Interactive character animations, game-like UI, highly interactive motion.

### Anime.js
```js
import anime from 'animejs';
anime({ targets: '.el', translateX: 250, rotate: '1turn', duration: 800, easing: 'easeOutElastic(1, .8)' });
```

### Auto-Animate (Zero-config)
```js
import autoAnimate from '@formkit/auto-animate';
autoAnimate(document.getElementById('list'));
// Any child additions/removals/reorders now animate automatically
```

---

## 24. Performance Rules

### Animation Performance
- Only animate `transform` and `opacity` (compositor-only, no layout)
- Use `will-change: transform` sparingly — only on elements you KNOW will animate
- Prefer CSS animations over JS for simple loops
- Use `requestAnimationFrame` for all JS animations
- Throttle scroll handlers: `{ passive: true }` on all scroll event listeners
- Remove `will-change` after animation completes

### Image Optimization
```html
<img
  src="hero.webp"
  srcset="hero-480.webp 480w, hero-800.webp 800w, hero-1280.webp 1280w"
  sizes="(max-width: 640px) 100vw, 800px"
  loading="lazy"
  decoding="async"
  alt="Description"
/>
```

### CSS Containment
```css
.card { contain: layout style; }
.isolated-widget { contain: strict; }
```

### Core Web Vitals Targets
```
LCP (Largest Contentful Paint): < 2.5s
CLS (Cumulative Layout Shift):  < 0.1
INP (Interaction to Next Paint):< 200ms
```

---

## 25. Handoff & Implementation

### Designer → Developer Handoff Checklist
- [ ] All colors as tokens (never hardcoded hex)
- [ ] All text styles as named styles (H1, Body, Caption, etc.)
- [ ] All spacing as grid/token values
- [ ] Interactive states documented (hover, active, disabled, focus, error)
- [ ] Motion specs: duration, easing, delay for every animation
- [ ] Responsive breakpoint behavior shown (not just desktop)
- [ ] Dark mode variants for every component
- [ ] Accessibility annotations: focus order, ARIA roles, alt text
- [ ] Asset export: SVG for icons, WebP for images, JSON for Lottie

### Code Quality for UI
```
✅ Use design tokens (CSS variables), never raw values
✅ Components accept className prop for override
✅ Zero !important usage
✅ All animations respect prefers-reduced-motion
✅ All touch targets ≥ 44×44px
✅ All inputs have associated label
✅ All icons have aria-label or aria-hidden + adjacent text
✅ Color contrast tested in both light AND dark mode
```

### Recommended Tech Stack by Project
| Project | Framework | Styling | Animation | 3D |
|---|---|---|---|---|
| SaaS Web | Next.js 15 | Tailwind + shadcn/ui | Framer Motion | - |
| Marketing | Astro | Tailwind | GSAP + ScrollTrigger | Spline embed |
| Dashboard | React | Tailwind + Radix | Auto-animate | - |
| Creative Portfolio | Next.js | CSS Modules | GSAP + Lottie | Three.js / R3F |
| Mobile App | React Native | NativeWind | Reanimated 3 | - |
| Interactive Promo | Vanilla / Vite | CSS | GSAP | Three.js |
| Design Tool MVP | React | CSS-in-JS | Framer Motion | - |

---

*Skill version: 1.0 — Covers UI/UX fundamentals, all design styles, motion, 3D, cursor effects, micro-interactions, scroll animations, transitions, loading states, accessibility, responsive design, design systems, dark mode, data viz, AR/VR, AI patterns, animation libraries, and performance. For Cursor AI agent use.*
