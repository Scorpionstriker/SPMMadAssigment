package com.dihanvisgaie.smartpantrymanager.database;

import android.provider.BaseColumns;

/**
    DatabaseSchema: defines the database schema in ONE place.

    Every table name, column name and CREATE TABLE statement lives here as a constant,
    so the rest of the app never types a raw column name (no typos, easy to change).
    Each inner class implements BaseColumns, which provides the "_id" primary key column.

    recipes 1 ───< ∞ recipe_ingredients        pantry_items (stand-alone)

    pantry_items and recipe_ingredients are NOT linked by a foreign key. They are compared
    at runtime through their normalized_name column by the strict-matching logic.
 */
public final class DatabaseSchema {

    public static final String DATABASE_NAME = "smart_pantry.db";

    /* Increase this whenever the schema changes, so onUpgrade() runs. */
    public static final int DATABASE_VERSION = 1;

    private DatabaseSchema() {
        // constants only: no instances
    }

    // pantry_items: what the user has at home this is full CRUD

    public static final class PantryTable implements BaseColumns {
        public static final String TABLE_NAME = "pantry_items";
        public static final String COL_NAME = "name";
        // as typed, e.g. "Tomatoes"
        public static final String COL_NORMALIZED_NAME = "normalized_name";
        // for matching, e.g. "tomato"
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_UNIT = "unit";
        public static final String COL_EXPIRY_DATE = "expiry_date";
        // "yyyy-MM-dd" or NULL

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_NAME + " TEXT NOT NULL, "
                        + COL_NORMALIZED_NAME + " TEXT NOT NULL, "
                        + COL_QUANTITY + " REAL NOT NULL CHECK (" + COL_QUANTITY + " > 0), "
                        + COL_UNIT + " TEXT NOT NULL, "
                        + COL_EXPIRY_DATE + " TEXT"
                        + ")";

        private PantryTable() { }
    }

    // recipes: seeded on first run, read-only in the app

    public static final class RecipeTable implements BaseColumns {
        public static final String TABLE_NAME = "recipes";
        public static final String COL_NAME = "name";
        public static final String COL_STEPS = "steps";
        // one step per line ("\n"-separated)

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_NAME + " TEXT NOT NULL UNIQUE, "
                        + COL_STEPS + " TEXT NOT NULL"
                        + ")";

        private RecipeTable() { }
    }

    // recipe_ingredients: the ingredients each recipe needs many per recipe

    public static final class RecipeIngredientTable implements BaseColumns {
        public static final String TABLE_NAME = "recipe_ingredients";
        public static final String COL_RECIPE_ID = "recipe_id";
        // FK -> recipes._id
        public static final String COL_NAME = "name";
        public static final String COL_NORMALIZED_NAME = "normalized_name";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_UNIT = "unit";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_RECIPE_ID + " INTEGER NOT NULL, "
                        + COL_NAME + " TEXT NOT NULL, "
                        + COL_NORMALIZED_NAME + " TEXT NOT NULL, "
                        + COL_QUANTITY + " REAL NOT NULL CHECK (" + COL_QUANTITY + " > 0), "
                        + COL_UNIT + " TEXT NOT NULL, "
                        + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                        + RecipeTable.TABLE_NAME + "(" + RecipeTable._ID + ") ON DELETE CASCADE"
                        + ")";

        /* Speeds up get all ingredients for recipe X lookups. */
        public static final String CREATE_INDEX =
                "CREATE INDEX idx_recipe_ingredients_recipe_id ON "
                        + TABLE_NAME + "(" + COL_RECIPE_ID + ")";

        private RecipeIngredientTable() { }
    }
}

