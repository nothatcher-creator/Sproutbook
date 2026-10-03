package com.nothatcher.sproutbook.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds child customization and a separate local Wishlist; existing records stay in place. */
val MIGRATION_11_12 =
    object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE children ADD COLUMN accent TEXT NOT NULL DEFAULT 'Forest'")
            db.execSQL("ALTER TABLE children ADD COLUMN treeStyle TEXT NOT NULL DEFAULT 'Summer'")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS wishlistItems (id TEXT NOT NULL, childId TEXT NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL, link TEXT NOT NULL, obtained INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(childId) REFERENCES children(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_wishlistItems_childId ON wishlistItems (childId)")
        }
    }
