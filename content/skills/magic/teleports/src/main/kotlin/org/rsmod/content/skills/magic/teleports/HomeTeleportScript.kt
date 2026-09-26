package org.rsmod.content.skills.magic.teleports

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.synths
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.ui.IfOverlayButton
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.script.onPlayerLogout
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.utils.time.epochMinute
import org.rsmod.game.entity.Player
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.game.type.synth.SynthType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Standard spellbook Home Teleport. The destination is [params.spell_telecoord] on
 * [objs.spell_hometeleport_lumbridge], set in `SpellObjs`. The cast is the normal slow chalk-circle
 * sequence: each tick checks that the player is still standing on the start tile with no walk or
 * interaction queued, and the 30-minute cooldown is stored in [varps.aide_tele_timer] only after
 * the teleport completes.
 */
class HomeTeleportScript
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val protectedAccess: ProtectedAccessLauncher,
) : PluginScript() {
    private val logger = InlineLogger()
    private val casts = HashMap<Int, Cast>()

    private var ProtectedAccess.homeTeleCooldown by intVarp(varps.aide_tele_timer)

    override fun ScriptContext.startup() {
        onIfOverlayButton(home_components.teleport_home_standard) { clickHomeTeleport() }
        onPlayerQueueWithArgs<Int>(home_queues.home_teleport) { advance() }
        onPlayerLogout { casts.remove(player.slotId) }
    }

    private fun IfOverlayButton.clickHomeTeleport() {
        if (op != IfButtonOp.Op1) {
            return
        }
        protectedAccess.launch(player) { beginHomeTeleport() }
    }

    private fun ProtectedAccess.beginHomeTeleport() {
        val now = epochMinute()
        val readyAt = homeTeleCooldown + COOLDOWN_MINUTES
        if (homeTeleCooldown != 0 && now < readyAt) {
            val minutes = (readyAt - now).coerceAtLeast(1)
            val unit = if (minutes == 1) "minute" else "minutes"
            mes("You need to wait another $minutes $unit to cast this spell.")
            logger.info { "Home teleport blocked for ${player.username}: $minutes $unit remaining" }
            return
        }
        if (player.slotId in casts) {
            return
        }
        val dest = objTypes[objs.spell_hometeleport_lumbridge].param(params.spell_telecoord)
        casts[player.slotId] =
            Cast(start = player.coords, dest = dest, stage = 0, ticksLeft = STAGES[0].ticks)
        logger.info {
            "Home teleport started for ${player.username} from ${player.coords.x}," +
                "${player.coords.z},${player.coords.level} to ${dest.x},${dest.z},${dest.level}"
        }
        play(0)
        weakQueue(home_queues.home_teleport, cycles = 1, args = 0)
    }

    private fun ProtectedAccess.advance() {
        val cast = casts[player.slotId] ?: return
        if (player.homeTeleportInterrupted(cast.start)) {
            casts.remove(player.slotId)
            clearWeakQueue(home_queues.home_teleport)
            resetAnim()
            resetSpotanim()
            logger.info {
                "Home teleport interrupted for ${player.username} at ${player.coords.x}," +
                    "${player.coords.z},${player.coords.level}"
            }
            return
        }
        cast.ticksLeft--
        if (cast.ticksLeft > 0) {
            // Requeued from inside the queue processor, which decrements a new queue once
            // before the tick ends. 2 cycles is the next game tick.
            weakQueue(home_queues.home_teleport, cycles = 2, args = cast.stage)
            return
        }
        val next = cast.stage + 1
        if (next >= STAGES.size) {
            casts.remove(player.slotId)
            resetAnim()
            resetSpotanim()
            telejump(cast.dest)
            homeTeleCooldown = epochMinute()
            logger.info {
                "Home teleport landed ${player.username} at ${player.coords.x}," +
                    "${player.coords.z},${player.coords.level}"
            }
            return
        }
        cast.stage = next
        cast.ticksLeft = STAGES[next].ticks
        play(next)
        weakQueue(home_queues.home_teleport, cycles = 2, args = next)
    }

    private fun ProtectedAccess.play(stage: Int) {
        val step = STAGES[stage]
        anim(step.seq)
        if (step.spot == null) {
            resetSpotanim()
        } else {
            spotanim(step.spot)
        }
        if (step.sound != null) {
            soundSynth(step.sound)
        }
    }

    private fun Player.homeTeleportInterrupted(start: CoordGrid): Boolean =
        coords != start || routeDestination.isNotEmpty() || interaction != null

    private data class Cast(
        val start: CoordGrid,
        val dest: CoordGrid,
        var stage: Int,
        var ticksLeft: Int,
    )

    private data class Stage(
        val seq: SeqType,
        val spot: SpotanimType?,
        val sound: SynthType?,
        val ticks: Int,
    )

    private companion object {
        private const val COOLDOWN_MINUTES = 30

        private val STAGES =
            listOf(
                Stage(
                    home_seqs.drawing_chalk,
                    home_spots.chalk,
                    synths.aide_teleport_chalk,
                    ticks = 5,
                ),
                Stage(home_seqs.sit_down, null, synths.aide_teleport_sitdown, ticks = 4),
                Stage(home_seqs.get_book, home_spots.book, synths.aide_teleport_book, ticks = 5),
                Stage(home_seqs.recite, home_spots.portal, null, ticks = 6),
                Stage(
                    home_seqs.teleport,
                    home_spots.teleport,
                    synths.aide_teleport_portal,
                    ticks = 4,
                ),
            )
    }
}
