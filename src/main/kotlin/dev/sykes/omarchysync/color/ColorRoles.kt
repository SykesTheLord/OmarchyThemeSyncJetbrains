package dev.sykes.omarchysync.color

import dev.sykes.omarchysync.OmarchyTheme
import java.awt.Color

/**
 * Maps an Omarchy 16-color ANSI palette to the semantic roles the editor scheme and UI theme
 * consume. Centralizing the mapping keeps the editor and UI in visual agreement and gives one
 * place to tune how terminal palettes translate into IDE surfaces.
 */
class ColorRoles(val theme: OmarchyTheme) {

    // --- Base surfaces -------------------------------------------------------
    val background: Color get() = theme.background
    val foreground: Color get() = theme.foreground
    val caret: Color get() = theme.cursor
    val selectionBackground: Color get() = theme.selectionBackground
    val selectionForeground: Color get() = theme.selectionForeground
    val accent: Color get() = theme.accent

    // --- Syntax roles (conventional ANSI assignments) ------------------------
    val keyword: Color get() = theme.color(5)          // magenta
    val string: Color get() = theme.color(2)           // green
    val number: Color get() = theme.color(4)           // blue
    val comment: Color get() = dim(theme.color(8))     // bright black, softened
    val function: Color get() = theme.color(4)         // blue
    val type: Color get() = theme.color(3)             // yellow
    val constant: Color get() = theme.color(6)         // cyan
    val operator: Color get() = theme.foreground
    val error: Color get() = theme.color(1)            // red
    val warning: Color get() = theme.color(3)          // yellow
    val metadata: Color get() = theme.color(6)         // cyan (annotations)

    // --- Editor chrome -------------------------------------------------------
    val lineNumber: Color get() = blend(theme.color(8), background, 0.30f)
    val lineNumberCurrent: Color get() = theme.foreground
    val caretRow: Color get() = blend(background, foreground, 0.06f)
    val gutterBackground: Color get() = background
    val indentGuide: Color get() = blend(background, foreground, 0.12f)

    // --- UI surfaces (derived shades so unpatched-but-related widgets cohere) -
    val panelBackground: Color get() = background
    val lighterBackground: Color get() = blend(background, foreground, 0.05f)
    val darkerBackground: Color get() = if (theme.isLight) blend(background, Color.BLACK, 0.04f)
    else blend(background, Color.BLACK, 0.20f)
    val border: Color get() = blend(background, foreground, 0.18f)
    val hover: Color get() = blend(background, foreground, 0.10f)
    val disabledForeground: Color get() = blend(foreground, background, 0.45f)

    /** Relative luminance (0..1) of the background; secondary guard for light/dark decisions. */
    fun backgroundLuminance(): Double = luminance(background)

    private fun dim(c: Color): Color = blend(c, background, if (theme.isLight) 0.10f else 0.15f)

    companion object {
        /** Linear interpolation of two colors; [t] is the weight toward [b]. */
        fun blend(a: Color, b: Color, t: Float): Color {
            val u = t.coerceIn(0f, 1f)
            fun mix(x: Int, y: Int) = Math.round(x + (y - x) * u).coerceIn(0, 255)
            return Color(mix(a.red, b.red), mix(a.green, b.green), mix(a.blue, b.blue))
        }

        fun luminance(c: Color): Double {
            fun chan(v: Int): Double {
                val s = v / 255.0
                return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * chan(c.red) + 0.7152 * chan(c.green) + 0.0722 * chan(c.blue)
        }
    }
}
