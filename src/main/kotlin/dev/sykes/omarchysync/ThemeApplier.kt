package dev.sykes.omarchysync

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import dev.sykes.omarchysync.scheme.EditorSchemeGenerator
import dev.sykes.omarchysync.settings.OmarchySyncSettings
import dev.sykes.omarchysync.ui.UiThemeApplier

/**
 * Orchestrates a full sync: validate Omarchy, parse the active theme, then apply the editor
 * scheme and/or UI tint on the EDT according to user settings. All entry points (startup, the
 * file watcher, and the "Apply now" action) funnel through here.
 */
object ThemeApplier {

    private val LOG = logger<ThemeApplier>()

    /**
     * Applies the current Omarchy theme if syncing is enabled and this is an Omarchy machine.
     *
     * @param force reapply even when the theme name matches the last applied one.
     * @return true if a theme was applied.
     */
    fun applyNow(force: Boolean = false): Boolean {
        val settings = OmarchySyncSettings.getInstance()
        if (!settings.enabled) return false

        val install = OmarchyEnvironment.detect() ?: run {
            LOG.debug("Skipping apply: Omarchy not detected (${OmarchyEnvironment.lastFailureReason})")
            return false
        }
        val theme = OmarchyColorsParser.parse(install) ?: run {
            LOG.warn("Could not parse Omarchy colors from ${install.colorsTomlPath}")
            return false
        }

        val signature = "${theme.name}|light=${theme.isLight}"
        if (!force && settings.lastAppliedTheme == signature) {
            LOG.debug("Skipping apply: theme '$signature' already applied")
            return false
        }

        ApplicationManager.getApplication().invokeLater {
            runCatching {
                if (settings.syncEditorScheme) EditorSchemeGenerator.apply(theme)
            }.onFailure { LOG.warn("Editor scheme sync failed", it) }

            runCatching {
                if (settings.syncUiTheme) UiThemeApplier.apply(theme)
            }.onFailure { LOG.warn("UI theme sync failed (editor scheme still applied)", it) }

            settings.lastAppliedTheme = signature
            LOG.info("Synced JetBrains appearance to Omarchy theme '${theme.name}'")
        }
        return true
    }
}
