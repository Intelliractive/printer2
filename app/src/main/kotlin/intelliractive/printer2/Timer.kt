@file:Suppress("KDocMissingDocumentation", "ReplaceNotNullAssertionWithElvisReturn")

package intelliractive.printer2

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
open class Timer(open var seconds: Int, open var onTick: () -> Unit = {}, open var onTimeIsUp: () -> Unit = {}) {
    open var state: String = "on"
    open fun start() {
        object : BukkitRunnable() {
            override fun run() {
                if (seconds <= 0) {
                    onTimeIsUp()
                    // Timer has finished
                    state = "off"
                    cancel()
                } else {
                    onTick()
                    // Decrement the timer
                    seconds--
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("GP_Printer2")!!, 0, 20)
    }
}
