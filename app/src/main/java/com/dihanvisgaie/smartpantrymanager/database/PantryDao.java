package com.dihanvisgaie.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.PantryTable;
import com.dihanvisgaie.smartpantrymanager.logic.IngredientManager;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import java.util.ArrayList;
import java.util.List;

/**
    PantryDao Data Access Object: all CRUD operations for the pantry_items table.

    Create -> insert()
    Read   -> getAll(), getById()
    Update -> update()
    Delete -> delete(), deleteAll()

    Activities never write SQL themselves; they call these methods.
    All queries use ? placeholders selectionArgs, never string concatenation of
    user input, which protects against SQL injection.
 */
public class PantryDao {

    private final DatabaseHelper dbHelper;

    public PantryDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // CREATE

    /**
        Inserts a new pantry item.

        @return the new row's id, or -1 if the insert failed
     */
    public long insert(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newId = db.insert(PantryTable.TABLE_NAME, null, toContentValues(item));
        if (newId != -1) {
            item.setId(newId);
        }
        return newId;
    }

    // READ

    /** Returns every pantry item, sorted alphabetically case-insensitive. */
    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // try-with-resources closes the Cursor automatically, even if an error occurs
        try (Cursor cursor = db.query(
                PantryTable.TABLE_NAME,
                null,                                         // all columns
                null, null,                                   // no WHERE clause
                null, null,                                   // no GROUP BY / HAVING
                PantryTable.COL_NAME + " COLLATE NO CASE ASC")) {

            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    /** Returns one pantry item, or null if no row has that id. */
    public PantryItem getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                PantryTable.TABLE_NAME,
                null,
                PantryTable._ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {

            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    // UPDATE

    /**
        Updates an existing item matched by its id.

        @return number of rows changed 1 on success, 0 if the id wasn't found
     */
    public int update(PantryItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                PantryTable.TABLE_NAME,
                toContentValues(item),
                PantryTable._ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // DELETE

    /** Deletes one item by id. Returns number of rows deleted. */
    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                PantryTable.TABLE_NAME,
                PantryTable._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** Deletes every pantry item used by "Clear pantry" in Settings. Recipes are untouched. */
    public int deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(PantryTable.TABLE_NAME, null, null);
    }

    // Mapping helpers: Java object <-> database row

    /**
        Converts a PantryItem into column/value pairs. The normalized name is calculated
        here, so every row saved is ready for strict matching "Tomatoes " -> "tomato".
     */
    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(PantryTable.COL_NAME, item.getName().trim());
        values.put(PantryTable.COL_NORMALIZED_NAME, IngredientManager.normalize(item.getName()));
        values.put(PantryTable.COL_QUANTITY, item.getQuantity());
        values.put(PantryTable.COL_UNIT, item.getUnit());

        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            values.putNull(PantryTable.COL_EXPIRY_DATE);
        } else {
            values.put(PantryTable.COL_EXPIRY_DATE, item.getExpiryDate());
        }
        return values;
    }

    /** Builds a PantryItem from the Cursor's current row. */
    private PantryItem fromCursor(Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow(PantryTable.COL_EXPIRY_DATE);

        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(PantryTable._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryTable.COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryTable.COL_NORMALIZED_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(PantryTable.COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(PantryTable.COL_UNIT)),
                cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
    }
}

