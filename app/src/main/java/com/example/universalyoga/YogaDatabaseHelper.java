package com.example.universalyoga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YogaDatabaseHelper extends SQLiteOpenHelper {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersReference;
    DatabaseReference classesReference;
    DatabaseReference sessionsReference;
    DatabaseReference cartsReference;

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

        firebaseDatabase = FirebaseDatabase.getInstance("https://universal-yoga-df14b-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersReference = firebaseDatabase.getReference("users");
        classesReference = firebaseDatabase.getReference("yogaClasses");
        sessionsReference = firebaseDatabase.getReference("classInstances");
        cartsReference = firebaseDatabase.getReference("carts");
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

    // Method to add new Yoga Class
    public void addYogaClass(String dayOfWeek, String time, int capacity, int duration,
                             double price, String type, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CAPACITY, capacity);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);

        long classId = db.insert(TABLE_YOGA_CLASS, null, values);
        db.close();
        getClassFromSQLite(classId);
    }

    private void getClassFromSQLite(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(classId)});

        if (cursor.moveToFirst()) {
            Yoga yogaClass = new Yoga();
            yogaClass.setId(cursor.getInt(0));
            yogaClass.setDayOfWeek(cursor.getString(1));
            yogaClass.setTime(cursor.getString(2));
            yogaClass.setCapacity(cursor.getInt(3));
            yogaClass.setDuration(cursor.getInt(4));
            yogaClass.setPrice(cursor.getDouble(5));
            yogaClass.setType(cursor.getString(6));
            yogaClass.setDescription(cursor.getString(7));

            checkClassInFirebase(yogaClass);
        }
        cursor.close();
        db.close();
    }

    private void checkClassInFirebase(Yoga yogaClass) {
        DatabaseReference classesReference = firebaseDatabase.getReference("yogaClasses");

        classesReference.orderByChild("id").equalTo(yogaClass.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            saveClassToFirebase(yogaClass);
                        } else {
                            updateYogaClassToFireBase(yogaClass);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("Error checking class in Firebase: " + databaseError.getMessage());
                    }
                });
    }

    private void saveClassToFirebase(Yoga yogaClass) {
        DatabaseReference classesReference = firebaseDatabase.getReference("yogaClasses");

        String classId = String.valueOf(yogaClass.getId());

        classesReference.child(classId).setValue(yogaClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Yoga class saved to Firebase successfully!");
                    } else {
                        System.out.println("Failed to save yoga class to Firebase.");
                    }
                });
    }


    public List<Yoga> getAllYogaClasses() {
        List<Yoga> yogaClassList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Yoga yogaClass = new Yoga();
                yogaClass.setId(cursor.getInt(0));
                yogaClass.setDayOfWeek(cursor.getString(1));
                yogaClass.setTime(cursor.getString(2));
                yogaClass.setCapacity(cursor.getInt(3));
                yogaClass.setDuration(cursor.getInt(4));
                yogaClass.setPrice(cursor.getDouble(5));
                yogaClass.setType(cursor.getString(6));
                yogaClass.setDescription(cursor.getString(7));

                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return yogaClassList;
    }

    public Yoga getYogaClassById(long Id) {
        Yoga yogaClass = new Yoga();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Id)});

        if (cursor.moveToFirst()) {

            yogaClass.setId(cursor.getInt(0));
            yogaClass.setDayOfWeek(cursor.getString(1));
            yogaClass.setTime(cursor.getString(2));
            yogaClass.setCapacity(cursor.getInt(3));
            yogaClass.setDuration(cursor.getInt(4));
            yogaClass.setPrice(cursor.getDouble(5));
            yogaClass.setType(cursor.getString(6));
            yogaClass.setDescription(cursor.getString(7));

        }

        cursor.close();
        db.close();
        return yogaClass;
    }

    public void updateClassByIdWithIncludingSession(int id, String time, int duration, String description) {
        Yoga yoga = getYogaClassById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("dayOfWeek", yoga.getDayOfWeek());
        values.put("time", time);
        values.put("capacity", yoga.getCapacity());
        values.put("duration", duration);
        values.put("price", yoga.getPrice());
        values.put("type", yoga.getType());
        values.put("description", description);

        int rowsAffected = db.update(TABLE_YOGA_CLASS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});

        yoga.setTime(time);
        yoga.setDuration(duration);
        yoga.setDescription(description);

        checkClassInFirebase(yoga);

    }

    public void updateClassByIdWithNoSession(int id, String time, int duration, double price, String type,
                                             String dayOfWeek, String description, int capacity) {
        Yoga yoga = getYogaClassById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("dayOfWeek", dayOfWeek);
        values.put("time", time);
        values.put("capacity", capacity);
        values.put("duration", duration);
        values.put("price", price);
        values.put("type", type);
        values.put("description", description);

        int rowsAffected = db.update(TABLE_YOGA_CLASS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});

        yoga.setTime(time);
        yoga.setDuration(duration);
        yoga.setDescription(description);
        yoga.setDayOfWeek(dayOfWeek);
        yoga.setPrice(price);
        yoga.setType(type);
        yoga.setCapacity(capacity);

        checkClassInFirebase(yoga);

        db.close();
    }

    public boolean hasClassInstanceForYogaClass(int yogaClassId) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean hasInstance = false;

        String query = "SELECT 1 FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_CLASS_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(yogaClassId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                hasInstance = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return hasInstance;
    }

    private void updateYogaClassToFireBase(Yoga yoga) {
        DatabaseReference yogaClassesReference = firebaseDatabase.getReference("yogaClasses");

        yogaClassesReference.child(String.valueOf(yoga.getId()))
                .setValue(yoga)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Yoga class updated in Firebase successfully!");
                    } else {
                        System.out.println("Failed to update yoga class in Firebase.");
                    }
                });
    }

    public void deleteYogaClassById(int classId) {

        if (hasClassInstanceForYogaClass(classId)) {
            deleteSessionsForClass(classId);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_YOGA_CLASS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(classId)});

        if (rowsDeleted > 0) {
            System.out.println("Yoga class deleted from SQLite successfully!");
            deleteClassFromFirebase(classId);
        } else {
            System.out.println("Failed to delete yoga class from SQLite.");
        }

        db.close();
    }

    private void deleteSessionsForClass(int classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLASS_INSTANCE, COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(classId)});
        System.out.println("Sessions deleted from SQLite successfully for class ID: " + classId);
        deleteSessionsFromFirebaseByClassId(classId);
    }

    private void deleteSessionsFromFirebaseByClassId(int classId) {
        DatabaseReference sessionsReference = firebaseDatabase.getReference("classInstances");
        sessionsReference.orderByChild("classId").equalTo(classId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                        }
                        System.out.println("Sessions deleted from Firebase successfully for class ID: " + classId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Failed to delete sessions from Firebase: " + databaseError.getMessage());
                    }
                });
    }

    private void deleteClassFromFirebase(int classId) {
        DatabaseReference classesReference = firebaseDatabase.getReference("yogaClasses");
        String classKey = String.valueOf(classId);

        classesReference.child(classKey).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Yoga class deleted from Firebase successfully!");
                    } else {
                        System.out.println("Failed to delete yoga class from Firebase.");
                    }
                });
    }


    public Session getSessionById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Session session = null;
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_ID + " = ?";
        Cursor sessionCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});


        if (sessionCursor.moveToFirst()) {
            session = new Session();
            session.setId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_ID)));
            session.setClassId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)));
            session.setComment(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
            session.setInstructorId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
            session.setDate(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_DATE)));
        }

        sessionCursor.close();
        return session;
    }

    public List<Session> getSessionByClassId(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Session> lstSession = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_CLASS_ID + " = ?";
        Cursor sessionCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});

        if (sessionCursor.moveToFirst()) {
            do {
                Session session = new Session();
                session.setId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_ID)));
                session.setClassId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)));
                session.setComment(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
                session.setInstructorId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
                session.setDate(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_DATE)));
                lstSession.add(session);
            } while (sessionCursor.moveToNext());
        }

        sessionCursor.close();
        return lstSession;
    }

    public boolean updateSession(Session session, int instructorid, String date, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Log.e("check session id", String.valueOf(session.getId()));

        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, instructorid);
        values.put(COLUMN_COMMENTS, comment);
        values.put(COLUMN_CLASS_ID, session.getClassId());

        int result = db.update(TABLE_CLASS_INSTANCE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(session.getId())});

        db.close();
        checkSessionInFirebase(session.getId(), date, instructorid, comment, session.getClassId());
        return result > 0;
    }

    public List<Session> getAllSession() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Session> lstSession = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE;
        Cursor sessionCursor = db.rawQuery(selectQuery, null);

        if (sessionCursor.moveToFirst()) {
            do {
                Session session = new Session();
                session.setId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_ID)));
                session.setClassId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)));
                session.setComment(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
                session.setInstructorId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
                session.setDate(sessionCursor.getString(sessionCursor.getColumnIndexOrThrow(COLUMN_DATE)));

                lstSession.add(session);
            } while (sessionCursor.moveToNext());
        }

        sessionCursor.close();
        db.close();
        return lstSession;
    }

    public boolean deleteSessionById(long id) {
        DatabaseReference classInstanceRef = firebaseDatabase.getReference("classInstances");
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("ClassInstance", "id = ?", new String[]{String.valueOf(id)});

        classInstanceRef.child(String.valueOf(id)).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Session deleted successfully from Firebase.");
                    } else {
                        System.out.println("Failed to delete session from Firebase.");
                    }
                });
        return rowsDeleted > 0;
    }

    // Method to add new User
    public void addUser(String username, String email, String password, String role, String phone) {

        if (isEmailExistInSQLite(email)) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_PHONE, phone);

        long id = db.insert(TABLE_USER, null, values);
        db.close();

        User newUser = new User(username, email, password, role, phone);

        checkEmailInFirebase(newUser, id);
    }

    private boolean isEmailExistInSQLite(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private void checkEmailInFirebase(User user, long id) {
        usersReference.orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    saveUserToFirebase(user, id);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveUserToFirebase(User user, long id) {
        String userId = String.valueOf(id);
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("id", userId);
        userValues.put("username", user.getUsername());
        userValues.put("email", user.getEmail());
        userValues.put("password", user.getPassword());
        userValues.put("role", user.getRole());
        userValues.put("phone", user.getPhone());

        usersReference.child(userId).updateChildren(userValues)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("User saved to Firebase successfully!");
                    } else {
                        System.out.println("Failed to save user to Firebase.");
                    }
                });
    }


    public String getUserNameById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ? ";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
        String userName = null;
        if (cursor.moveToFirst()) {
            userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }
        cursor.close();
        db.close();

        return userName;
    }


    public boolean checkUserForLogin(String email, String password) {
        HashPass hashPass = new HashPass();
        String hashedPassword = hashPass.hashPassword(password);

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email, hashedPassword});

        boolean userExists = cursor.moveToFirst();
        cursor.close();
        return userExists;
    }


    public void addSession(String date, int teacherId, String comments, int classId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectTeacherQuery = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor teacherCursor = db.rawQuery(selectTeacherQuery, new String[]{String.valueOf(teacherId)});

        if (teacherCursor.moveToFirst()) {
            String role = teacherCursor.getString(0);
            if (!"instructor".equals(role)) {
                return;
            }
        } else {
            return;
        }

        String checkSessionQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_DATE + " = ? AND " + COLUMN_TEACHER + " = ?";
        Cursor sessionCursor = db.rawQuery(checkSessionQuery, new String[]{date, String.valueOf(teacherId)});

        if (sessionCursor.moveToFirst()) {
            sessionCursor.close();
            teacherCursor.close();
            return;
        }
        sessionCursor.close();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, teacherId);
        values.put(COLUMN_COMMENTS, comments);
        values.put(COLUMN_CLASS_ID, classId);

        long instanceId = db.insert(TABLE_CLASS_INSTANCE, null, values);

        db.close();

        checkSessionInFirebase(instanceId, date, teacherId, comments, classId);
    }

    private void checkSessionInFirebase(long sessionId, String date, int teacherId, String comments, int classId) {

        Session session = new Session();
        session.setId((int) sessionId);
        session.setDate(date);
        session.setInstructorId(teacherId);
        session.setComment(comments);
        session.setClassId(classId);

        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");


        classInstancesReference.child(String.valueOf(session.getId()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            saveSessionToFirebase(session);
                        } else {
                            updateSessionInFirebase(session);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("Error checking class instance in Firebase: " + databaseError.getMessage());
                    }
                });

    }


    private void saveSessionToFirebase(Session classInstance) {
        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");

        classInstancesReference.child(String.valueOf(classInstance.getId()))
                .setValue(classInstance)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Class instance saved to Firebase successfully!");
                    } else {
                        System.out.println("Failed to save class instance to Firebase.");
                    }
                });
    }

    private void updateSessionInFirebase(Session session) {
        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");

        classInstancesReference.child(String.valueOf(session.getId()))
                .setValue(session)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Class instance updated in Firebase successfully!");
                    } else {
                        System.out.println("Failed to update class instance in Firebase.");
                    }
                });
    }


    public List<User> getInstructors() {
        List<User> instructorList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        String selectQuery = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_ROLE + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{"instructor"});

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                instructorList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return instructorList;
    }

    public void updateUser(User user, String newPhone, String newName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PHONE, newPhone);
        values.put(COLUMN_USERNAME, newName);

        int result = db.update(TABLE_USER, values, COLUMN_EMAIL + " = ?", new String[]{user.getEmail()});
        if (result == -1) {
            Log.e("SQLiteUpdate", "Failed to update user in SQLite.");
            return;
        }

        User updatedUser = getUserByEmail(user.getEmail());
        if (updatedUser == null) {
            Log.e("SQLiteFetch", "Failed to fetch updated user from SQLite.");
            return;
        }

        Map<String, Object> updatedValues = new HashMap<>();
        updatedValues.put("username", updatedUser.getUsername());
        updatedValues.put("phone", updatedUser.getPhone());


        usersReference.child(String.valueOf(user.getId())).updateChildren(updatedValues).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseUpdate", "User updated successfully in Firebase.");
            } else {
                Log.e("FirebaseUpdate", "Failed to update user in Firebase: " + task.getException());
            }
        });
    }


    public String getUsernameByEmail(String email) {
        if (email == null || email.isEmpty()) {

        } else {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            if (cursor.moveToFirst()) {
                String username = cursor.getString(0);
                cursor.close();
                return username;
            }

            cursor.close();
            return null;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        } else {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            User user = null;

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(0));
                user.setUsername(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setPassword(cursor.getString(3));
                user.setRole(cursor.getString(4));
                user.setPhone(cursor.getString(5));
            }

            cursor.close();
            return user;
        }
    }


    public String getUserRoleByEmail(String email) {
        if (email == null || email.isEmpty()) {

        } else {
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

        } else {
            SQLiteDatabase db = this.getWritableDatabase();

            String selectQuery = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{email});

            long id = -1;

            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
                cursor.close();
            }

            int rowsAffected = db.delete(TABLE_USER, COLUMN_EMAIL + " = ?", new String[]{email});

            if (rowsAffected > 0) {
                System.out.println("User deleted successfully from SQLite.");

                deleteUserFromFirebase(id);
            } else {
                System.out.println("No user found with the given email in SQLite.");
            }

            db.close();
        }

    }

    private void deleteUserFromFirebase(long userId) {

        String userIdString = String.valueOf(userId);


        usersReference.child(userIdString).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("User deleted successfully from Firebase.");
                    } else {
                        System.out.println("Failed to delete user from Firebase.");
                    }
                });
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

    public void syncAllDataToFirebase() {
        syncUsersToFirebase();
        syncClassesToFirebase();
        syncClassInstancesToFirebase();
    }

    public void syncUsersToFirebase() {
        Log.e("syncUsersToFirebase", "in");
        List<User> users = getAllUsers();
        DatabaseReference usersReference = firebaseDatabase.getReference("users");

        for (User user : users) {
            usersReference.child(String.valueOf(user.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        usersReference.child(String.valueOf(user.getId())).setValue(user)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {
                                        System.out.println("User " + user.getUsername() + " synced to Firebase successfully.");
                                        Log.e("saveToFirebase", user.toString());
                                    } else {
                                        System.out.println("Failed to sync user " + user.getUsername() + " to Firebase.");
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("Error syncing user " + user.getUsername() + ": " + databaseError.getMessage());
                }
            });
        }
    }

    private void syncClassesToFirebase() {
        List<Yoga> classes = getAllYogaClasses();

        for (Yoga yogaClass : classes) {
            classesReference.child(String.valueOf(yogaClass.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        classesReference.child(String.valueOf(yogaClass.getId())).setValue(yogaClass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        System.out.println("Class " + yogaClass.getType() + " synced to Firebase successfully.");
                                    } else {
                                        System.out.println("Failed to sync class " + yogaClass.getType() + " to Firebase.");
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("Error syncing class " + yogaClass.getType() + ": " + databaseError.getMessage());
                }
            });
        }
    }

    private void syncClassInstancesToFirebase() {
        List<Session> classInstances = getAllSession();
        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");

        for (Session classInstance : classInstances) {
            classInstancesReference.child(String.valueOf(classInstance.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        classInstancesReference.child(String.valueOf(classInstance.getId())).setValue(classInstance)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        System.out.println("Class instance " + classInstance.getId() + " synced to Firebase successfully.");
                                    } else {
                                        System.out.println("Failed to sync class instance " + classInstance.getId() + " to Firebase.");
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("Error syncing class instance " + classInstance.getId() + ": " + databaseError.getMessage());
                }
            });
        }
    }



    public void syncUsersFromFirebase() {
        usersReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !isUserInSQLite(user.getId()) && user.getId() != 0) {
                        saveUserToSQLite(user);
                    }
                }
                System.out.println("All users synced from Firebase to SQLite.");
            } else {
                System.out.println("Failed to sync users: " + task.getException().getMessage());
            }
        });
    }

    private boolean isUserInSQLite(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private void saveUserToSQLite(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, user.getId());
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_PHONE, user.getPhone());

        db.insert(TABLE_USER, null, values);
    }

    public List<Session> getSessionByInstructorId(int id) {
        if (id != 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_TEACHER + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
            List<Session> sessionList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Session session = new Session();
                    session.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    session.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    session.setInstructorId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)));
                    session.setClassId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)));
                    session.setComment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));
                    sessionList.add(session);
                } while (cursor.moveToNext());
            }

            db.close();
            return sessionList;
        }
        return null;

    }

    public void deleteAllDataOfFire() {

        classesReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("All classes deleted successfully.");
            } else {
                System.out.println("Failed to delete classes.");
            }
        });

        sessionsReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("All sessions deleted successfully.");
            } else {
                System.out.println("Failed to delete sessions.");
            }
        });

        cartsReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("All cart items deleted successfully.");
                Log.e("clearCart", "clear");
            } else {
                System.out.println("Failed to delete cart items.");
                Log.e("clearCart", "not clear");
            }
        });
    }

    public void resetDataInSQLite() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);

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
        db.close();
    }

}
