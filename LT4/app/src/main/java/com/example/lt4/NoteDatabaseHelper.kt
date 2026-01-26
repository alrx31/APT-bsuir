package com.example.lt4

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteException
import android.util.Log

class NoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext
    @Volatile var lastError: String? = null
        private set

    companion object {
        private const val DATABASE_NAME = "notes.db"
        // Bump when schema changes.
        // v2 adds the 'date' column, v3 adds missing 'title'/'content' migrations for older DBs.
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "notes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_DATE = "date"
        private const val TAG = "MyNotesDB"
    }

    init {
        // Helpful when DB creation/insert fails on a device/emulator.
        Log.d(TAG, "DB path=${appContext.getDatabasePath(DATABASE_NAME)} version=$DATABASE_VERSION")
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val createTable = """
                CREATE TABLE $TABLE_NAME (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TITLE TEXT NOT NULL,
                    $COLUMN_CONTENT TEXT NOT NULL,
                    $COLUMN_DATE TEXT NOT NULL
                )
            """.trimIndent()
            db.execSQL(createTable)
            Log.i(TAG, "onCreate(): table '$TABLE_NAME' created")
        } catch (ex: SQLiteException) {
            Log.e(TAG, "onCreate(): failed to create table '$TABLE_NAME'", ex)
            throw ex
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            Log.w(TAG, "onUpgrade(): $oldVersion -> $newVersion, migrating schema for '$TABLE_NAME'")
            ensureSchema(db)
        } catch (ex: SQLiteException) {
            Log.e(TAG, "onUpgrade(): failed for table '$TABLE_NAME' ($oldVersion -> $newVersion)", ex)
            throw ex
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // If the app was updated but version check didn't trigger for some reason,
        // keep the schema consistent.
        try {
            if (!db.isReadOnly) {
                ensureSchema(db)
            }
        } catch (ex: SQLiteException) {
            Log.e(TAG, "onOpen(): ensureSchema failed", ex)
        }
    }

    private fun ensureSchema(db: SQLiteDatabase) {
        // Create table if missing, otherwise add missing columns (e.g. 'date').
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL
            )
            """.trimIndent()
        )

        val existingColumns = mutableSetOf<String>()
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_NAME)", null)
        cursor.use {
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIndex >= 0) existingColumns.add(it.getString(nameIndex))
            }
        }

        // Some older variants of this lab used 'description' (or misspelled 'descriotion') instead of 'content'
        val legacyDescriptionName = when {
            existingColumns.contains("description") -> "description"
            existingColumns.contains("descriotion") -> "descriotion"
            else -> null
        }

        if (!existingColumns.contains(COLUMN_TITLE)) {
            Log.w(TAG, "ensureSchema(): adding missing column '$COLUMN_TITLE'")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_TITLE TEXT NOT NULL DEFAULT ''")
        }

        if (!existingColumns.contains(COLUMN_CONTENT)) {
            Log.w(TAG, "ensureSchema(): adding missing column '$COLUMN_CONTENT'")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_CONTENT TEXT NOT NULL DEFAULT ''")

            // Best-effort data migration from legacy column name.
            if (legacyDescriptionName != null && hasColumn(db, TABLE_NAME, legacyDescriptionName)) {
                Log.w(TAG, "ensureSchema(): migrating legacy '$legacyDescriptionName' -> '$COLUMN_CONTENT'")
                try {
                    db.execSQL("UPDATE $TABLE_NAME SET $COLUMN_CONTENT = $legacyDescriptionName WHERE $COLUMN_CONTENT = ''")
                } catch (ex: SQLiteException) {
                    // If the legacy column doesn't actually exist (corrupt/odd schema), don't crash.
                    Log.e(TAG, "ensureSchema(): failed to migrate legacy '$legacyDescriptionName' -> '$COLUMN_CONTENT'", ex)
                }
            }
        }

        if (!existingColumns.contains(COLUMN_DATE)) {
            // Column was added in v2. Provide DEFAULT to satisfy NOT NULL constraint.
            Log.w(TAG, "ensureSchema(): adding missing column '$COLUMN_DATE'")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_DATE TEXT NOT NULL DEFAULT ''")
        }

        // If title exists but empty, populate it from content (nice-to-have for old rows).
        try {
            db.execSQL(
                "UPDATE $TABLE_NAME SET $COLUMN_TITLE = " +
                    "CASE WHEN length($COLUMN_CONTENT) > 50 THEN substr($COLUMN_CONTENT, 1, 50) || '...' ELSE $COLUMN_CONTENT END " +
                    "WHERE $COLUMN_TITLE = ''"
            )
        } catch (ex: SQLiteException) {
            Log.e(TAG, "ensureSchema(): failed to backfill '$COLUMN_TITLE' from '$COLUMN_CONTENT'", ex)
        }
    }

    private fun hasColumn(db: SQLiteDatabase, table: String, column: String): Boolean {
        return try {
            val cursor = db.rawQuery("PRAGMA table_info($table)", null)
            cursor.use {
                val nameIndex = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (nameIndex >= 0 && it.getString(nameIndex).equals(column, ignoreCase = true)) {
                        return true
                    }
                }
            }
            false
        } catch (ex: SQLiteException) {
            Log.e(TAG, "hasColumn($table, $column): failed", ex)
            false
        }
    }

    private data class ColumnInfo(
        val name: String,
        val notNull: Boolean,
        val hasDefault: Boolean
    )

    private fun getTableInfo(db: SQLiteDatabase, table: String): List<ColumnInfo> {
        val cols = mutableListOf<ColumnInfo>()
        val cursor = db.rawQuery("PRAGMA table_info($table)", null)
        cursor.use {
            val nameIndex = it.getColumnIndex("name")
            val notNullIndex = it.getColumnIndex("notnull")
            val defaultIndex = it.getColumnIndex("dflt_value")
            while (it.moveToNext()) {
                val name = if (nameIndex >= 0) it.getString(nameIndex) else continue
                val notNull = notNullIndex >= 0 && it.getInt(notNullIndex) == 1
                val hasDefault = defaultIndex >= 0 && !it.isNull(defaultIndex)
                cols.add(ColumnInfo(name, notNull, hasDefault))
            }
        }
        return cols
    }

    fun insertNote(title: String, content: String, date: String): Long {
        val db = writableDatabase
        return try {
            lastError = null
            val tableInfo = getTableInfo(db, TABLE_NAME)
            val values = ContentValues()

            // Fill known columns in whatever schema exists on the device.
            for (col in tableInfo) {
                when (col.name.lowercase()) {
                    COLUMN_ID -> Unit // autoincrement
                    COLUMN_TITLE -> values.put(col.name, title)
                    COLUMN_CONTENT -> values.put(col.name, content)
                    COLUMN_DATE -> values.put(col.name, date)
                    "description", "descriotion" -> values.put(col.name, content) // legacy schema expects this
                    else -> {
                        // If some old schema has extra NOT NULL columns without defaults, satisfy them.
                        if (col.notNull && !col.hasDefault && !values.containsKey(col.name)) {
                            values.put(col.name, "")
                        }
                    }
                }
            }

            val rowId = db.insertOrThrow(TABLE_NAME, null, values)
            Log.d(TAG, "insertNote(): inserted rowId=$rowId")
            rowId
        } catch (ex: SQLiteException) {
            // insert() sometimes returns -1 without throwing; insertOrThrow() gives us the real reason.
            lastError = ex.message
            Log.e(TAG, "insertNote(): failed (titleLen=${title.length}, contentLen=${content.length}, date=$date)", ex)
            -1L
        }
    }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        return try {
            val cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_ID DESC"
            )

            cursor.use {
                val idIndex = it.getColumnIndex(COLUMN_ID)
                val titleIndex = it.getColumnIndex(COLUMN_TITLE)
                val contentIndex = it.getColumnIndex(COLUMN_CONTENT)
                val dateIndex = it.getColumnIndex(COLUMN_DATE)

                while (it.moveToNext()) {
                    val id = it.getInt(idIndex)
                    val title = it.getString(titleIndex)
                    val content = it.getString(contentIndex)
                    val date = it.getString(dateIndex)
                    notes.add(Note(id, title, content, date))
                }
            }
            notes
        } catch (ex: SQLiteException) {
            Log.e(TAG, "getAllNotes(): failed", ex)
            emptyList()
        }
    }

    fun getNoteById(id: Int): Note? {
        val db = readableDatabase
        return try {
            val cursor = db.query(
                TABLE_NAME,
                null,
                "$COLUMN_ID = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(COLUMN_ID)
                    val titleIndex = it.getColumnIndex(COLUMN_TITLE)
                    val contentIndex = it.getColumnIndex(COLUMN_CONTENT)
                    val dateIndex = it.getColumnIndex(COLUMN_DATE)

                    val noteId = it.getInt(idIndex)
                    val title = it.getString(titleIndex)
                    val content = it.getString(contentIndex)
                    val date = it.getString(dateIndex)
                    Note(noteId, title, content, date)
                } else {
                    null
                }
            }
        } catch (ex: SQLiteException) {
            Log.e(TAG, "getNoteById($id): failed", ex)
            null
        }
    }

    fun updateNote(id: Int, title: String, content: String, date: String): Int {
        val db = writableDatabase
        return try {
            lastError = null
            val tableInfo = getTableInfo(db, TABLE_NAME)
            val values = ContentValues()
            for (col in tableInfo) {
                when (col.name.lowercase()) {
                    COLUMN_TITLE -> values.put(col.name, title)
                    COLUMN_CONTENT -> values.put(col.name, content)
                    COLUMN_DATE -> values.put(col.name, date)
                    "description", "descriotion" -> values.put(col.name, content)
                }
            }
            val rows = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "updateNote($id): rows=$rows")
            rows
        } catch (ex: SQLiteException) {
            lastError = ex.message
            Log.e(TAG, "updateNote($id): failed", ex)
            0
        }
    }

    fun deleteNote(id: Int): Int {
        val db = writableDatabase
        return try {
            lastError = null
            val rows = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "deleteNote($id): rows=$rows")
            rows
        } catch (ex: SQLiteException) {
            lastError = ex.message
            Log.e(TAG, "deleteNote($id): failed", ex)
            0
        }
    }
}
