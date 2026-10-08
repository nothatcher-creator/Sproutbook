package com.nothatcher.sproutbook.core

/** Persisted names are shared by profiles and portable backups. */
enum class TreeTheme(val style: String, val description: String) {
    SUMMER("Summer", "Sunlit greens and meadow daisies"),
    AUTUMN("Autumn", "Amber foliage and woodland mushrooms"),
    NIGHT("Night", "Moonlit greens and quiet stars"),
    SPRING("Spring", "Fresh greens and little meadow flowers"),
    BLOSSOM("Blossom", "Rosy blossoms and drifting petals"),
    WINTER("Winter", "Icy blues and a dusting of snow"),
    RAINBOW("Rainbow", "Pastel colours and a rainbow overhead"),
}

object TreeThemes {
    val default = TreeTheme.SUMMER
    val all = TreeTheme.entries
    val names = all.map { it.style }

    fun find(style: String): TreeTheme? = all.firstOrNull { it.style == style }
}
