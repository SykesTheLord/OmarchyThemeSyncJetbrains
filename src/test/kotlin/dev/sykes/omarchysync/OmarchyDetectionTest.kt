package dev.sykes.omarchysync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Color

/**
 * Exercises the pure logic — the Omarchy-detection gate and the colors.toml parser — against the
 * real system, without booting an IDE. On a non-Omarchy machine the detection assertions are
 * skipped (detect() legitimately returns null there), but the parser and hex tests always run.
 */
class OmarchyDetectionTest {

    @Test
    fun `hex parsing handles Omarchy format`() {
        assertEquals(Color(0x16, 0x24, 0x2d), OmarchyColorsParser.parseHex("#16242d"))
        assertEquals(Color(0xff, 0xff, 0xff), OmarchyColorsParser.parseHex("#FFFFFF"))
        assertNull(OmarchyColorsParser.parseHex("#zzz"))
        assertNull(OmarchyColorsParser.parseHex("16242d-notlen6plushash"))
    }

    @Test
    fun `detect and parse the live Omarchy theme when present`() {
        val install = OmarchyEnvironment.detect()
        if (install == null) {
            println("Omarchy not detected (${OmarchyEnvironment.lastFailureReason}); skipping live checks.")
            return
        }

        val theme = OmarchyColorsParser.parse(install)
        assertNotNull("colors.toml should parse on an Omarchy machine", theme)
        theme!!

        assertTrue("theme name should be non-empty", theme.name.isNotBlank())
        // Every Omarchy palette defines background/foreground and the 16 ANSI slots.
        assertNotNull(theme.background)
        assertNotNull(theme.foreground)
        for (i in 0..15) assertNotNull("color$i must be present", theme.color(i))
        println("Detected Omarchy theme '${theme.name}' (light=${theme.isLight}), ${theme.colors.size} colors parsed")
    }
}
