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

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent

class Game(val plugin: App) : Listener { // plugin не трогать! (нужно для Bukkit заданий)
    var world = getWorld("world")!!

    // Состояние игры
    var isGameStarted = false
    var isWaitingForPlayers = false

    // a list of players that are going to play
    var goingToPlay = mutableListOf<Player>()

    // Список игроков по командам
    var lightBlueTeam = mutableListOf<Player>()
    var greenTeam = mutableListOf<Player>()

    // Полоса на выбор команд
    var teamChoiceTimerBar: BossBar? = BossBar.bossBar(
        Component.text("Ожидание выбора команды", TextColor.color(252, 186, 3)),
        0.0F,
        BossBar.Color.RED,
        Overlay.PROGRESS
    )

    // Выбор команды
    var teamChoiceTimer: Timer = Timer(15).apply {
        onTick = { teamChoiceTimerBar?.progress(seconds.toFloat() / 10) }
    }

    fun createTeams() {
        dispatchCommand(getConsoleSender(), "team add green")
        dispatchCommand(getConsoleSender(), "team modify green collisionRule never")
        dispatchCommand(getConsoleSender(), "team modify green color green")
        dispatchCommand(getConsoleSender(), "team modify green nameTagVisibility never")

        dispatchCommand(getConsoleSender(), "team add lightBlue")
        dispatchCommand(getConsoleSender(), "team modify lightBlue collisionRule never")
        dispatchCommand(getConsoleSender(), "team modify lightBlue color aqua")
        dispatchCommand(getConsoleSender(), "team modify lightBlue nameTagVisibility never")
    }

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        createTeams()
    }

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
            })
        )

        if (!isGameStarted || goingToPlay.size <= 10)
            waitForPlayers()

        if (!isGameStarted && isWaitingForPlayers) {
            // Игрок выбирает команду
            teamChoiceTimerBar?.addViewer(player)

            player.sendMessage(Component.text("Выбери команду!", TextColor.color(255, 0, 255)))
            player.sendMessage(
                Component.text("[Голубая]", TextColor.color(0, 255, 255)).clickEvent(
                    ClickEvent.callback {
                        dispatchCommand(getConsoleSender(), "/execute as ${player.name} run team join lightBlue")
                        lightBlueTeam.add(player)
                    }
                )
            )
            player.sendMessage(Component.text(" ---- ", TextColor.color(245, 245, 245)))
            player.sendMessage(
                Component.text("[Зелёная]", TextColor.color(0, 255, 0)).clickEvent(
                    ClickEvent.callback {
                        dispatchCommand(getConsoleSender(), "/execute as ${player.name} run team join green")
                        greenTeam.add(player)
                    }
                )
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (event.player in goingToPlay)
            goingToPlay.remove(event.player)
//        if (!isGameStarted &&)
    }

    val arcs: List<Location> = listOf(
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
    fun waitForPlayers() {
//        broadcast(Component.text("Скоро начнём", TextColor.color(0, 150, 250)))
        isWaitingForPlayers = true

        var waitingForPlayersBar: BossBar? = BossBar.bossBar(Component.text("Ожидание игроков", TextColor.color(0, 200, 250)), 0.0F, BossBar.Color.BLUE, Overlay.NOTCHED_6)

        goingToPlay.forEach { player ->
            player.teleport(Locations.WaitingPlate.loc)
            player.gameMode = GameMode.ADVENTURE

            waitingForPlayersBar?.addViewer(player)
        }

        val countdown = Timer(100)
        countdown.onTick = {
            waitingForPlayersBar?.progress(countdown.seconds / 100.0F)
        }
        countdown.onTimeIsUp = {
            teamChoiceTimer.onTick = {
                isWaitingForPlayers = false
                goingToPlay.forEach { waitingForPlayersBar?.removeViewer(it) }

                broadcast(Component.text("ИГРА СТАРТУЕТ!", TextColor.color(0, 200, 0)))

                // Игроки телепортируются под арку (случайную)
                goingToPlay.forEach {
                    it.teleport(arcs.random())
                }

                // Игроки телепортируются на игровое поле.
                lightBlueTeam.forEach { player ->
                    player.teleport(Locations.Light_Blue_Game_Area.loc)
                }
                greenTeam.forEach { player ->
                    player.teleport(Locations.Green_Game_Area.loc)
                }

                // Убирается полоса
                goingToPlay.forEach {
                    teamChoiceTimerBar?.removeViewer(it)
                }

                if (goingToPlay.isNotEmpty()) {
                    // set the game to started
                    isGameStarted = true
                    beginGame()
                }
            }
        }
        countdown.start()
    }

    // Алгоритм игры
    fun beginGame() {
        broadcast(Component.text("Старт!", TextColor.color(0, 200, 0)))
        broadcast(Component.text("Играют: ${goingToPlay.joinToString(", ")}", TextColor.color(200, 200, 0)))


        /* Each row of a picture is a list of blocks. From the end to beginning, a row is selected.
        Next players stand in place of a block and hold it ih their hand.
        After some time, a row gets printed with blocks in players' place and which they hold.
        The printed row is compared to the selected row from the original picture.
        If the printed row is the same as the selected row from the original picture, players get 10 points.
        The game is over when all the rows are printed. */

        // Select a random picture
        val picture = Picture.entries.random()

        // представление игры
        dispatchCommand(getConsoleSender(), "title @a times 20 20 20")
        dispatchCommand(
            getConsoleSender(),
            "title @a title [\"\",{\"text\":\"\\u041c\\u0438\\u043d\\u0438-\\u0438\\u0433\\u0440\\u0430 \\\"\",\"color\":\"dark_green\"},{\"text\":\"\\u041f\\u0440\\u0438\\u043d\\u0442\\u0435\\u0440\"},{\"text\":\"\\\"\",\"color\":\"dark_green\"}]"
        )
        // краткая инструкция
        dispatchCommand(
            getConsoleSender(),
            "title @a actionbar {\"text\":\"\\u0421\\u0442\\u0430\\u043d\\u043e\\u0432\\u0438\\u0442\\u0435\\u0441\\u044c \\u043a\\u0443\\u0434\\u0430 \\u043d\\u0430\\u0434\\u043e, \\u0434\\u0435\\u0440\\u0436\\u0430 \\u0432 \\u0440\\u0443\\u043a\\u0435 \\u043f\\u0440\\u0430\\u0432. \\u0431\\u043b\\u043e\\u043a!\",\"underlined\":true,\"color\":\"gold\"}"
        )
        // "Ваша картинка - ..."
        dispatchCommand(
            getConsoleSender(),
            "title @a actionbar [\"\",{\"text\":\"\\u2191\",\"color\":\"#3CFF0A\"},{\"text\":\" \\u0412\\u0430\\u0448\\u0430 \\u043a\\u0430\\u0440\\u0442\\u0438\\u043d\\u043a\\u0430 -\",\"italic\":true,\"color\":\"yellow\"},{\"text\":\" ...\",\"color\":\"light_purple\"},{\"text\":\" \\u2191\",\"color\":\"#09FF00\"}]"
        )
        // изображение
        dispatchCommand(getConsoleSender(), "title @a times 10 20 10")
        if (picture.rusNameSubt.isNullOrEmpty().not()) dispatchCommand(
            getConsoleSender(),
            "title @a subtitle ${picture.rusNameSubt}"
        )
        dispatchCommand(getConsoleSender(), "title @a title ${picture.rusName}")

        val neutralZone = listOf<Block>(
            world.getBlockAt(3, -51, 35),
            world.getBlockAt(3, -51, 36),
            world.getBlockAt(3, -51, 37),
            world.getBlockAt(3, -51, 38),
            world.getBlockAt(3, -51, 39),
            world.getBlockAt(3, -51, 40),
            world.getBlockAt(3, -51, 41),
            world.getBlockAt(3, -51, 42),
            world.getBlockAt(3, -51, 43),
            world.getBlockAt(3, -51, 44)
        )

        val bonusZone = listOf<Block>(
            // green
            world.getBlockAt(2, -51, 35),
            world.getBlockAt(2, -51, 36),
            world.getBlockAt(2, -51, 37),
            world.getBlockAt(2, -51, 38),
            world.getBlockAt(2, -51, 39),
            // light blue
            world.getBlockAt(2, -51, 40),
            world.getBlockAt(2, -51, 41),
            world.getBlockAt(2, -51, 42),
            world.getBlockAt(2, -51, 43),
            world.getBlockAt(2, -51, 44)
        )

        val hintRow = listOf(
            world.getBlockAt(4, -52, 35),
            world.getBlockAt(4, -52, 36),
            world.getBlockAt(4, -52, 37),
            world.getBlockAt(4, -52, 38),
            world.getBlockAt(4, -52, 39),
            world.getBlockAt(4, -52, 40),
            world.getBlockAt(4, -52, 41),
            world.getBlockAt(4, -52, 42),
            world.getBlockAt(4, -52, 43),
            world.getBlockAt(4, -52, 44)
        )

//        for (row in picture.grid.reversed()) {
//
//        }

        // Конец игры
        isGameStarted = false
        broadcast(Component.text("Игра окончена!", TextColor.color(0, 150, 190)))
        goingToPlay.forEach { player ->
            player.gameMode = GameMode.SURVIVAL
            player.teleport(Locations.WaitingPlate.loc)
        }
        goingToPlay.clear()
    }
}