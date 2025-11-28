package com.example.cafeteriaexamen.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CafeteriaExamen.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla de usuarios
    public static final String TABLE_USERS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_TELEFONO = "telefono";

    // Tabla de valoraciones
    public static final String TABLE_VALORACIONES = "valoraciones";
    private static final String COLUMN_VALORACION_ID = "id";
    private static final String COLUMN_VALORACION = "valoracion";
    private static final String COLUMN_FECHA = "fecha";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOMBRE + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_TELEFONO + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // ✅ CREAR TABLA DE VALORACIONES
        String CREATE_VALORACIONES_TABLE = "CREATE TABLE " + TABLE_VALORACIONES + "("
                + COLUMN_VALORACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_VALORACION + " INTEGER NOT NULL,"
                + COLUMN_FECHA + " INTEGER NOT NULL" + ")";
        db.execSQL(CREATE_VALORACIONES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Registrar nuevo usuario
    public boolean registrarUsuario(String nombre, String email, String password, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_TELEFONO, telefono);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Verificar login
    public boolean verificarLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Obtener datos del usuario
    public Cursor obtenerUsuario(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_NOMBRE, COLUMN_EMAIL, COLUMN_TELEFONO},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
    }

    // Actualizar perfil
    public boolean actualizarUsuario(String nombre, String email, String password, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("password", password);
        values.put("telefono", telefono);

        int rowsAffected = db.update("usuarios", values, "email = ?", new String[]{email});
        return rowsAffected > 0;
    }
    public void guardarValoracion(int valoracion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VALORACION, valoracion);
        values.put(COLUMN_FECHA, System.currentTimeMillis());
        db.insert(TABLE_VALORACIONES, null, values);
        db.close();
        System.out.println("💾 Valoración guardada: " + valoracion + "/10");
    }



    public double obtenerSatisfaccionGlobal() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + COLUMN_VALORACION + ") FROM " + TABLE_VALORACIONES;
        Cursor cursor = db.rawQuery(query, null);
        double promedio = 0;
        if (cursor.moveToFirst()) {
            promedio = cursor.getDouble(0);
        }
        cursor.close();
        System.out.println("📊 Satisfacción global calculada: " + promedio);
        return promedio;
    }
}