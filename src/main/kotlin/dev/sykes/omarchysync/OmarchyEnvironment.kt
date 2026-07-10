package dev.sykes.omarchysync

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Resolves and validates that the current machine is running Omarchy.
 *
 * The plugin must stay completely dormant on non-Omarchy systems, so this is the single
 * gate every feature is guarded behind. Validation mirrors how Omarchy itself locates its
 * install (the `OMARCHY_PATH` env var and the `~/.config/omarchy` tree) as of Omarchy 3.8.x.
 */
data class OmarchyInstall(
    /** Root of the Omarchy code install (e.g. ~/.local/share/omarchy). */
    val root: Path,
    /** ~/.config/omarchy/current/theme.name — the active theme name (lowercase-kebab). */
    val themeNamePath: Path,
    /** ~/.config/omarchy/current/theme — the swapped-in theme config directory. */
    val currentThemeDir: Path,
    /** ~/.config/omarchy/current/theme/colors.toml — canonical color source. */
    val colorsTomlPath: Path,
    /** ~/.config/omarchy/current/theme/light.mode — present only for light themes. */
    val lightModePath: Path,
    /** ~/.config/omarchy/current — the directory watched for theme changes. */
    val currentDir: Path,
) {
    fun readThemeName(): String =
        runCatching { Files.readString(themeNamePath).trim() }.getOrDefault("unknown").ifEmpty { "unknown" }

    fun isLight(): Boolean = Files.exists(lightModePath)
}

object OmarchyEnvironment {

    /** Human-readable reason describing why detection failed, for the settings UI. */
    @Volatile
    var lastFailureReason: String = "Not checked yet"
        private set

    /**
     * Returns a validated [OmarchyInstall] or null when this machine is not running Omarchy.
     *
     * All three signals must hold: an Omarchy code root with a `version` file, an existing
     * `theme.name`, and a readable `colors.toml` (the file the plugin actually consumes).
     */
    fun detect(): OmarchyInstall? {
        if (!SystemInfo.isLinux) {
            lastFailureReason = "Omarchy is Linux-only; current OS is not supported."
            return null
        }

        val root = resolveRoot()
        if (root == null || !Files.isRegularFile(root.resolve("version"))) {
            lastFailureReason =
                "Omarchy install not found (checked \$OMARCHY_PATH and ~/.local/share/omarchy)."
            return null
        }

        val home = userHome() ?: run {
            lastFailureReason = "Could not resolve the user home directory."
            return null
        }
        val currentDir = home.resolve(".config/omarchy/current")
        val themeNamePath = currentDir.resolve("theme.name")
        val themeDir = currentDir.resolve("theme")
        val colorsToml = themeDir.resolve("colors.toml")

        if (!Files.isRegularFile(themeNamePath)) {
            lastFailureReason = "No active Omarchy theme (~/.config/omarchy/current/theme.name missing)."
            return null
        }
        if (!Files.isReadable(colorsToml)) {
            lastFailureReason = "Omarchy theme colors not found (${colorsToml} unreadable)."
            return null
        }

        lastFailureReason = ""
        return OmarchyInstall(
            root = root,
            themeNamePath = themeNamePath,
            currentThemeDir = themeDir,
            colorsTomlPath = colorsToml,
            lightModePath = themeDir.resolve("light.mode"),
            currentDir = currentDir,
        )
    }

    private fun resolveRoot(): Path? {
        System.getenv("OMARCHY_PATH")?.takeIf { it.isNotBlank() }?.let { envPath ->
            val p = Paths.get(envPath)
            if (Files.isDirectory(p)) return p
        }
        val home = userHome() ?: return null
        val fallback = home.resolve(".local/share/omarchy")
        return if (Files.isDirectory(fallback)) fallback else null
    }

    private fun userHome(): Path? =
        System.getProperty("user.home")?.takeIf { it.isNotBlank() }?.let { Paths.get(it) }
}
