package dev.sykes.omarchysync.ui

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.openapi.diagnostic.logger
import dev.sykes.omarchysync.OmarchyTheme
import dev.sykes.omarchysync.color.ColorRoles
import java.awt.Color
import java.awt.Window
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource

/**
 * Retints the IDE frame from an Omarchy palette.
 *
 * Two layers, most-reliable first:
 *  1. Switch the base look-and-feel to a dark/light theme matching Omarchy's light.mode, so
 *     computed platform colors (the majority of the UI) stay coherent.
 *  2. Overlay a curated set of Omarchy colors onto the shared UIManager defaults and repaint
 *     live windows, tinting the common surfaces (panels, trees, tables, tabs, borders, popups).
 *
 * The overlay uses [ColorUIResource] so Swing's `updateComponentTreeUI` recognizes the values as
 * installable defaults. This is best-effort by nature — some IntelliJ surfaces compute their own
 * colors — so every step is guarded and failures degrade to "editor + dark/light only".
 *
 * Must run on the EDT.
 */
object UiThemeApplier {

    private val LOG = logger<UiThemeApplier>()

    fun apply(theme: OmarchyTheme) {
        val roles = ColorRoles(theme)
        switchBaseLaf(theme)
        overlayDefaults(roles)
        refreshWindows()
        LOG.info("Applied Omarchy UI tint for '${theme.name}' (light=${theme.isLight})")
    }

    /** Selects Darcula (dark) or IntelliJ Light among installed LAFs, if not already current. */
    private fun switchBaseLaf(theme: OmarchyTheme) {
        val lafManager = LafManager.getInstance()
        val wanted = if (theme.isLight) LIGHT_NAMES else DARK_NAMES
        val current = runCatching { lafManager.currentUIThemeLookAndFeel?.name }.getOrNull()
        if (current != null && wanted.any { it.equals(current, ignoreCase = true) }) return

        val target = lafManager.installedLookAndFeels
            .filterIsInstance<UIThemeLookAndFeelInfo>()
            .firstOrNull { info -> wanted.any { it.equals(info.name, ignoreCase = true) } }
            ?: run {
            LOG.warn("No installed LAF matched ${wanted.joinToString()}; keeping current LAF")
            return
        }
        runCatching { lafManager.setCurrentLookAndFeel(target, true) }
            .onFailure { LOG.warn("Failed to switch LAF to ${target.name}", it) }
    }

    private fun overlayDefaults(roles: ColorRoles) {
        val defaults = UIManager.getDefaults()
        fun put(key: String, color: Color) {
            defaults[key] = ColorUIResource(color)
        }

        // Base surfaces
        for (key in BACKGROUND_KEYS) put(key, roles.panelBackground)
        for (key in FOREGROUND_KEYS) put(key, roles.foreground)
        for (key in DARKER_BACKGROUND_KEYS) put(key, roles.darkerBackground)
        for (key in LIGHTER_BACKGROUND_KEYS) put(key, roles.lighterBackground)

        // Selection
        for (key in SELECTION_BG_KEYS) put(key, roles.selectionBackground)
        for (key in SELECTION_FG_KEYS) put(key, roles.selectionForeground)

        // Borders / separators
        for (key in BORDER_KEYS) put(key, roles.border)

        // Accent-ish surfaces
        put("Link.activeForeground", roles.accent)
        put("Component.focusColor", roles.accent)
        put("ProgressBar.progressColor", roles.accent)
        put("Counter.background", roles.accent)
    }

    private fun refreshWindows() {
        for (window in Window.getWindows()) {
            runCatching { SwingUtilities.updateComponentTreeUI(window) }
            window.repaint()
        }
    }

    private val DARK_NAMES = listOf("Dark", "Darcula", "IntelliJ Darcula")
    private val LIGHT_NAMES = listOf("Light", "IntelliJ Light", "IntelliJ")

    private val BACKGROUND_KEYS = listOf(
        "Panel.background", "Viewport.background", "ScrollPane.background",
        "Tree.background", "Table.background", "List.background",
        "TextField.background", "FormattedTextField.background", "PasswordField.background",
        "TextArea.background", "TextPane.background", "EditorPane.background",
        "ComboBox.background", "MenuBar.background", "Menu.background", "MenuItem.background",
        "PopupMenu.background", "TabbedPane.background", "ToolBar.background",
        "OptionPane.background", "SidePanel.background",
        "ToolWindow.background", "ToolWindow.Header.inactiveBackground",
        "DefaultTabs.background", "EditorTabs.background", "StatusBar.background",
        "MainToolbar.background", "Popup.background", "CompletionPopup.background",
    )
    private val DARKER_BACKGROUND_KEYS = listOf(
        "ToolWindow.Header.background", "Table.stripeColor", "Tree.stripeColor",
    )
    private val LIGHTER_BACKGROUND_KEYS = listOf(
        "Button.background", "ActionButton.hoverBackground",
    )
    private val FOREGROUND_KEYS = listOf(
        "Panel.foreground", "Label.foreground", "Tree.foreground", "Table.foreground",
        "List.foreground", "TextField.foreground", "TextArea.foreground", "TextPane.foreground",
        "EditorPane.foreground", "ComboBox.foreground", "MenuItem.foreground", "Menu.foreground",
        "MenuBar.foreground", "TabbedPane.foreground", "ToolTip.foreground", "CheckBox.foreground",
        "RadioButton.foreground", "Button.foreground",
    )
    private val SELECTION_BG_KEYS = listOf(
        "Tree.selectionBackground", "Table.selectionBackground", "List.selectionBackground",
        "TextField.selectionBackground", "TextArea.selectionBackground",
        "EditorPane.selectionBackground", "MenuItem.selectionBackground", "Menu.selectionBackground",
        "ComboBox.selectionBackground",
    )
    private val SELECTION_FG_KEYS = listOf(
        "Tree.selectionForeground", "Table.selectionForeground", "List.selectionForeground",
        "TextField.selectionForeground", "TextArea.selectionForeground",
        "MenuItem.selectionForeground", "Menu.selectionForeground", "ComboBox.selectionForeground",
    )
    private val BORDER_KEYS = listOf(
        "Component.borderColor", "Separator.foreground", "Separator.separatorColor",
        "TabbedPane.contentAreaColor", "Borders.color", "Group.separatorColor",
        "ToolWindow.HeaderTab.underlineColor",
    )
}
