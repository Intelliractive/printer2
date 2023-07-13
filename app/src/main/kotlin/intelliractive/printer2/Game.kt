/*
    This is a Minecraft mini-game made with Spigot

    The game goes like this: players enter, with a sufficient number of players, the game begins.
    A picture is selected that will be printed by a large printer.
    Players are "jets" that have their own material (a cell in the inventory is selected).
    Printing takes place line by line, 1 block at a time.
    Depending on the number of players, certain blocks have already been placed: if there are 10 players, then there are none, if there are fewer, a block has already been placed in place of the missing players.
    The places in which to put blocks (if there are less than 10 players) are determined in advance.
    The goal is to print the picture correctly (each block is in place and of the correct color).
    The game is special for its dependence on the coherence of the players' team.
 */
@file:Suppress("KDocMissingDocumentation")

package intelliractive.printer2

//import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.*
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Level

open class Game(val plugin: App) : Listener {
    // Состояние игры
    var isStarted: Boolean = false

    // Событие - игрок присоединился
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // greet the player
        event.player.sendMessage(Component.text("Привет! Игра скоро начнётся!", TextColor.color(10, 20, 255)))

        // if the game is already started, don't count down
        if (isStarted) {
            getLogger().log(Level.SEVERE, "Игра уже запущена")
            return
        } else {
            // if the game is not started, but there are enough players, start the game
            // if (getServer().onlinePlayers.size >= 2)
            countDownAndStart()
        }
    }

    // Отсчёт до игры
    fun countDownAndStart() {
        getLogger().log(Level.FINER, "Counting down")

        broadcast(Component.text("Скоро начнём", TextColor.color(90, 80, 100)))

        val countdown = Countdown(10)
        countdown.start()

        getScheduler().runTaskLaterAsynchronously(plugin, { ->
            if (countdown.state == "off")
                beginGame()
        }, 210)

        // set the game to started
        isStarted = true
    }

    // Алгоритм игры
    fun beginGame() {
        getLogger().log(Level.FINER, "Starting game")

        // Игроки телепортируются на игровое поле.
        getOnlinePlayers().forEach { player ->
            player.teleport(Location(getWorld("world"), -3.0, -50.0, -1.0))

            getScheduler().runTaskLaterAsynchronously(plugin, { ->
                getScheduler().getMainThreadExecutor(plugin)
                    .execute { getServer().getWorld("world")?.let { player.isJumping = true } }
            }, 20)
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


