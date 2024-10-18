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
                        // Nếu lớp học không tồn tại, lưu lên Firebase
                        if (!dataSnapshot.exists()) {
                            saveClassToFirebase(yogaClass);
                        } else {
                            System.out.println("Class already exists in Firebase: ID = " + yogaClass.getId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("Error checking class in Firebase: " + databaseError.getMessage());
                    }
                });
    }

    private void saveClassToFirebase(Yoga yogaClass) {
        // Get a unique ID for the class (if needed)
        DatabaseReference classesReference = firebaseDatabase.getReference("yogaClasses");
        String classId = classesReference.push().getKey();

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


    // Method to add new User
    public void addUser(String username, String email, String password, String role, String phone) {
        // 1. Lưu user vào SQLite trước
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_PHONE, phone);

        db.insert(TABLE_USER, null, values);
        db.close();

        // 2. Lưu user mới vào Firebase
        User newUser = new User(username, email, password, role, phone);
        saveUserToFirebase(newUser);
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

                // Cập nhật user lên Firebase, sử dụng email làm khóa
                saveUserToFirebase(user);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void saveUserToFirebase(User user) {
        // Chuẩn hóa email để làm khóa (thay dấu chấm bằng dấu phẩy)
        String sanitizedEmail = user.getEmail().replace(".", ",");

        // Tạo một bản đồ chứa các giá trị của user
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("id", user.getId());
        userValues.put("username", user.getUsername());
        userValues.put("email", user.getEmail());
        userValues.put("password", user.getPassword());
        userValues.put("role", user.getRole());
        userValues.put("phone", user.getPhone());

        // Lưu user vào Firebase dưới khóa là email đã chuẩn hóa
        usersReference.child(sanitizedEmail).updateChildren(userValues)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Xử lý khi lưu thành công
                        System.out.println("User saved to Firebase successfully!");
                    } else {
                        // Xử lý khi thất bại
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
            return true;
        }

        cursor.close();
        return false;
    }



    public void addClassInstance(String date, int teacherId, String comments, int classId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the teacherId is valid and if they are actually a teacher
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

        // Check if the class instance already exists with the same date and teacherId
        String checkInstanceQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_DATE + " = ? AND " + COLUMN_TEACHER + " = ?";
        Cursor instanceCursor = db.rawQuery(checkInstanceQuery, new String[]{date, String.valueOf(teacherId)});

        if (instanceCursor.moveToFirst()) {
            instanceCursor.close();
            teacherCursor.close();
            throw new IllegalArgumentException("Class instance already exists for this teacher on the given date.");
        }
        instanceCursor.close();

        // Proceed to insert the new class instance into SQLite
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, teacherId);
        values.put(COLUMN_COMMENTS, comments);
        values.put(COLUMN_CLASS_ID, classId);

        long instanceId = db.insert(TABLE_CLASS_INSTANCE, null, values);

        // Close the database after insertion
        db.close();

        // Now save this instance to Firebase if it is not already there
        saveClassInstanceToFirebase(instanceId, date, teacherId, comments, classId);
    }

    private void saveClassInstanceToFirebase(long instanceId, String date, int teacherId, String comments, int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(instanceId)});

        if (cursor.moveToFirst()) {
            // Create a new ClassInstance object (assuming you have a ClassInstance class)
            Session classInstance = new Session();
            classInstance.setId(cursor.getInt(0)); // Assuming the first column is the ID
            classInstance.setDate(cursor.getString(1));
            classInstance.setInstructorId(cursor.getInt(2)); // Assuming teacher is an int ID
            classInstance.setComment(cursor.getString(3));
            classInstance.setClassId(cursor.getInt(4));

            // Check if the class instance already exists in Firebase
            checkClassInstanceInFirebase(classInstance);
        }

        cursor.close();
        db.close();
    }

    private void checkClassInstanceInFirebase(Session classInstance) {
        DatabaseReference classInstancesReference = firebaseDatabase.getReference("classInstances");
        classInstancesReference.orderByChild("id").equalTo(classInstance.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // If the class instance does not exist, save it to Firebase
                        if (!dataSnapshot.exists()) {
                            saveClassInstanceToFirebase(classInstance);
                        } else {
                            System.out.println("Class instance already exists in Firebase: ID = " + classInstance.getId());
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
        String instanceId = classInstancesReference.push().getKey(); // Generate a unique ID for Firebase

        classInstancesReference.child(instanceId).setValue(classInstance)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Class instance saved to Firebase successfully!");
                    } else {
                        System.out.println("Failed to save class instance to Firebase.");
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

        // 1. Cập nhật dữ liệu trong SQLite
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Thêm các giá trị mới cho cột trong SQLite
        values.put(COLUMN_PHONE, newPhone);
        values.put(COLUMN_USERNAME, newName);

        // Cập nhật dữ liệu trong SQLite, tìm bằng email của người dùng
        int result = db.update(TABLE_USER, values, COLUMN_EMAIL + " = ?", new String[]{user.getEmail()});
        if (result == -1) {
            Log.e("SQLiteUpdate", "Failed to update user in SQLite.");
            return;
        }

        // 2. Sau khi cập nhật thành công trong SQLite, lấy dữ liệu mới nhất từ SQLite
        User updatedUser = getUserByEmail(user.getEmail());
        if (updatedUser == null) {
            Log.e("SQLiteFetch", "Failed to fetch updated user from SQLite.");
            return;
        }

        // 3. Cập nhật dữ liệu từ SQLite lên Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        // Chuẩn hóa email để tránh dấu chấm trong Firebase keys
        String sanitizedEmail = updatedUser.getEmail().replace(".", ",");

        // Tạo HashMap để chứa các giá trị cần cập nhật trong Firebase
        Map<String, Object> updatedValues = new HashMap<>();
        updatedValues.put("username", updatedUser.getUsername());
        updatedValues.put("phone", updatedUser.getPhone());

        // Cập nhật dữ liệu trong Firebase
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
