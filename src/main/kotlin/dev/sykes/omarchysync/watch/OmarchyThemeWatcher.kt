package dev.sykes.omarchysync.watch

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import dev.sykes.omarchysync.OmarchyEnvironment
import dev.sykes.omarchysync.ThemeApplier
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Watches `~/.config/omarchy/current` for Omarchy theme switches and reapplies the sync.
 *
 * `omarchy-theme-set` rewrites `theme.name` and atomically swaps the whole `theme/` directory,
 * so watching the parent directory reliably catches every switch without fighting the `mv`.
 * Events are debounced to coalesce the burst a single switch produces.
 */
@Service(Service.Level.APP)
class OmarchyThemeWatcher : Disposable {

    private val LOG = logger<OmarchyThemeWatcher>()
    private val started = AtomicBoolean(false)
    @Volatile private var watchService: WatchService? = null
    @Volatile private var thread: Thread? = null

    /** Starts the watch loop once, only on a validated Omarchy machine. Safe to call repeatedly. */
    fun startIfNeeded() {
        if (!started.compareAndSet(false, true)) return
        val install = OmarchyEnvironment.detect() ?: run {
            LOG.debug("Watcher not started: Omarchy not detected")
            started.set(false)
            return
        }
        val service = runCatching { FileSystems.getDefault().newWatchService() }.getOrElse {
            LOG.warn("Could not create WatchService; theme changes won't be auto-detected", it)
            started.set(false)
            return
        }
        watchService = service
        runCatching {
            install.currentDir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        }.onFailure {
            LOG.warn("Failed to register watch on ${install.currentDir}", it)
        }

        thread = Thread({ runLoop(service) }, "omarchy-theme-watcher").apply {
            isDaemon = true
            start()
        }
        LOG.info("Watching ${install.currentDir} for Omarchy theme changes")
    }

    private fun runLoop(service: WatchService) {
        while (!Thread.currentThread().isInterrupted) {
            val key = try {
                service.take()
            } catch (_: InterruptedException) {
                break
            } catch (_: java.nio.file.ClosedWatchServiceException) {
                break
            }
            // Drain and debounce: absorb the burst from one theme switch.
            key.pollEvents()
            Thread.sleep(DEBOUNCE_MS)
            drainPending(service)

            LOG.debug("Omarchy theme change detected; reapplying")
            ThemeApplier.applyNow(force = true)

            if (!key.reset()) {
                // The watched directory became inaccessible; stop cleanly.
                LOG.info("Watch key invalid; stopping watcher")
                break
            }
        }
    }

    private fun drainPending(service: WatchService) {
        while (true) {
            val next = service.poll() ?: break
            next.pollEvents()
            next.reset()
        }
    }

    override fun dispose() {
        thread?.interrupt()
        runCatching { watchService?.close() }
        watchService = null
        thread = null
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
        fun getInstance(): OmarchyThemeWatcher = service()
    }
}
