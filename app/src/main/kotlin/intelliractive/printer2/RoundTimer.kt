package intelliractive.printer2

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

class RoundTimer(override var seconds: Int, var picRow: List<Material>?=null): Timer(seconds){
    override fun start() {
        object : BukkitRunnable() {
            override fun run() {
                if (seconds <= 0) {
                    onTimeIsUp()
                    // Timer has finished
                    state = "off"
                    cancel()
                } else {
                    // Decrement the timer
                    seconds--
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("GP_Printer2")!!, 0, 20)
    }
}