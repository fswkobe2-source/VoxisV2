package org.rsmod.content.other.maps

import org.rsmod.api.type.builders.map.tile.MapTileBuilder

/** Custom terrain (`m[x]_[z]`) for the Edgeville map squares. See this module's README. */
object EdgevilleTiles : MapTileBuilder() {
    override fun onPackMapTask() {
        resourceFile<EdgevilleTiles>("/map/m48_54")
    }
}
