package dev.sykes.omarchysync.settings

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/**
 * Application-level, persisted user preferences for the sync behavior.
 */
@Service(Service.Level.APP)
@State(name = "OmarchyThemeSyncSettings", storages = [Storage("omarchy-theme-sync.xml")])
class OmarchySyncSettings :
    SerializablePersistentStateComponent<OmarchySyncSettings.State>(State()) {

    data class State(
        @JvmField val enabled: Boolean = true,
        @JvmField val syncEditorScheme: Boolean = true,
        @JvmField val syncUiTheme: Boolean = true,
        @JvmField val applyOnStartup: Boolean = true,
        /** Name of the last Omarchy theme we applied, to skip redundant reapplies. */
        @JvmField val lastAppliedTheme: String = "",
    )

    var enabled: Boolean
        get() = state.enabled
        set(value) {
            updateState { it.copy(enabled = value) }
        }

    var syncEditorScheme: Boolean
        get() = state.syncEditorScheme
        set(value) {
            updateState { it.copy(syncEditorScheme = value) }
        }

    var syncUiTheme: Boolean
        get() = state.syncUiTheme
        set(value) {
            updateState { it.copy(syncUiTheme = value) }
        }

    var applyOnStartup: Boolean
        get() = state.applyOnStartup
        set(value) {
            updateState { it.copy(applyOnStartup = value) }
        }

    var lastAppliedTheme: String
        get() = state.lastAppliedTheme
        set(value) {
            updateState { it.copy(lastAppliedTheme = value) }
        }

    companion object {
        fun getInstance(): OmarchySyncSettings = service()
    }
}
