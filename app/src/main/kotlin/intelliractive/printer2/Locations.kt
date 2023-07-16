package intelliractive.printer2

import org.bukkit.Bukkit
import org.bukkit.Location

enum class Locations(var loc: Location) {
    WaitingPlate(Location(Bukkit.getWorld("world"), -3.0, -49.0, -1.0)), // площадка
    // игровое поле
    Light_Blue_Game_Area(Location(Bukkit.getWorld("world"), 1.0, -51.0, 40.0)),
    Green_Game_Area(Location(Bukkit.getWorld("world"), 1.0, -51.0, 39.0))
}