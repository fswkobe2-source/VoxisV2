package org.rsmod.content.skills.magic.teleports

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences

internal typealias home_components = HomeTeleportComponents

internal typealias home_queues = HomeTeleportQueues

internal typealias home_seqs = HomeTeleportSeqs

internal typealias home_spots = HomeTeleportSpotanims

internal object HomeTeleportComponents : ComponentReferences() {
    val teleport_home_standard = find("magic_spellbook:teleport_home_standard")
}

internal object HomeTeleportQueues : QueueReferences() {
    val home_teleport = find("home_teleport")
}

internal object HomeTeleportSeqs : SeqReferences() {
    val drawing_chalk = find("aide_drawing_chalk_circle")
    val sit_down = find("aide_sitting_down_crosslegged")
    val get_book = find("aide_player_getting_book")
    val recite = find("aide_reciting_incantation")
    val teleport = find("aide_player_teleporting")
}

internal object HomeTeleportSpotanims : SpotanimReferences() {
    val chalk = find("aide_chalk_circle")
    val book = find("aide_player_book_get")
    val portal = find("aide_player_book_portal")
    val teleport = find("aide_player_teleport")
}
