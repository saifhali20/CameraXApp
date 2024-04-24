package com.example.cameraxapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // companion object to hold database constants
    companion object {
        private const val DATABASE_NAME = "images.db"
        private const val DATABASE_VERSION = 1
    }

    // creating database table
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE images (_id INTEGER PRIMARY KEY AUTOINCREMENT, image_uri TEXT NOT NULL)")
    }

    // upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS images")
        onCreate(db)
    }
}
