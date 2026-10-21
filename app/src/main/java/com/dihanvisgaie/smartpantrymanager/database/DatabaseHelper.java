package com.dihanvisgaie.smartpantrymanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.PantryTable;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeIngredientTable;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeTable;

/**
    DatabaseHelper: creates, opens and upgrades the on-device SQLite database.

    It is a SINGLETON: the whole app shares one helper and therefore one database
    connection. This avoids database is locked errors and leaked connections that
    happen when every screen opens its own helper.

    Lifecycle of the database file smart_pantry.db, stored in the app's private storage:
    first launch        -> onConfigure() then onCreate(): tables created + recipes seeded
    every later launch  -> onConfigure() then the existing file is opened data persists
    DATABASE_VERSION++  -> onUpgrade()
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    /**
        Returns the single shared helper. The application context is used so that
        no Activity gets leaked by being held in this static field.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseSchema.DATABASE_NAME, null, DatabaseSchema.DATABASE_VERSION);
    }

    /**
        Called every time the database is opened, before onCreate/onUpgrade.
        SQLite has foreign keys switched OFF by default, so they are switched on here.
        This makes ON DELETE CASCADE work for recipe_ingredients.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
        Runs ONCE, the very first time the app opens the database the file doesn't exist yet.
        Android already wraps this method in a transaction, so if seeding fails, the
        tables are not left half-filled.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PantryTable.CREATE_TABLE);
        db.execSQL(RecipeTable.CREATE_TABLE);
        db.execSQL(RecipeIngredientTable.CREATE_TABLE);
        db.execSQL(RecipeIngredientTable.CREATE_INDEX);

        // Pre-load the 20 starter recipes seeded on first run in the brief
        RecipePreLouder.seed(db);
    }

    /**
        Runs when DATABASE_VERSION is increased. For this project the simplest safe approach
        is to drop and rebuild everything. NOTE: this also clears the user's pantry.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecipeIngredientTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PantryTable.TABLE_NAME);
        onCreate(db);
    }
}