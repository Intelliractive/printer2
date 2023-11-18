@file:Suppress("KDocMissingDocumentation", "ReplaceNotNullAssertionWithElvisReturn")
package intelliractive.printer2

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.scheduler.BukkitRunnable
/** #### Отсчёт до игры*/
class Countdown(override var seconds: Int): Timer(seconds){
    override var state: String = "on"
    override fun start() {
        object : BukkitRunnable() {
            override fun run() {
                if (seconds <= 0) {
                    onTimeIsUp()
                    // Timer has finished
                    state = "off"
                    cancel()
                } else {
                    getOnlinePlayers().forEach { player ->
                        player.sendMessage(Component.text("Игра начнётся через ${seconds}с", TextColor.color(155, 255, 255)))
                    }

                    // Decrement the timer
                    seconds--
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("GP_Printer2")!!, 0, 20)
    }
}

