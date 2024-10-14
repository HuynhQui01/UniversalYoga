package com.example.universalyoga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class YogaDatabaseHelper extends SQLiteOpenHelper {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersReference;

    // Database Information
    private static final String DATABASE_NAME = "YogaApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_YOGA_CLASS = "YogaClass";
    private static final String TABLE_CLASS_INSTANCE = "ClassInstance";
    private static final String TABLE_USER = "User";

    // Yoga Class table columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DAY_OF_WEEK = "dayOfWeek";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";

    // Class Instance table columns
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TEACHER = "teacher";
    private static final String COLUMN_COMMENTS = "comments";
    private static final String COLUMN_CLASS_ID = "classId";

    // User table columns
    private static final String COLUMN_USER_ID = "userId";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role"; // "admin" or "customer"
    private static final String COLUMN_PHONE = "phone";

    public YogaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Yoga Class table
        String CREATE_YOGA_CLASS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_YOGA_CLASS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, "
                + COLUMN_TIME + " TEXT NOT NULL, "
                + COLUMN_CAPACITY + " INTEGER NOT NULL, "
                + COLUMN_DURATION + " INTEGER NOT NULL, "
                + COLUMN_PRICE + " REAL NOT NULL, "
                + COLUMN_TYPE + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT " + ")";
        db.execSQL(CREATE_YOGA_CLASS_TABLE);

        // Create User table
        String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_ROLE + " TEXT NOT NULL, " // admin or teacher or customer
                + COLUMN_PHONE + " TEXT " + ")";
        db.execSQL(CREATE_USER_TABLE);

        // Create Class Instance table with reference to User where role is 'teacher'
        String CREATE_CLASS_INSTANCE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CLASS_INSTANCE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + COLUMN_TEACHER + " INTEGER NOT NULL, "
                + COLUMN_COMMENTS + " TEXT, "
                + COLUMN_CLASS_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_TEACHER + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + ") "
                + "ON DELETE CASCADE ON UPDATE CASCADE, "
                + "FOREIGN KEY(" + COLUMN_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_CLASS_INSTANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void dropAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Query to get all table names
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);

                // Skip the metadata tables (e.g., android_metadata, sqlite_sequence)
                if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")) {
                    db.execSQL("DROP TABLE IF EXISTS " + tableName);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }


    // Method to add new Yoga Class
    public void addYogaClass(String dayOfWeek, String time, int capacity, int duration, double price, String type, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CAPACITY, capacity);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);

        db.insert(TABLE_YOGA_CLASS, null, values);
        db.close();
    }

    // Method to add new User
    public void addUser(String username, String email, String password, String role, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_PHONE, phone);

        db.insert(TABLE_USER, null, values);
        db.close();

        // After inserting into SQLite, save all users to Firebase
        saveAllUsersToFirebase();
    }

    public void saveAllUsersToFirebase() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_USER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setUsername(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPassword(cursor.getString(3));
                user.setRole(cursor.getString(4));
                user.setPhone(cursor.getString(5));

                // Save user to Firebase
                saveUserToFirebase(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void saveUserToFirebase(User user) {
        // Get a unique ID for the user
        String userId = usersReference.push().getKey();

        // Save user data to Firebase
        usersReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Handle success
                        System.out.println("User saved to Firebase successfully!");
                    } else {
                        // Handle failure
                        System.out.println("Failed to save user to Firebase.");
                    }
                });
    }

    // Method to check user login
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email, password});

        if (cursor.moveToFirst()) {
            cursor.close();
            return true; // User found
        }

        cursor.close();
        return false; // User not found
    }

    public void addClassInstance(String date, int teacherId, String comments, int classId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_ROLE + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(teacherId), "teacher"});

        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_TEACHER, teacherId);
            values.put(COLUMN_COMMENTS, comments);
            values.put(COLUMN_CLASS_ID, classId);

            db.insert(TABLE_CLASS_INSTANCE, null, values);
        } else {
            // Handle case where teacherId is not a valid teacher
            throw new IllegalArgumentException("Invalid teacher ID or the user is not a teacher.");
        }

        cursor.close();
        db.close();
    }

    public String getUsernameByEmail(String email) {
        if (email == null || email.isEmpty()) {

        }else{
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            if (cursor.moveToFirst()) {
                String username = cursor.getString(0);  // Get the username
                cursor.close();
                return username;
            }

            cursor.close();
            return null;
        }
        return null;
    }

    public String getUserRoleByEmail(String email) {
        if (email == null || email.isEmpty()) {

        }else {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            if (cursor.moveToFirst()) {
                String role = cursor.getString(0);
                cursor.close();
                return role;
            }

            cursor.close();
            return null;
        }
        return null;
    }


    public void deleteUserByEmail(String email) {
        if (email == null || email.isEmpty()) {

        }else {
            SQLiteDatabase db = this.getWritableDatabase();

            // Use an SQL delete statement to remove the user
            int rowsAffected = db.delete(TABLE_USER, COLUMN_EMAIL + " = ?", new String[]{email});

            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No user found with the given email.");
            }

            db.close();
        }

    }

    public String getPhoneByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        } else {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT " + COLUMN_PHONE + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            if (cursor.moveToFirst()) {
                String phone = cursor.getString(0);
                cursor.close();
                return phone;
            }

            cursor.close();
            return null;
        }
    }




    // Method to get all users
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_USER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setUsername(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPassword(cursor.getString(3));
                user.setRole(cursor.getString(4));
                user.setPhone(cursor.getString(5));

                userList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return userList;
    }
}
