package com.nothatcher.sproutbook

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import com.nothatcher.sproutbook.data.AppDatabase
import com.nothatcher.sproutbook.data.MIGRATION_11_12
import com.nothatcher.sproutbook.data.MIGRATION_12_13
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test fun originalSchemaMigratesWithoutLosingChild(): Unit = migrate(1)

    @Test fun v3InstallationMigratesWithoutLosingChild(): Unit = migrate(9)

    @Test fun v31InstallationMigratesWithoutLosingCareRecords(): Unit = migrate(10)

    @Test fun v35InstallationMigratesWithoutLosingFamilyAndCareRecords(): Unit = migrate(11)

    @Test fun v36InstallationMigratesWithoutLosingCustomizationAndWishlist(): Unit = migrate(12)

    private fun migrate(version: Int): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        val schema =
            InstrumentationRegistry.getInstrumentation()
                .context
                .assets
                .open("com.nothatcher.sproutbook.data.AppDatabase/$version.json")
                .bufferedReader()
                .use { JsonParser.parseReader(it).asJsonObject.getAsJsonObject("database") }
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            schema.getAsJsonArray("entities").forEach { element ->
                val entity = element.asJsonObject
                db.execSQL(
                    entity
                        .get("createSql")
                        .asString
                        .replace("\${TABLE_NAME}", entity.get("tableName").asString)
                )
                entity.getAsJsonArray("indices")?.forEach { index ->
                    db.execSQL(
                        index.asJsonObject
                            .get("createSql")
                            .asString
                            .replace("\${TABLE_NAME}", entity.get("tableName").asString)
                    )
                }
            }
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42,?)",
                arrayOf(schema.get("identityHash").asString),
            )
            db.execSQL(
                "INSERT INTO children (id,name,stage,notes,updatedAt) VALUES ('original','Rowan','BABY','Preserved',1)"
            )
            if (version >= 9)
                db.execSQL(
                    "INSERT INTO healthRecords (id,childId,kind,recordedAt,title,value,unit,notes,updatedAt) VALUES ('legacy-weight','original','Weight',1000,'Weight','3.215','kg','Precise',1)"
                )
            if(version>=10) db.execSQL("INSERT INTO diapers (id,childId,recordedAt,kind,notes,updatedAt) VALUES ('legacy-diaper','original',1000,'Mixed','Kept',1)")
            if (version >= 11) {
                db.execSQL("INSERT INTO routines (id,childId,title,weekdays,timeMinutes,owner,notes,active,updatedAt) VALUES ('legacy-routine','original','Bedtime',127,-1,'Caregiver','Kept',1,1)")
                db.execSQL("INSERT INTO routineCompletions (id,childId,routineId,day,updatedAt) VALUES ('legacy-completion','original','legacy-routine',1,1)")
                db.execSQL("INSERT INTO shoppingItems (id,childId,name,quantity,unit,checked,notes,updatedAt) VALUES ('legacy-shopping','original','Wipes',2,'packs',0,'Kept',1)")
            }
            if (version >= 12) {
                db.execSQL("UPDATE children SET accent='Sky',treeStyle='Autumn' WHERE id='original'")
                db.execSQL("INSERT INTO wishlistItems (id,childId,title,notes,link,obtained,updatedAt) VALUES ('legacy-wishlist','original','A book','Kept','',0,1)")
            }
            db.version = version
        }
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_11_12, MIGRATION_12_13).build()
        assertEquals("Preserved", db.children().all().single().notes)
        assertTrue(db.memorys().forChild("original").isEmpty())
        val child = db.children().all().single()
        assertEquals(if (version >= 12) "Sky" else "Forest", child.accent)
        assertEquals(if (version >= 12) "Autumn" else "Summer", child.treeStyle)
        assertEquals("", child.homeSections)
        assertEquals("", child.homeHiddenSections)
        assertEquals("memory,schedule", child.homeQuickActions)
        assertEquals("woodland", child.homeBackground)
        assertNull(child.homeBackgroundPhoto)
        if (version >= 12) assertEquals("Kept", db.wishlistItems().get("legacy-wishlist")!!.notes)
        else assertTrue(db.wishlistItems().forChild("original").isEmpty())
        if(version>=10) assertEquals("Kept",db.diapers().get("legacy-diaper")!!.notes)
        else assertTrue(db.diapers().forChild("original").isEmpty())
        if (version >= 11) {
            assertEquals("Kept", db.routines().get("legacy-routine")!!.notes)
            assertEquals("legacy-routine", db.routineCompletions().all().single().routineId)
            assertEquals("Kept", db.shoppingItems().get("legacy-shopping")!!.notes)
        } else {
            assertTrue(db.routines().forChild("original").isEmpty())
            assertTrue(db.shoppingItems().forChild("original").isEmpty())
        }
        assertTrue(db.pottyLogs().forChild("original").isEmpty())
        assertTrue(db.milkContainers().forChild("original").isEmpty())
        assertTrue(db.bottlePreps().forChild("original").isEmpty())
        if (version >= 9) assertEquals("3.215", db.healthRecords().get("legacy-weight")!!.value)
        assertEquals(AppDatabase.VERSION, db.openHelper.readableDatabase.version)
        db.close()
        context.deleteDatabase(name)
    }
}
