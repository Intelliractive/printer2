/*
    Note for CodeWhisperer:

        This is a Minecraft mini-game made with Spigot

        The game goes like this: players enter, with a sufficient number of players, the game begins.
        A picture is selected that will be printed by a large printer.
        Players are "jets" that have their own material (a cell in the inventory is selected).
        Printing takes place line by line, 1 block at a time.
        Depending on the number of players, certain blocks have already been placed: if there are 10 players, then there are none, if there are fewer, a block has already been placed in place of the missing players.
        The places in which to put blocks (if there are less than 10 players) are determined in advance.
        The goal is to print the picture correctly (each block is in place and of the correct color).
        The game is special for its dependence on the coherence of the players' team.

        Code comments are written in English and Russian.
 */
@file:Suppress("KDocMissingDocumentation", "MemberVisibilityCanBePrivate", "CanBeVal")

package intelliractive.printer2

//import kotlinx.coroutines.*
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class Game(val plugin: App) : Listener { // plugin не трогать! (нужно для Bukkit заданий)
    @EventHandler
    fun onStarted(event: org.bukkit.event.world.WorldLoadEvent){
        dispatchCommand(getConsoleSender(), "team add green")
        dispatchCommand(getConsoleSender(), "team modify green collisionRule never")
        dispatchCommand(getConsoleSender(), "team modify green color green")
        dispatchCommand(getConsoleSender(), "team modify green nameTagVisibility never")

        dispatchCommand(getConsoleSender(), "team add lightBlue")
        dispatchCommand(getConsoleSender(), "team modify lightBlue collisionRule never")
        dispatchCommand(getConsoleSender(), "team modify lightBlue color aqua")
        dispatchCommand(getConsoleSender(), "team modify lightBlue nameTagVisibility never")
    }

    // Состояние игры
    var isStarted: Boolean = false

    // a list of players that are going to play
    var goingToPlay: MutableList<Player> = mutableListOf()

    // Событие - игрок присоединился
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        // greet the player
        player.sendMessage(Component.text("Привет! Игра скоро начнётся!", TextColor.color(10, 20, 255)))
        player.sendMessage(Component.text("Будешь играть?", TextColor.color(100, 20, 255)))
        player.sendMessage(
            Component.text("[Да]", TextColor.color(0, 200, 0)).clickEvent(ClickEvent.callback {
                goingToPlay.add(player)
                player.sendMessage(
                    Component.text(
                        "Ты в игре!",
                        TextColor.color(0, 220, 0)
                    )
                )

                //// Таймер и отсчёт до игры
                val preGameTimer = Timer(6)
                var preGameCDBar: BossBar? = BossBar.bossBar(
                    Component.text("Ожидание игроков ещё ${preGameTimer.seconds} секунд", TextColor.color(255, 255, 0)),
                    0.0f,
                    BossBar.Color.GREEN,
                    BossBar.Overlay.PROGRESS
                )
                preGameCDBar?.addViewer(player)
                preGameTimer.tick = {
                    // broadcast(Component.text("Ожидание игроков ещё ${preGameTimer.seconds} секунд", TextColor.color(255, 255, 0)))
                    preGameCDBar?.progress(preGameTimer.seconds.toFloat() / 6)
                }
                preGameTimer.task = {
                    // if the game is already started, don't count down
                    if (isStarted) {
                        broadcast(Component.text("Игра уже запущена!", TextColor.color(255, 0, 0)))
                    } else {
                        // if the game is not started, but there are enough players, start the game
                        // if (getServer().onlinePlayers.size >= 2)
                        if (goingToPlay.isNotEmpty())
                            countDownAndStart(goingToPlay)
                        // else
                        //     broadcast(Component.text("Недостаточно игроков!", TextColor.color(255, 0, 0)))
                    }

                    preGameCDBar?.removeViewer(player)
                }
                preGameTimer.start()
            })
        )
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (event.player in goingToPlay)
            goingToPlay.remove(event.player)
    }

    val Arcs: List<Location> = listOf(
        Location(getWorld("world"), -31.0, -60.0, 34.0),
        Location(getWorld("world"), -31.0, -60.0, 29.0),
        Location(getWorld("world"), -31.0, -60.0, 24.0),
        Location(getWorld("world"), -31.0, -60.0, 19.0),

        Location(getWorld("world"), -31.0, -60.0, 39.0),

        Location(getWorld("world"), -31.0, -60.0, 44.0),
        Location(getWorld("world"), -31.0, -60.0, 49.0),
        Location(getWorld("world"), -31.0, -60.0, 54.0),
        Location(getWorld("world"), -31.0, -60.0, 59.0)
    )

    // Отсчёт до игры
    fun countDownAndStart(goingToPlay: MutableList<Player>) {
        broadcast(Component.text("Скоро начнём", TextColor.color(90, 80, 100)))
        Timer(1, task = {
            goingToPlay.forEach { player ->
                player.teleport(Locations.WaitingPlate.loc)
                player.gameMode = GameMode.ADVENTURE
            }
        }).start()

        val countdown = Countdown(10)
        countdown.task = {
            broadcast(Component.text("ИГРА СТАРТУЕТ!", TextColor.color(0, 200, 0)))

            // Игроки телепортируются под арку (случайную)
            goingToPlay.forEach {
                it.teleport(Arcs.random())
            }

            // Список игроков по командам
            var lightBlueTeam = mutableListOf<Player>()
            var greenTeam = mutableListOf<Player>()

            // Полоса на выбор команд
            var playersChooseTeamTimerBar: BossBar? = BossBar.bossBar(
                Component.text("Ожидание выбора команды", TextColor.color(252, 186, 3)),
                0.0F,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS
            )

            // Игроки выбирают команду
            goingToPlay.forEach { player ->
                playersChooseTeamTimerBar?.addViewer(player)

                player.sendMessage(Component.text("Выбери команду!", TextColor.color(255, 0, 255)))
                player.sendMessage(
                    Component.text("[Голубая]", TextColor.color(0, 255, 255))
                        .clickEvent(
                            ClickEvent.callback {
                                dispatchCommand(getConsoleSender(), "/sudo ${player.name} team join lightBlue")
                                lightBlueTeam.add(player)
                            }
                        )
                        .append(Component.text(" ---- "))
                        .append(Component.text("[Зелёная]", TextColor.color(0, 255, 0)))
                        .clickEvent(
                            ClickEvent.callback {
                                dispatchCommand(getConsoleSender(), "/sudo ${player.name} team join green")
                                greenTeam.add(player)
                            }
                        )
                )
            }

            val playersChooseTeamTimer = Timer(15, task = {

                // Игроки телепортируются на игровое поле.

                lightBlueTeam.forEach { player ->
                    player.teleport(Locations.Light_Blue_Game_Area.loc)
                }

                greenTeam.forEach { player ->
                    player.teleport(Locations.Green_Game_Area.loc)
                }


                // Убирается полоса
                goingToPlay.forEach {
                    playersChooseTeamTimerBar?.removeViewer(it)
                }

                if (goingToPlay.isNotEmpty()) {
                    // set the game to started
                    isStarted = true
                    beginGame(goingToPlay)
                }
            })
            playersChooseTeamTimer.tick = {
                playersChooseTeamTimerBar?.progress(playersChooseTeamTimer.seconds.toFloat() / 10)
            }
            playersChooseTeamTimer.start()

        }
        countdown.start()

    }

    // Алгоритм игры
    fun beginGame(goingToPlay: MutableList<Player>) {
        broadcast(Component.text("Старт!", TextColor.color(0, 200, 0)))
        broadcast(Component.text("Играют: ${goingToPlay.joinToString(", ")}", TextColor.color(200, 200, 0)))


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
        broadcast(Component.text("Игра окончена!", TextColor.color(0, 150, 190)))
        goingToPlay.forEach { player ->
            player.gameMode = GameMode.SURVIVAL
            player.teleport(Locations.WaitingPlate.loc)
        }
        goingToPlay.clear()
        return
    }
}