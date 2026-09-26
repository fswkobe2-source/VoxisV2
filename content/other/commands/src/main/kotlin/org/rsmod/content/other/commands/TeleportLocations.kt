package org.rsmod.content.other.commands

import org.rsmod.map.CoordGrid

/** Named destinations used by `::home` and `::teleto <name>`. */
internal object TeleportLocations {
    val HOME: CoordGrid = CoordGrid(x = 3094, z = 3491, level = 0)

    val byName: Map<String, CoordGrid> =
        mapOf(
            "edgeville" to HOME,
            "lumbridge" to CoordGrid(x = 3222, z = 3218, level = 0),
            "varrock" to CoordGrid(x = 3212, z = 3424, level = 0),
            "falador" to CoordGrid(x = 2965, z = 3379, level = 0),
            "grand_exchange" to CoordGrid(x = 3164, z = 3487, level = 0),
        )
}
