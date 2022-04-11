package am.barcodemanager.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Constants for Database name, table name, and column names
    public static final String DB_NAME = "Barcode";
    public static final String TABLE_NAME = "RollBarcode";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PALLET_NUMBER = "palletNumber";
    public static final String COLUMN_LOT_NUMBER = "lotNumber";
    public static final String COLUMN_ROLL_NUMBER = "rollNumber";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_METERS = "meters";
    public static final String COLUMN_ARTICLE_NUMBER = "articleNumber";
    public static final String COLUMN_STATUS = "status";

    //database version
    private static final int DB_VERSION = 1;

    //Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //creating the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " + COLUMN_PALLET_NUMBER + " VARCHAR, " + COLUMN_LOT_NUMBER +
                " VARCHAR, " + COLUMN_ROLL_NUMBER +
                " VARCHAR, " + COLUMN_WEIGHT +
                " VARCHAR, " + COLUMN_DATE +
                " VARCHAR, " + COLUMN_METERS +
                " VARCHAR, " + COLUMN_ARTICLE_NUMBER +
                " VARCHAR, " + COLUMN_STATUS +
                " TINYINT);";
        db.execSQL(sql);
    }

    //upgrading the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS Persons";
        db.execSQL(sql);
        onCreate(db);
    }

    /*
     * This method is taking two arguments
     * first one is the name that is to be saved
     * second one is the status
     * 0 means the name is synced with the server
     * 1 means the name is not synced with the server
     * */
    public boolean addRollNumber(String palletNo, String rollNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_PALLET_NUMBER, palletNo);
        contentValues.put(COLUMN_ROLL_NUMBER, rollNo);
//        contentValues.put(COLUMN_STATUS, Status);


        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    /*
     * This method taking two arguments
     * first one is the id of the name for which
     * we have to update the sync status
     * and the second one is the status that will be changed
     * */
    public boolean updateNameStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }

    /*
     * this method will give us all the name stored in sqlite
     * */
    public Cursor getRollInfo() {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC;";

        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        return c;
    }
   /* public Cursor getRollInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, new String[]{DatabaseHelper.COLUMN_PALLET_NUMBER, DatabaseHelper.COLUMN_ROLL_NUMBER, DatabaseHelper.COLUMN_STATUS}, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }*/

    /*
     * this method is for getting all the unsynced name
     * so that we can sync it with database
     * */
    public Cursor getUnsyncedRoll() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public boolean deleteCart(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_NAME);
            return true;

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return true;

    }
}
