# 🌐 Nyro Browser

A lightweight desktop browser built with **Tauri (Rust)** + **SolidJS** + **TypeScript** + **Tailwind CSS**, replicating Google Chrome's UI/UX while being optimized for low-end hardware.

---

## 📁 Project Structure

```
nyro-browser/
├── .github/
│   └── workflows/
│       └── release.yml          ← CI/CD pipeline (all platforms)
├── src/
│   ├── App.tsx                  ← Main SolidJS UI (tabs, toolbar, omnibox)
│   ├── index.css                ← Tailwind + Chrome palette + tab shapes
│   └── main.tsx                 ← SolidJS render entry point
├── src-tauri/
│   ├── src/
│   │   └── main.rs              ← Rust backend (drag, URL resolve, window ctrl)
│   ├── Cargo.toml               ← Rust dependencies
│   └── tauri.conf.json          ← Tauri config (frameless, user-agent, etc.)
├── index.html
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Node.js | ≥ 20 |
| Rust | stable (via rustup) |
| Tauri CLI | `npm install -g @tauri-apps/cli` |
| Windows | WebView2 runtime (pre-installed on Win11) |
| Linux | `libwebkit2gtk-4.1-dev`, `libgtk-3-dev` |

### Install & Run (dev)

```bash
# 1. Install Node dependencies
npm install

# 2. Start dev server (hot-reload SolidJS + Tauri window)
npm run tauri dev
```

### Build for production

```bash
npm run tauri build
# Output: src-tauri/target/release/bundle/
```

---

## 🏗️ CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/release.yml`) will:

1. **Trigger** on push to `main` (nightly draft) or a `v*.*.*` tag (release draft).
2. **Build** on Windows, macOS (Intel + ARM), and Ubuntu in parallel.
3. **Cache** Rust and Node dependencies for ~60% faster subsequent builds.
4. **Create** a draft GitHub Release with all installers attached.
5. **Publish** manually by editing and publishing the draft.

### Releasing a new version

```bash
git tag v1.2.0
git push origin v1.2.0
# → GitHub Actions builds all platforms and creates a draft release
```

### Optional Secrets (for code signing)

| Secret | Purpose |
|--------|---------|
| `APPLE_CERTIFICATE` | macOS code signing certificate (base64) |
| `APPLE_CERTIFICATE_PASSWORD` | Certificate password |
| `APPLE_SIGNING_IDENTITY` | e.g. "Developer ID Application: ..." |
| `APPLE_ID` | Your Apple ID email |
| `APPLE_PASSWORD` | App-specific password |
| `APPLE_TEAM_ID` | Your Apple Developer Team ID |
| `TAURI_SIGNING_PRIVATE_KEY` | Windows updater signing key |

---

## 🎨 UI Features

- **Trapezoidal tabs** — SVG-based `<path>` trapezoid shapes, perspective-accurate
- **Active/inactive tab colors** — `#ffffff` (active) vs `#dee1e6` (frame), matching Chrome exactly
- **Omnibox** — Smart URL vs Google Search detection, focus ring, lock icon
- **Custom title bar** — Frameless window with native-feeling minimize/maximize/close
- **Loading progress bar** — Animated blue progress stripe on navigation
- **Window drag** — `start_drag` Tauri command bound to the tab strip area

---

## 🦀 Rust Commands

| Command | Description |
|---------|-------------|
| `start_drag` | Initiates native window drag from frameless title bar |
| `resolve_url(input)` | Converts omnibox input → URL or Google Search URL |
| `get_version()` | Returns app version string |
| `minimize_window` | Minimizes the main window |
| `toggle_maximize` | Toggles maximize/restore |
| `close_window` | Closes the application |

---

## 📄 License

MIT © Nyro Browser
