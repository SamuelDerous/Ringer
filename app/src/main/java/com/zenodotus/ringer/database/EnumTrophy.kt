package com.zenodotus.ringer.database

enum class EnumTrophy(
    val type: String,
    val typeName: String,
    val minValue: Int,
    val maxValue: Int,
    val title: String,
    val assetPath: String

) {
    // Streak bekers
    INIT_STREAK("streak", "INIT_STREAK", 0, 10, "Init Streak", "plastic.png"),
    BRONZE_STREAK("streak", "BRONZE_STREAK", 11, 20, "Bronze Streak", "bronze.png"),
    SILVER_STREAK("streak", "SILVER_STREAK", 21, 30, "Silver Streak", "silver.png"),
    GOLD_STREAK("streak", "GOLD_STREAK", 31, Int.MAX_VALUE, "Gold Streak", "gold.png"),

    // Cumulatief
    STARTER("cumulative", "STARTER", 0, 10, "Starter", "starter.png"),
    APPRENTICE("cumulative", "APPRENTICE", 11, 20, "Apprentice", "apprentice.png"),
    JOURNEYMAN("cumulative", "JOURNEYMAN", 21, 50, "Journeyman", "journeyman.png"),
    EXPERT("cumulative", "EXPERT", 51, 75, "Expert", "expert.png"),
    MASTER("cumulative", "MASTER", 76, 100, "Master", "master.png"),
    GOD("cumulative", "GOD", 101, Int.MAX_VALUE, "God", "god.png");

    companion object {

        fun findTrophy(type: String, value: Int): EnumTrophy {
            return values().first {
                it.type == type && value in it.minValue..it.maxValue
            }
        }

    }
}


