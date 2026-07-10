package dev.sykes.omarchysync

import java.awt.Color
import java.nio.file.Files

/**
 * Parses Omarchy's `colors.toml`, which is a flat `key = "#rrggbb"` file (no nested tables).
 * A hand-rolled line scanner avoids pulling in a TOML dependency for this stable, simple format.
 */
object OmarchyColorsParser {

    private val LINE = Regex("""^\s*([A-Za-z0-9_]+)\s*=\s*"?(#[0-9a-fA-F]{6})"?\s*$""")

    fun parse(install: OmarchyInstall): OmarchyTheme? {
        val text = runCatching { Files.readString(install.colorsTomlPath) }.getOrNull() ?: return null
        val colors = LinkedHashMap<String, Color>()
        for (raw in text.lineSequence()) {
            val m = LINE.find(raw.trim()) ?: continue
            val key = m.groupValues[1]
            val color = parseHex(m.groupValues[2]) ?: continue
            colors[key] = color
        }
        if (colors.isEmpty()) return null
        return OmarchyTheme(
            name = install.readThemeName(),
            isLight = install.isLight(),
            colors = colors,
        )
    }

    fun parseHex(hex: String): Color? {
        val h = hex.removePrefix("#")
        if (h.length != 6) return null
        return runCatching {
            Color(h.substring(0, 2).toInt(16), h.substring(2, 4).toInt(16), h.substring(4, 6).toInt(16))
        }.getOrNull()
    }
}
