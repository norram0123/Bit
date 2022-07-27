package com.norram.bit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoriteOpenHelper constructor(context: Context) : SQLiteOpenHelper(context, DBName, null, VERSION) {
    companion object {
        private const val DBName = "FAVORITE_DB"
        private const val VERSION = 1 // Database version(onUpgrade method is run when growing in value)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE FAVORITE_TABLE (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "url TEXT, " +
                "name TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FAVORITE_TABLE")
        onCreate(db)
    }
}