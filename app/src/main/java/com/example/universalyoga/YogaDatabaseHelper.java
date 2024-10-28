package com.example.universalyoga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

        long classId = db.insert(TABLE_YOGA_CLASS, null, values);
        db.close();


        saveClassToFirebase(classId);
    }

    private void saveClassToFirebase(long classId) {
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

    public Yoga getYogaClassById(long Id){
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

    public void updateClassByIdWithIncludingSession(int id, String time, int duration, String description){
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


        int rowsAffected = db.update(TABLE_YOGA_CLASS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        yoga.setTime(time);
        yoga.setDuration(duration);
        yoga.setDescription(description);

        checkClassInFirebase(yoga);


//        db.close();
    }

    public void updateClassByIdWithNoSession(int id, String time, int duration, double price,String type, String dayOfWeek, String description, int capacity){
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


        int rowsAffected = db.update(TABLE_YOGA_CLASS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

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

    private void updateYogaClassToFireBase(Yoga yoga){
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
        int rowsDeleted = db.delete(TABLE_YOGA_CLASS, COLUMN_ID + " = ?", new String[]{String.valueOf(classId)});

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


        deleteSessionsFromFirebase(classId);
    }

    private void deleteSessionsFromFirebase(int classId) {
        DatabaseReference sessionsReference = firebaseDatabase.getReference("classInstances");

        sessionsReference.orderByChild("classId").equalTo(classId).addListenerForSingleValueEvent(new ValueEventListener() {
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



    public Session GetSessionById(long id) {
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

    public List<Session> GetSessionByClassId(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        List<Session> lstSession = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_CLASS_ID + " = ?";
        Cursor sessionCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)} );

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

        int result = db.update(TABLE_CLASS_INSTANCE, values, COLUMN_ID + " = ?", new String[]{String.valueOf(session.getId())});

        db.close();
        checkClassInstanceInFirebase(session.getId(), date, instructorid, comment, session.getClassId());
        return result > 0;
    }

    public List<Session> getAllSession(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<Session> lstSession = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE;
        Cursor sessionCursor = db.rawQuery(selectQuery,null );

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

    public boolean deleteSessionById(long id){
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

        SQLiteDatabase db = this.getWritableDatabase();

        HashPass hash = new HashPass();
        String hashedpass = hash.hashPassword(password);

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedpass);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_PHONE, phone);

        long id = db.insert(TABLE_USER, null, values);
        db.close();

        User newUser = new User(username, email, hashedpass, role, phone);
        saveUserToFirebase(newUser, id);
    }

    public String getUserNameById(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ? ";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
        String userName = null;
        if(cursor.moveToFirst()){
            userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }
        cursor.close();
        db.close();

        return userName;
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


    public boolean checkUser(String email, String password) {
        HashPass hashPass = new HashPass();
        String hashedPassword = hashPass.hashPassword(password);

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email, hashedPassword});

        boolean userExists = cursor.moveToFirst();
        cursor.close();
        return userExists;
    }



    public void addClassInstance(String date, int teacherId, String comments, int classId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectTeacherQuery = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor teacherCursor = db.rawQuery(selectTeacherQuery, new String[]{String.valueOf(teacherId)});

        if (teacherCursor.moveToFirst()) {
            String role = teacherCursor.getString(0);
            if (!"instructor".equals(role)) {
                throw new IllegalArgumentException("Invalid teacher ID or the user is not a teacher.");
            }
        } else {
            throw new IllegalArgumentException("Invalid teacher ID.");
        }

        String checkInstanceQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_DATE + " = ? AND " + COLUMN_TEACHER + " = ?";
        Cursor instanceCursor = db.rawQuery(checkInstanceQuery, new String[]{date, String.valueOf(teacherId)});

        if (instanceCursor.moveToFirst()) {
            instanceCursor.close();
            teacherCursor.close();
            throw new IllegalArgumentException("Class instance already exists for this teacher on the given date.");
        }
        instanceCursor.close();


        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, teacherId);
        values.put(COLUMN_COMMENTS, comments);
        values.put(COLUMN_CLASS_ID, classId);

        long instanceId = db.insert(TABLE_CLASS_INSTANCE, null, values);

        db.close();


        checkClassInstanceInFirebase(instanceId, date, teacherId, comments, classId);
    }

    private void checkClassInstanceInFirebase(long instanceId, String date, int teacherId, String comments, int classId) {

        Session classInstance = new Session();
        classInstance.setId((int) instanceId);
        classInstance.setDate(date);
        classInstance.setInstructorId(teacherId);
        classInstance.setComment(comments);
        classInstance.setClassId(classId);

        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");


        classInstancesReference.child(String.valueOf(classInstance.getId()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            saveClassInstanceToFirebase(classInstance);
                        } else {
                            updateClassInstanceInFirebase(classInstance);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("Error checking class instance in Firebase: " + databaseError.getMessage());
                    }
                });

    }


    private void saveClassInstanceToFirebase(Session classInstance) {
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

    private void updateClassInstanceInFirebase(Session classInstance) {
        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");

        classInstancesReference.child(String.valueOf(classInstance.getId()))
                .setValue(classInstance) // Phương thức này sẽ tự động cập nhật nếu node đã tồn tại
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
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))) ;
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                instructorList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return instructorList;
    }

    public void UpdateUser(User user, String newPhone, String newName) {


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


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");


        String sanitizedEmail = updatedUser.getEmail().replace(".", ",");


        Map<String, Object> updatedValues = new HashMap<>();
        updatedValues.put("username", updatedUser.getUsername());
        updatedValues.put("phone", updatedUser.getPhone());


        usersRef.child(sanitizedEmail).updateChildren(updatedValues).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseUpdate", "User updated successfully in Firebase.");
            } else {
                Log.e("FirebaseUpdate", "Failed to update user in Firebase: " + task.getException());
            }
        });
    }



    public String getUsernameByEmail(String email) {
        if (email == null || email.isEmpty()) {

        }else{
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
