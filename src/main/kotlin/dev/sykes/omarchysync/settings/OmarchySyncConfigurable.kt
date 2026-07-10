package dev.sykes.omarchysync.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import dev.sykes.omarchysync.OmarchyColorsParser
import dev.sykes.omarchysync.OmarchyEnvironment
import dev.sykes.omarchysync.ThemeApplier
import javax.swing.JComponent

/**
 * Settings ▸ Appearance & Behavior ▸ Omarchy Theme Sync.
 * Toggles what gets synced, shows Omarchy-detection status, and offers a manual "Apply now".
 */
class OmarchySyncConfigurable : Configurable {

    private var panel: com.intellij.openapi.ui.DialogPanel? = null
    private val settings get() = OmarchySyncSettings.getInstance()

    override fun getDisplayName(): String = "Omarchy Theme Sync"

    override fun createComponent(): JComponent {
        val status = detectionStatus()
        val built = panel {
            row {
                cell(JBLabel(status))
            }
            row {
                checkBox("Enable Omarchy theme sync")
                    .bindSelected(settings::enabled)
            }
            indent {
                row {
                    checkBox("Sync editor color scheme")
                        .bindSelected(settings::syncEditorScheme)
                }
                row {
                    checkBox("Sync IDE UI theme (light/dark + surface colors)")
                        .bindSelected(settings::syncUiTheme)
                }
                row {
                    checkBox("Apply on IDE startup")
                        .bindSelected(settings::applyOnStartup)
                }
            }
            row {
                button("Apply Current Omarchy Theme Now") {
                    panel?.apply()
                    ThemeApplier.applyNow(force = true)
                }
            }
        }
        panel = built
        return built
    }

    private fun detectionStatus(): String {
        val install = OmarchyEnvironment.detect()
            ?: return "Omarchy not detected — ${OmarchyEnvironment.lastFailureReason}"
        val theme = OmarchyColorsParser.parse(install)
        val mode = if (install.isLight()) "light" else "dark"
        return if (theme != null) {
            "Omarchy detected. Active theme: ${theme.name} ($mode)."
        } else {
            "Omarchy detected, but its colors.toml could not be parsed."
        }
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
        ThemeApplier.applyNow(force = true)
    }

    override fun reset() {
        panel?.reset()
    }
}
