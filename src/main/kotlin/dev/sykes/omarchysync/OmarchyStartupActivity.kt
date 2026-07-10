package dev.sykes.omarchysync

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.sykes.omarchysync.settings.OmarchySyncSettings
import dev.sykes.omarchysync.watch.OmarchyThemeWatcher
import java.util.concurrent.atomic.AtomicBoolean

/**
 * On the first project opened in this IDE session: validate Omarchy, apply the current theme once
 * (if enabled), and start the file watcher. The one-time guard keeps multi-project sessions from
 * re-running the sync or re-notifying.
 */
class OmarchyStartupActivity : ProjectActivity {

    private val LOG = logger<OmarchyStartupActivity>()

    override suspend fun execute(project: Project) {
        if (!initialized.compareAndSet(false, true)) return

        val settings = OmarchySyncSettings.getInstance()
        if (!settings.enabled) {
            LOG.info("Omarchy Theme Sync is disabled in settings")
            return
        }

        val install = OmarchyEnvironment.detect()
        if (install == null) {
            LOG.info("Omarchy not detected: ${OmarchyEnvironment.lastFailureReason}")
            OmarchyNotifier.info(
                project,
                "Omarchy Theme Sync inactive",
                "This machine isn't running Omarchy, so theme syncing is disabled. " +
                    "Reason: ${OmarchyEnvironment.lastFailureReason}",
            )
            return
        }

        if (settings.applyOnStartup) {
            ThemeApplier.applyNow(force = true)
        }
        OmarchyThemeWatcher.getInstance().startIfNeeded()
    }

    companion object {
        private val initialized = AtomicBoolean(false)
    }
}
