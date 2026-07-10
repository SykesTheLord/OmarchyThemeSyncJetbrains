package dev.sykes.omarchysync.scheme

import com.intellij.execution.process.ConsoleHighlighter
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import dev.sykes.omarchysync.OmarchyTheme
import dev.sykes.omarchysync.color.ColorRoles
import java.awt.Color
import java.awt.Font

/**
 * Builds an editor color scheme from an Omarchy palette by cloning a platform base scheme
 * (Darcula for dark themes, IntelliJ Light for light) and overriding editor chrome, the core
 * syntax roles, and the 16 ANSI console/terminal colors.
 *
 * Must be invoked on the EDT — it changes the global editor scheme.
 */
object EditorSchemeGenerator {

    private val LOG = logger<EditorSchemeGenerator>()
    const val SCHEME_PREFIX = "Omarchy"

    fun schemeName(theme: OmarchyTheme): String = "$SCHEME_PREFIX: ${theme.name}"

    fun apply(theme: OmarchyTheme) {
        val manager = EditorColorsManager.getInstance()
        val baseName = if (theme.isLight) "IntelliJ Light" else "Darcula"
        val base = manager.getScheme(baseName) ?: manager.globalScheme
        val scheme = base.clone() as EditorColorsScheme
        scheme.name = schemeName(theme)

        val roles = ColorRoles(theme)
        applyEditorChrome(scheme, roles)
        applySyntax(scheme, roles)
        applyConsole(scheme, theme)

        manager.addColorScheme(scheme)
        manager.setGlobalScheme(scheme)
        LOG.info("Applied Omarchy editor scheme '${scheme.name}' (light=${theme.isLight})")
    }

    private fun applyEditorChrome(scheme: EditorColorsScheme, roles: ColorRoles) {
        // Default text: drives editor background/foreground for un-highlighted text.
        scheme.setAttributes(
            HighlighterColors.TEXT,
            TextAttributes(roles.foreground, roles.background, null, null, Font.PLAIN),
        )

        val colorKeys = mapOf(
            EditorColors.CARET_COLOR to roles.caret,
            EditorColors.CARET_ROW_COLOR to roles.caretRow,
            EditorColors.SELECTION_BACKGROUND_COLOR to roles.selectionBackground,
            EditorColors.SELECTION_FOREGROUND_COLOR to roles.selectionForeground,
            EditorColors.GUTTER_BACKGROUND to roles.gutterBackground,
            EditorColors.LINE_NUMBERS_COLOR to roles.lineNumber,
            EditorColors.TEARLINE_COLOR to roles.border,
            EditorColors.INDENT_GUIDE_COLOR to roles.indentGuide,
            EditorColors.RIGHT_MARGIN_COLOR to roles.border,
            EditorColors.WHITESPACES_COLOR to roles.indentGuide,
        )
        for ((key, color) in colorKeys) scheme.setColor(key, color)

        // Current line number gets the full foreground for contrast against dimmed siblings.
        runCatching { scheme.setColor(EditorColors.LINE_NUMBER_ON_CARET_ROW_COLOR, roles.lineNumberCurrent) }
    }

    private fun applySyntax(scheme: EditorColorsScheme, roles: ColorRoles) {
        fun set(key: TextAttributesKey, color: Color, style: Int = Font.PLAIN, effect: Color? = null) {
            scheme.setAttributes(
                key,
                TextAttributes(color, null, effect, effect?.let { EffectType.LINE_UNDERSCORE }, style),
            )
        }

        set(DefaultLanguageHighlighterColors.KEYWORD, roles.keyword, Font.BOLD)
        set(DefaultLanguageHighlighterColors.STRING, roles.string)
        set(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE, roles.constant)
        set(DefaultLanguageHighlighterColors.NUMBER, roles.number)
        set(DefaultLanguageHighlighterColors.LINE_COMMENT, roles.comment, Font.ITALIC)
        set(DefaultLanguageHighlighterColors.BLOCK_COMMENT, roles.comment, Font.ITALIC)
        set(DefaultLanguageHighlighterColors.DOC_COMMENT, roles.comment, Font.ITALIC)
        set(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION, roles.function)
        set(DefaultLanguageHighlighterColors.FUNCTION_CALL, roles.function)
        set(DefaultLanguageHighlighterColors.CLASS_NAME, roles.type)
        set(DefaultLanguageHighlighterColors.CLASS_REFERENCE, roles.type)
        set(DefaultLanguageHighlighterColors.INTERFACE_NAME, roles.type)
        set(DefaultLanguageHighlighterColors.CONSTANT, roles.constant)
        set(DefaultLanguageHighlighterColors.STATIC_FIELD, roles.constant)
        set(DefaultLanguageHighlighterColors.INSTANCE_FIELD, roles.foreground)
        set(DefaultLanguageHighlighterColors.LOCAL_VARIABLE, roles.foreground)
        set(DefaultLanguageHighlighterColors.PARAMETER, roles.foreground)
        set(DefaultLanguageHighlighterColors.IDENTIFIER, roles.foreground)
        set(DefaultLanguageHighlighterColors.OPERATION_SIGN, roles.operator)
        set(DefaultLanguageHighlighterColors.BRACES, roles.operator)
        set(DefaultLanguageHighlighterColors.BRACKETS, roles.operator)
        set(DefaultLanguageHighlighterColors.PARENTHESES, roles.operator)
        set(DefaultLanguageHighlighterColors.DOT, roles.operator)
        set(DefaultLanguageHighlighterColors.COMMA, roles.operator)
        set(DefaultLanguageHighlighterColors.SEMICOLON, roles.operator)
        set(DefaultLanguageHighlighterColors.METADATA, roles.metadata)
        set(DefaultLanguageHighlighterColors.MARKUP_TAG, roles.keyword)
        set(DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE, roles.type)
        set(DefaultLanguageHighlighterColors.MARKUP_ENTITY, roles.constant)
    }

    private fun applyConsole(scheme: EditorColorsScheme, theme: OmarchyTheme) {
        // ConsoleHighlighter ANSI slots are TextAttributesKeys; set their foreground color.
        val ansi: List<Pair<TextAttributesKey, Int>> = listOf(
            ConsoleHighlighter.BLACK to 0,
            ConsoleHighlighter.RED to 1,
            ConsoleHighlighter.GREEN to 2,
            ConsoleHighlighter.YELLOW to 3,
            ConsoleHighlighter.BLUE to 4,
            ConsoleHighlighter.MAGENTA to 5,
            ConsoleHighlighter.CYAN to 6,
            ConsoleHighlighter.GRAY to 7,
            ConsoleHighlighter.DARKGRAY to 8,
            ConsoleHighlighter.RED_BRIGHT to 9,
            ConsoleHighlighter.GREEN_BRIGHT to 10,
            ConsoleHighlighter.YELLOW_BRIGHT to 11,
            ConsoleHighlighter.BLUE_BRIGHT to 12,
            ConsoleHighlighter.MAGENTA_BRIGHT to 13,
            ConsoleHighlighter.CYAN_BRIGHT to 14,
            ConsoleHighlighter.WHITE to 15,
        )
        for ((key, idx) in ansi) runCatching {
            scheme.setAttributes(key, TextAttributes(theme.color(idx), null, null, null, Font.PLAIN))
        }
    }
}
