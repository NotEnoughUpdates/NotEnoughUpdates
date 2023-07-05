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

package io.github.moulberry.notenoughupdates.util.hypixelapi

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.groupBy
import kotlin.collections.mapNotNull
import kotlin.collections.mapValues
import kotlin.collections.maxOf
import kotlin.collections.sumOf
import kotlin.collections.toList
import kotlin.collections.toMap
import kotlin.collections.toSet

data class ProfileCollectionInfo(
    val collections: Map<String, CollectionInfo>,
    val craftedGenerators: Map<String, Int>,
) {
    data class CollectionInfo(
        val collection: Collection,
        val totalCollectionCount: BigInteger,
        val personalCollectionCount: BigInteger,
        val unlockedTiers: List<CollectionTier>,
    )

    class CollectionMetadata internal constructor() {
        lateinit var collections: Map<String, CollectionCategory>
            private set
        val allCollections by lazy { collections.values.flatMap { it.items.toList() }.toMap() }
    }

    class CollectionCategory internal constructor() {
        lateinit var items: Map<String, Collection>
            private set
    }

    class Collection internal constructor() {
        lateinit var name: String
            private set
        var maxTiers: Int = -1
            private set
        lateinit var tiers: List<CollectionTier>
            private set

        override fun toString(): String {
            return "Collection(name=$name, maxTiers=$maxTiers, tiers=$tiers)"
        }
    }

    class CollectionTier internal constructor() {
        var tier: Int = -1
            private set
        var amountRequired: Int = -1
            private set
        lateinit var unlocks: List<String>
            private set

        override fun toString(): String {
            return "CollectionTier(tier=$tier, amountRequired=$amountRequired, unlocks=$unlocks)"
        }
    }


    companion object {


        val generatorPattern = "^([^0-9]+)_([0-9]+)$".toRegex()

        val hypixelCollectionInfo: CompletableFuture<CollectionMetadata> by lazy {
            NotEnoughUpdates.INSTANCE.manager.apiUtils
                .newAnonymousHypixelApiRequest("resources/skyblock/collections")
                .requestJson()
                .thenApply {
                    NotEnoughUpdates.INSTANCE.manager.gson.fromJson(it, CollectionMetadata::class.java)
                }
        }

        fun getCollectionData(
            profileData: JsonObject,
            mainPlayer: String,
            collectionData: CollectionMetadata
        ): ProfileCollectionInfo? {
            val mainPlayerUUID = mainPlayer.replace("-", "")
            val members = profileData["members"] as? JsonObject ?: return null
            val mainPlayerData =
                (members[mainPlayerUUID] as? JsonObject ?: return null)
            val mainPlayerCollection = mainPlayerData["collection"] as? JsonObject ?: return null
            val memberCollections = members.entrySet().mapNotNull { (uuid, data) ->
                if (data !is JsonObject) return null
                data["collection"] as? JsonObject
            }
            val generators = members.entrySet().mapNotNull { (uuid, data) ->
                if (data !is JsonObject) return null
                data["crafted_generators"] as? JsonArray
            }.flatMap { it.toList() }
            return ProfileCollectionInfo(
                collectionData.allCollections.mapValues { (name, collection) ->
                    val totalCollection = memberCollections.sumOf { it[name]?.asBigInteger ?: BigInteger.ZERO }
                    val personalCollection = mainPlayerCollection[name]?.asBigInteger ?: BigInteger.ZERO
                    CollectionInfo(
                        collection,
                        totalCollection,
                        personalCollection,
                        collection.tiers.filter { BigInteger.valueOf(it.amountRequired.toLong()) <= totalCollection }
                    )
                },
                generators.toSet()
                    .mapNotNull {
                        val pattern = generatorPattern.matchEntire(it.asString) ?: return@mapNotNull null
                        pattern.groupValues[1] to pattern.groupValues[2].toInt()
                    }
                    .groupBy { it.first }
                    .mapValues {
                        it.value.maxOf { it.second }
                    }
                    .toMap()
            )
        }

        /**
         * This should be the json object returned by /skyblock/profiles at profiles.<somenumber>. (aka the root tag
         * should contain profile_id, members, cute_name, etc.)
         */
        @JvmStatic
        fun getCollectionData(profileData: JsonObject, mainPlayer: String): CompletableFuture<ProfileCollectionInfo?> {
            return hypixelCollectionInfo.thenApply {
                getCollectionData(profileData, mainPlayer, it)
            }
        }
    }

}
