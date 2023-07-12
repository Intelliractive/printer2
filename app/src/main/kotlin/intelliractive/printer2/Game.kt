/*
    This is a Minecraft mini-game made with Spigot

    The game goes like this: players enter, with a sufficient number of players, the game begins.
    A picture is selected that will be printed by a large printer.
    The picture is divided into blocks. Picture size is 10 by 10.
    Players are "jets" that have their own color (a cell in the inventory is selected).
    Printing takes place line by line, 1-2 blocks depending on the number of players.
    The goal is to print the picture correctly (each block is in right place and of right color).
    The game differs depending on the coherence of the team of players.
    The game is over when all the blocks are printed.
 */
//@file:Suppress("DEPRECATION")

package intelliractive.printer2

//import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.*
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class Game : Listener {
    // Состояние игры
    private var isStarted = false

    // Событие - игрок присоединился
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // greet the player
        event.player.sendMessage("&bПривет! Игра скоро начнётся!")

        // if the game is already started, don't count down
        if (isStarted) {
            return
        } else {
            // if the game is not started, but there are enough players, start the game
//            if (getServer().onlinePlayers.size >= 2)
            countDownAndStart()
        }
    }

    // Отсчёт до игры
    private fun countDownAndStart() {
        Thread{Runnable {
            broadcast(Component.text("Скоро начнём", TextColor.color(255, 255, 255)))

            for (i in 1..10) {
                getServer().broadcastMessage("&aИгра начнётся через &e" + i + "&r&a секунд")
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // set the game to started
            isStarted = true

            getServer().broadcastMessage("&bИГРА СТАРТУЕТ!")
            start()
        }}.start()
    }

    // Алгоритм игры
    private fun start() {
        // Игроки телепортируются на игровое поле.
        getOnlinePlayers().forEach { player ->
//            getServer().getWorld("world")?.let { player.teleport(it.spawnLocation) }
            getServer().getWorld("world")?.let { player.teleport(Location(getWorld("world"), 0.0, 0.0, 0.0)) }
            Thread.sleep(1000)
            getServer().getWorld("world")?.let { player.setJumping(true) }
        }

        /* Each row of a picture is a list of blocks. From the end to beginning, a row is selected.
        Next players stand in place of a block and hold it ih their hand.
        After some time, a row gets printed with blocks in players' place and which they hold.
        The printed row is compared to the selected row from the original picture.
        If the printed row is the same as the selected row from the original picture, players get 10 points.
        The game is over when all the rows are printed. */

        // Select a random picture
//        val picture = Picture.entries.random()
//
//        for (row in picture.grid.reversed()) {
//
//        }

        // Конец игры
        isStarted = false
    }
}


