package com.nothatcher.sproutbook.data
/** Matches the versioned backup transport limit; never silently truncates a parent's notes. */
internal fun validateText(vararg fields: Pair<String, String>) {
    fields.forEach { (label, value) ->
        require(value.length <= 10000) { "$label must be 10,000 characters or fewer." }
    }
}
