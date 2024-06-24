package com.example.cocoro_messenger

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FriendSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "friends_db"
        private const val TABLE_FRIENDS = "friends"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_FRIENDS_TABLE = ("CREATE TABLE " + TABLE_FRIENDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT," + KEY_PHONE + " TEXT" + ")")
        db?.execSQL(CREATE_FRIENDS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FRIENDS")
        onCreate(db)
    }

    fun addFriends(friends: List<Friend>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (friend in friends) {
                val values = ContentValues()
                values.put(KEY_ID, friend.id)
                values.put(KEY_NAME, friend.name)
                values.put(KEY_EMAIL, friend.email)
                values.put(KEY_PHONE, friend.phone)
                db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllFriends(): List<Friend> {
        val friendList = ArrayList<Friend>()
        val selectQuery = "SELECT * FROM $TABLE_FRIENDS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val friend = Friend(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE))
                )
                friendList.add(friend)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return friendList
    }
    fun deleteFriend(email: String) {
        val db = this.writableDatabase
        db.delete(TABLE_FRIENDS, "$KEY_EMAIL = ?", arrayOf(email))
        db.close()
    }
}
