package com.nothatcher.sproutbook

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import com.nothatcher.sproutbook.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test fun originalSchemaMigratesWithoutLosingChild(): Unit = migrate(1)

    @Test fun v3InstallationMigratesWithoutLosingChild(): Unit = migrate(9)

    @Test fun v31InstallationMigratesWithoutLosingCareRecords(): Unit = migrate(10)

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
            if(version==10) db.execSQL("INSERT INTO diapers (id,childId,recordedAt,kind,notes,updatedAt) VALUES ('legacy-diaper','original',1000,'Mixed','Kept',1)")
            db.version = version
        }
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        assertEquals("Preserved", db.children().all().single().notes)
        assertTrue(db.memorys().forChild("original").isEmpty())
        if(version==10) assertEquals("Kept",db.diapers().get("legacy-diaper")!!.notes)
        else assertTrue(db.diapers().forChild("original").isEmpty())
        assertTrue(db.routines().forChild("original").isEmpty())
        assertTrue(db.pottyLogs().forChild("original").isEmpty())
        assertTrue(db.milkContainers().forChild("original").isEmpty())
        assertTrue(db.bottlePreps().forChild("original").isEmpty())
        assertTrue(db.shoppingItems().forChild("original").isEmpty())
        if (version >= 9) assertEquals("3.215", db.healthRecords().get("legacy-weight")!!.value)
        assertEquals(11, db.openHelper.readableDatabase.version)
        db.close()
        context.deleteDatabase(name)
    }
}
