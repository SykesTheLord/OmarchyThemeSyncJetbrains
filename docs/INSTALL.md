# Installing Omarchy Theme Sync

This guide covers building the plugin and installing it into any JetBrains IDE.
There are three ways to install — pick one:

- **[A. Install script](#a-install-script-fastest)** — build + install in one command (recommended).
- **[B. IDE GUI](#b-from-the-ide-gui-works-in-every-jetbrains-ide)** — "Install Plugin from Disk". Identical steps in every IDE.
- **[C. Manual copy](#c-manual-copy-per-ide-directories)** — drop the folder into the IDE's plugins dir.

> **Requirements:** JDK 21 to build; an IDE on build **2024.3 or newer** (`since-build=243`, no upper
> bound). The plugin only *activates* on an Omarchy machine — it stays dormant elsewhere.

---

## Build the zip

The installable artifact is produced by Gradle:

```bash
cd /home/jacob/Projects/OmarchyThemeSyncJetbrains
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew buildPlugin
# -> build/distributions/omarchy-theme-sync-0.1.0.zip
```

The zip contains a single top-level folder `omarchy-theme-sync/` — that is what gets installed.

---

## A. Install script (fastest)

`scripts/install.sh` builds the zip (unless `--no-build`) and copies it into the plugins directory
of the IDE(s) you name. It discovers your installed IDEs from `~/.config/JetBrains`.

```bash
scripts/install.sh                 # interactive menu of detected IDEs
scripts/install.sh Rider2026.1     # one specific IDE
scripts/install.sh IntelliJIdea    # every installed IntelliJ IDEA version
scripts/install.sh all             # every detected JetBrains IDE
scripts/install.sh --no-build all  # reuse the existing zip, skip the Gradle build
```

Then **fully restart** the target IDE. Verify under **Settings ▸ Plugins ▸ Installed** and
**Settings ▸ Appearance & Behavior ▸ Omarchy Theme Sync**.

---

## B. From the IDE GUI (works in every JetBrains IDE)

These steps are **identical** for IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, Rider, CLion, GoLand,
RubyMine, DataGrip, DataSpell, RustRover, Aqua, and MPS.

1. Open the IDE.
2. **File ▸ Settings** (Linux/Windows shortcut **Ctrl+Alt+S**; macOS: **⌘,**).
3. Select **Plugins** in the left panel.
4. Click the **⚙ gear icon** at the top ▸ **Install Plugin from Disk…**
5. Choose `build/distributions/omarchy-theme-sync-0.1.0.zip`.
6. Click **OK**, then **Restart IDE** when prompted.

After restart, open **Settings ▸ Appearance & Behavior ▸ Omarchy Theme Sync** to see the detection
status and toggles. Switch an Omarchy theme (`omarchy-theme-set <name>`) to watch it sync live.

> **Android Studio** uses the same dialog and shortcut, but is built on an older platform branch —
> if it refuses to load, it is below build 243. See [compatibility](#compatibility--troubleshooting).

---

## C. Manual copy (per-IDE directories)

On Linux, each JetBrains IDE loads externally-installed plugins from:

```
~/.local/share/JetBrains/<Product><Version>/
```

Install by extracting the zip there (creates `<Product><Version>/omarchy-theme-sync/`):

```bash
IDE="Rider2026.1"   # change to your IDE (see table below)
DEST="$HOME/.local/share/JetBrains/$IDE"
mkdir -p "$DEST"
rm -rf "$DEST/omarchy-theme-sync"
unzip -o build/distributions/omarchy-theme-sync-0.1.0.zip -d "$DEST"
# then restart the IDE
```

### Directory token per IDE

`<Version>` looks like `2026.1`. To see exactly what you have installed:
`ls ~/.config/JetBrains`.

| JetBrains IDE            | `<Product>` token   | Example plugins path                              |
|-------------------------|---------------------|---------------------------------------------------|
| IntelliJ IDEA Ultimate  | `IntelliJIdea`      | `~/.local/share/JetBrains/IntelliJIdea2026.1/`    |
| IntelliJ IDEA Community  | `IdeaIC`            | `~/.local/share/JetBrains/IdeaIC2026.1/`          |
| PyCharm Professional     | `PyCharm`           | `~/.local/share/JetBrains/PyCharm2026.1/`         |
| PyCharm Community        | `PyCharmCE`         | `~/.local/share/JetBrains/PyCharmCE2026.1/`       |
| WebStorm                 | `WebStorm`          | `~/.local/share/JetBrains/WebStorm2026.1/`        |
| PhpStorm                 | `PhpStorm`          | `~/.local/share/JetBrains/PhpStorm2026.1/`        |
| Rider                    | `Rider`             | `~/.local/share/JetBrains/Rider2026.1/`           |
| CLion                    | `CLion`             | `~/.local/share/JetBrains/CLion2026.1/`           |
| GoLand                   | `GoLand`            | `~/.local/share/JetBrains/GoLand2026.1/`          |
| RubyMine                 | `RubyMine`          | `~/.local/share/JetBrains/RubyMine2026.1/`        |
| DataGrip                 | `DataGrip`          | `~/.local/share/JetBrains/DataGrip2026.1/`        |
| DataSpell                | `DataSpell`         | `~/.local/share/JetBrains/DataSpell2026.1/`       |
| RustRover                | `RustRover`         | `~/.local/share/JetBrains/RustRover2026.1/`       |
| Aqua                     | `Aqua`              | `~/.local/share/JetBrains/Aqua2026.1/`            |
| MPS                      | `MPS`               | `~/.local/share/JetBrains/MPS2026.1/`             |
| Android Studio¹          | `AndroidStudio`     | `~/.local/share/Google/AndroidStudio2026.1/`      |

¹ Android Studio lives under `~/.local/share/Google/` (not `JetBrains/`) and tracks a different
platform build; install only if its build is ≥ 243.

---

## Verifying

1. Restart the IDE.
2. **Settings ▸ Plugins ▸ Installed** — "Omarchy Theme Sync" should be listed and enabled.
3. **Settings ▸ Appearance & Behavior ▸ Omarchy Theme Sync** — should read
   *"Omarchy detected. Active theme: `<name>` (dark/light)."*
4. Run `omarchy-theme-set "Tokyo Night"` in a terminal; the IDE editor + UI should re-skin within ~1s.

---

## Updating

Rebuild and reinstall the same way (script re-run, or GUI "Install Plugin from Disk" again, or
re-extract). The install script removes the old `omarchy-theme-sync` folder before copying. Restart
the IDE to load the new version.

## Uninstalling

- GUI: **Settings ▸ Plugins ▸ Installed ▸** Omarchy Theme Sync ▸ **Uninstall**, then restart.
- Manual: `rm -rf ~/.local/share/JetBrains/<Product><Version>/omarchy-theme-sync` and restart.

---

## Compatibility / troubleshooting

- **Plugin not listed after install:** you likely copied into the wrong `<Product><Version>` dir.
  Confirm the exact name with `ls ~/.config/JetBrains`, and note the plugins dir is under
  `~/.local/share/JetBrains/` (not `~/.config/`).
- **"Plugin is incompatible" / not loading:** the IDE build is older than 243 (pre-2024.3). Upgrade
  the IDE, or lower `pluginSinceBuild` in `gradle.properties` and rebuild.
- **Status says "Omarchy not detected":** expected on non-Omarchy machines — the plugin intentionally
  stays dormant. The status line shows the exact reason.
- **Editor updates but some UI surfaces don't recolor:** the full-UI tint is best-effort; a few
  IntelliJ surfaces compute their own colors. The editor scheme and light/dark switch always apply.
- **Must restart:** JetBrains loads disk-installed plugins at startup. A running IDE won't pick up a
  freshly copied plugin until restarted.
