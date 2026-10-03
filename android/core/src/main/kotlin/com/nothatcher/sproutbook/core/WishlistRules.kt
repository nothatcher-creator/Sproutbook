package com.nothatcher.sproutbook.core

import java.net.URI
import java.util.Locale

object WishlistRules {
    /** Optional links remain ordinary web addresses; credentials are never stored in them. */
    fun validLink(link: String): Boolean {
        if (link.isEmpty()) return true
        if (link.length > 2048) return false
        return try {
            val uri = URI(link)
            uri.scheme?.lowercase(Locale.ROOT) in listOf("http", "https") &&
                !uri.host.isNullOrBlank() && uri.rawUserInfo == null &&
                (uri.port == -1 || uri.port in 1..65535)
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: java.net.URISyntaxException) {
            false
        }
    }
}
