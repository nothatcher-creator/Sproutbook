package com.nothatcher.sproutbook.data

import com.nothatcher.sproutbook.core.WishlistRules
import kotlinx.coroutines.flow.Flow

fun FamilyRepository.observeWishlist(
    childId: String,
    limit: Int = 200,
    filter: String = "All",
): Flow<List<WishlistItem>> {
    require(limit > 0) { "Choose a valid list size." }
    require(filter in listOf("All", "Wanted", "Obtained")) { "Choose a Wishlist filter." }
    return db.wishlistItems().observe(childId, limit, filter)
}

suspend fun FamilyRepository.saveWishlist(item: WishlistItem) = write {
    validateText("Notes" to item.notes)
    require(item.id.length in 1..160) { "This item identity is invalid." }
    require(item.title.trim().length in 1..160) { "Enter an item title up to 160 characters." }
    val link = item.link.trim()
    require(WishlistRules.validLink(link)) { "Enter an HTTP or HTTPS link up to 2,048 characters, without a username or password." }
    require(db.children().get(item.childId) != null) { "Choose a child for this item." }
    require(db.wishlistItems().get(item.id)?.let { it.childId == item.childId } != false) {
        "This item belongs to another child."
    }
    db.wishlistItems().save(item.copy(title = item.title.trim(), link = link, updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.setWishlistObtained(id: String, childId: String, obtained: Boolean) = write {
    val item = db.wishlistItems().get(id) ?: error("This item no longer exists.")
    require(item.childId == childId) { "This item belongs to another child." }
    db.wishlistItems().save(item.copy(obtained = obtained, updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.deleteWishlist(id: String, childId: String) = write {
    val item = db.wishlistItems().get(id) ?: error("This item no longer exists.")
    require(item.childId == childId) { "This item belongs to another child." }
    db.wishlistItems().delete(id, childId)
}
