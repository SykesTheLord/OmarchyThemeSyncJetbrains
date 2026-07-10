# Omarchy Theme Sync (JetBrains)

Keeps a JetBrains IDE's appearance in sync with the active [Omarchy](https://omarchy.org) theme.

When you run `omarchy-theme-set <theme>`, Omarchy rewrites
`~/.config/omarchy/current/theme/colors.toml` and swaps the whole `current/theme/` directory.
This plugin watches for that change and reskins the IDE to match — no restart, no manual step.

## What it does

- **Validates Omarchy first.** The plugin only activates when it detects a real Omarchy install
  (`$OMARCHY_PATH`/`~/.local/share/omarchy` + `version`, plus `~/.config/omarchy/current/theme.name`
  and a readable `colors.toml`). On any other machine it stays completely dormant.
- **Editor color scheme.** Clones Darcula (dark) or IntelliJ Light (light) and recolors editor
  chrome, the core syntax roles, and the 16 ANSI console/terminal colors directly from the palette.
- **IDE UI tint.** Switches the base look-and-feel to match Omarchy's light/dark mode and overlays
  the theme's surface/selection/border/accent colors onto common UI components.
- **Live sync.** A background file watcher reapplies within ~1s of every theme switch.

## Detection & color source (Omarchy 3.8.x)

| Purpose            | Path |
|--------------------|------|
| Install root       | `$OMARCHY_PATH` or `~/.local/share/omarchy` (must contain `version`) |
| Active theme name  | `~/.config/omarchy/current/theme.name` |
| Colors             | `~/.config/omarchy/current/theme/colors.toml` |
| Light-mode marker  | `~/.config/omarchy/current/theme/light.mode` (present ⇒ light) |

## Settings

**Settings ▸ Appearance & Behavior ▸ Omarchy Theme Sync**: enable/disable, toggle editor-scheme
vs. UI-theme syncing, toggle apply-on-startup, view detection status, and "Apply now".

## Build & run

Requires JDK 21.

```bash
# Build the installable plugin zip -> build/distributions/
./gradlew buildPlugin

# Launch a sandbox IDE with the plugin loaded
./gradlew runIde
```

To try it live, run the sandbox IDE and switch themes in another terminal:

```bash
omarchy-theme-set "Tokyo Night"        # dark
omarchy-theme-set "Catppuccin Latte"   # light
omarchy-theme-set "Lumon"
```

## Install

See **[docs/INSTALL.md](docs/INSTALL.md)** for full instructions (build + all install methods +
a per-IDE directory table for every JetBrains IDE).

Quick options:

```bash
# Build the zip and install into a chosen IDE in one step
scripts/install.sh Rider2026.1     # or: IntelliJIdea | all | (no arg for a menu)
```

Or install the built `build/distributions/*.zip` manually via
**Settings ▸ Plugins ▸ ⚙ ▸ Install Plugin from Disk…**.

## Notes / limitations

- The editor scheme and the light/dark switch are the reliable core. The full-UI tint is
  best-effort: some IntelliJ surfaces compute their own colors, so a small number may not recolor
  perfectly. Every step is guarded — if the UI tint fails, the editor scheme and dark/light switch
  still apply.
- Linux only (Omarchy is Arch + Hyprland).
