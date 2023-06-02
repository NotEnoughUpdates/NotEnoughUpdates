/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.commands.misc

import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.lang3.text.WordUtils
import java.util.*
import java.util.concurrent.*

@NEUAutoSubscribe
class PeekCommand {

    var future: Future<*>? = null
    val executor = Executors.newScheduledThreadPool(1)

    fun executePeek(name: String) {
        val chatGui = Minecraft.getMinecraft().ingameGUI.chatGUI
        val id = Random().nextInt(Int.MAX_VALUE / 2) + Int.MAX_VALUE / 2
        fun deleteReply(text: String) {
            chatGui.printChatMessageWithOptionalDeletion(ChatComponentText(text), id)
        }

        deleteReply("$YELLOW[PEEK] Getting player information...")


        NotEnoughUpdates.profileViewer.loadPlayerByName(
            name
        ) { profile: SkyblockProfiles? ->
            if (profile == null) {
                deleteReply("$RED[PEEK] Unknown player or the Hypixel API is down.")
            } else {
                profile.resetCache()
                if (future?.isDone != true) {
                    Utils.addChatMessage(
                        "$RED[PEEK] New peek command was run, cancelling old one."
                    )
                    future?.cancel(true)
                }
                deleteReply("$YELLOW[PEEK] Getting the player's SkyBlock profile(s)...")
                val startTime = System.currentTimeMillis()
                future = ForkJoinPool.commonPool().submit(object : Runnable {
                    override fun run() {
                        if (System.currentTimeMillis() - startTime > 10 * 1000) {
                            deleteReply("$RED[PEEK] Getting profile info took too long, aborting.")
                            return
                        }
                        val g = GRAY.toString()
                        val profileInfo = profile.latestProfile.profileJson
                        if (profileInfo == null) {
                            future = executor.schedule(this, 200, TimeUnit.MILLISECONDS)
                            return
                        }
                        var overallScore = 0f
                        val isMe = name.equals("moulberry", ignoreCase = true)
                        val stats = profile.latestProfile.stats
                        if (stats == null) {
                            future = executor.schedule(this, 200, TimeUnit.MILLISECONDS)
                            return
                        }
                        val skyblockInfo = profile.latestProfile.levelingInfo
                        if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth) {
                            deleteReply("$YELLOW[PEEK] Getting the player's Skyblock networth...")
                            val countDownLatch = CountDownLatch(1)
                            profile.latestProfile.getSoopyNetworth { countDownLatch.countDown() }
                            try { // Wait for async network request
                                countDownLatch.await(10, TimeUnit.SECONDS)
                            } catch (_: InterruptedException) {}

                            // Now it's waited for network request the data should be cached (accessed in nw section)
                        }
                        deleteReply(
                            "$GREEN $STRIKETHROUGH-=-$RESET$GREEN ${
                                Utils.getElementAsString(
                                    profile.hypixelProfile!!["displayname"],
                                    name
                                )
                            }'s Info $STRIKETHROUGH-=-"
                        )
                        if (skyblockInfo == null || !profile.latestProfile.skillsApiEnabled()) {
                            Utils.addChatMessage(YELLOW.toString() + "Skills API disabled!")
                        } else {
                            var totalSkillLVL = 0f
                            var totalSkillCount = 0f
                            val skills: List<String> =
                                mutableListOf(
                                    "taming",
                                    "mining",
                                    "foraging",
                                    "enchanting",
                                    "farming",
                                    "combat",
                                    "fishing",
                                    "alchemy",
                                    "carpentry"
                                )
                            for (skillName in skills) {
                                totalSkillLVL += skyblockInfo[skillName]!!.level
                                totalSkillCount++
                            }
                            var combat = skyblockInfo["combat"]!!.level
                            var zombie = skyblockInfo["zombie"]!!.level
                            var spider = skyblockInfo["spider"]!!.level
                            var wolf = skyblockInfo["wolf"]!!.level
                            var enderman = skyblockInfo["enderman"]!!.level
                            var blaze = skyblockInfo["blaze"]!!.level
                            var avgSkillLVL = totalSkillLVL / totalSkillCount
                            if (isMe) {
                                avgSkillLVL = 6f
                                combat = 4f
                                zombie = 2f
                                spider = 1f
                                wolf = 2f
                                enderman = 0f
                                blaze = 0f
                            }
                            val combatPrefix =
                                if (combat > 20) (if (combat > 35) GREEN else YELLOW) else RED
                            val zombiePrefix =
                                if (zombie > 3) (if (zombie > 6) GREEN else YELLOW) else RED
                            val spiderPrefix =
                                if (spider > 3) (if (spider > 6) GREEN else YELLOW) else RED
                            val wolfPrefix =
                                if (wolf > 3) (if (wolf > 6) GREEN else YELLOW) else RED
                            val endermanPrefix =
                                if (enderman > 3) (if (enderman > 6) GREEN else YELLOW) else RED
                            val blazePrefix =
                                if (blaze > 3) (if (blaze > 6) GREEN else YELLOW) else RED
                            val avgPrefix =
                                if (avgSkillLVL > 20) (if (avgSkillLVL > 35) GREEN else YELLOW) else RED
                            overallScore += zombie * zombie / 81f
                            overallScore += spider * spider / 81f
                            overallScore += wolf * wolf / 81f
                            overallScore += enderman * enderman / 81f
                            overallScore += blaze * blaze / 81f
                            overallScore += avgSkillLVL / 20f
                            val cata = skyblockInfo["catacombs"]!!.level.toInt()
                            val cataPrefix =
                                if (cata > 15) (if (cata > 25) GREEN else YELLOW) else RED
                            overallScore += cata * cata / 2000f
                            Utils.addChatMessage(
                                g + "Combat: " + combatPrefix + Math.floor(combat.toDouble())
                                    .toInt() +
                                        (if (cata > 0) "$g - Cata: $cataPrefix$cata" else "") +
                                        g + " - AVG: " + avgPrefix + Math.floor(avgSkillLVL.toDouble())
                                    .toInt()
                            )
                            Utils.addChatMessage(
                                g + "Slayer: " + zombiePrefix + Math.floor(zombie.toDouble())
                                    .toInt() + g + "-" +
                                        spiderPrefix + Math.floor(spider.toDouble())
                                    .toInt() + g + "-" +
                                        wolfPrefix + Math.floor(wolf.toDouble()).toInt() + g + "-" +
                                        endermanPrefix + Math.floor(enderman.toDouble())
                                    .toInt() + g + "-" +
                                        blazePrefix + Math.floor(blaze.toDouble()).toInt()
                            )
                        }
                        val health = stats["health"].toInt()
                        val defence = stats["defence"].toInt()
                        val strength = stats["strength"].toInt()
                        val intelligence = stats["intelligence"].toInt()
                        val healthPrefix =
                            if (health > 800) (if (health > 1600) GREEN else YELLOW) else RED
                        val defencePrefix =
                            if (defence > 200) (if (defence > 600) GREEN else YELLOW) else RED
                        val strengthPrefix =
                            if (strength > 100) (if (strength > 300) GREEN else YELLOW) else RED
                        val intelligencePrefix =
                            if (intelligence > 300) (if (intelligence > 900) GREEN else YELLOW) else RED
                        Utils.addChatMessage(
                            g + "Stats  : " + healthPrefix + health + RED + "\u2764 " +
                                    defencePrefix + defence + GREEN + "\u2748 " +
                                    strengthPrefix + strength + RED + "\u2741 " +
                                    intelligencePrefix + intelligence + AQUA + "\u270e "
                        )
                        val bankBalance =
                            Utils.getElementAsFloat(
                                Utils.getElement(
                                    profileInfo,
                                    "banking.balance"
                                ), -1f
                            )
                        val purseBalance =
                            Utils.getElementAsFloat(
                                Utils.getElement(
                                    profileInfo,
                                    "coin_purse"
                                ), 0f
                            )
                        val networth = if (NotEnoughUpdates.INSTANCE.config.profileViewer.useSoopyNetworth) {
                            val nwData = profile.latestProfile.getSoopyNetworth {}
                            nwData?.networth ?: -2L
                        } else {
                            profile.latestProfile.networth
                        }
                        val money =
                            Math.max(bankBalance + purseBalance, networth.toFloat())
                        val moneyPrefix =
                            if (money > 50 * 1000 * 1000) (if (money > 200 * 1000 * 1000) GREEN else YELLOW) else RED
                        Utils.addChatMessage(
                            g + "Purse: " + moneyPrefix + Utils.shortNumberFormat(
                                purseBalance.toDouble(),
                                0
                            ) + g + " - Bank: " +
                                    (if (bankBalance == -1f) YELLOW.toString() + "N/A" else moneyPrefix.toString() +
                                            if (isMe) "4.8b" else Utils.shortNumberFormat(
                                                bankBalance.toDouble(),
                                                0
                                            )) +
                                    if (networth > 0) "$g - Net: $moneyPrefix" + Utils.shortNumberFormat(
                                        networth.toDouble(),
                                        0
                                    ) else ""
                        )
                        overallScore += Math.min(2f, money / (100f * 1000 * 1000))
                        val activePet =
                            Utils.getElementAsString(
                                Utils.getElement(
                                    profile.latestProfile.petsInfo, "active_pet.type"
                                ),
                                "None Active"
                            )
                        val activePetTier =
                            Utils.getElementAsString(
                                Utils.getElement(
                                    profile.latestProfile.petsInfo,
                                    "active_pet.tier"
                                ), "UNKNOWN"
                            )
                        var col = NotEnoughUpdates.petRarityToColourMap[activePetTier]
                        if (col == null) col = LIGHT_PURPLE.toString()
                        Utils.addChatMessage(
                            g + "Pet    : " + col + WordUtils.capitalizeFully(
                                activePet.replace("_", " ")
                            )
                        )
                        var overall = "Skywars Main"
                        if (isMe) {
                            overall =
                                Utils.chromaString("Literally the best player to exist") // ego much
                        } else if (overallScore < 5 && bankBalance + purseBalance > 500 * 1000 * 1000) {
                            overall = GOLD.toString() + "Bill Gates"
                        } else if (overallScore > 9) {
                            overall =
                                Utils.chromaString("Didn't even think this score was possible")
                        } else if (overallScore > 8) {
                            overall =
                                Utils.chromaString("Mentally unstable")
                        } else if (overallScore > 7) {
                            overall = GOLD.toString() + "Why though 0.0"
                        } else if (overallScore > 5.5) {
                            overall = GOLD.toString() + "Bro stop playing"
                        } else if (overallScore > 4) {
                            overall = GREEN.toString() + "Kinda sweaty"
                        } else if (overallScore > 3) {
                            overall = YELLOW.toString() + "Alright I guess"
                        } else if (overallScore > 2) {
                            overall = YELLOW.toString() + "Ender Non"
                        } else if (overallScore > 1) {
                            overall = RED.toString() + "Played SkyBlock"
                        }
                        Utils.addChatMessage(
                            g + "Overall score: " + overall + g + " (" + Math.round(
                                overallScore * 10
                            ) / 10f + ")"
                        )
                    }
                })
            }
        }
    }

    @SubscribeEvent
    fun onCommand(event: RegisterBrigadierCommandEvent) {
        event.command("peek") {
            thenArgument("player", string()) { player ->
                suggestsList { Minecraft.getMinecraft().theWorld.playerEntities.map { it.name } }
                thenExecute {
                    executePeek(this[player])
                }
            }.withHelp("Quickly glance at other peoples stats")
            thenExecute {
                executePeek(Minecraft.getMinecraft().thePlayer.name)
            }
        }.withHelp("Quickly glance at your own stats")
    }
}
