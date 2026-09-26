package org.rsmod.content.other.maps

import org.rsmod.api.type.builders.map.loc.MapLocSpawnBuilder

/** Custom loc placements (`l[x]_[z]`) for the Edgeville map squares. See this module's README. */
object EdgevilleLocs : MapLocSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<EdgevilleLocs>("/map/l48_54")
    }
}
