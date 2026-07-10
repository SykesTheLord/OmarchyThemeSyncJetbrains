package dev.sykes.omarchysync

import java.awt.Color

/**
 * Immutable snapshot of a parsed Omarchy theme.
 *
 * Colors come from `colors.toml`; [isLight] comes from the presence of the `light.mode`
 * marker file. Accessors fall back to foreground/background so a theme missing an optional
 * key still yields a usable scheme rather than throwing.
 */
data class OmarchyTheme(
    val name: String,
    val isLight: Boolean,
    /** Raw key -> "#rrggbb" map as read from colors.toml. */
    val colors: Map<String, Color>,
) {
    val background: Color get() = colors["background"] ?: BLACK
    val foreground: Color get() = colors["foreground"] ?: WHITE
    val cursor: Color get() = colors["cursor"] ?: foreground
    val accent: Color get() = colors["accent"] ?: color(5)
    val selectionBackground: Color get() = colors["selection_background"] ?: color(8)
    val selectionForeground: Color get() = colors["selection_foreground"] ?: foreground
    val activeBorder: Color get() = colors["active_border_color"] ?: accent
    val activeTabBackground: Color get() = colors["active_tab_background"] ?: color(4)

    /** ANSI color N (0..15), falling back to foreground when absent. */
    fun color(index: Int): Color = colors["color$index"] ?: foreground

    companion object {
        private val BLACK = Color(0x16, 0x24, 0x2d)
        private val WHITE = Color(0xd6, 0xe2, 0xee)
    }
}
