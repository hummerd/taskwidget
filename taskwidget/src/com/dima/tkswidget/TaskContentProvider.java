package com.dima.tkswidget;

import com.dima.tkswidget.TaskMetadata.EntityMetaInfo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class TaskContentProvider extends ContentProvider {

  private static final String DATABASE_NAME = "tkswidget.db";
  private static final String TABLE_NAME_TASK = "task";
  private static final String TABLE_NAME_TASK_LIST = "task_list";
  private static final int DATABASE_VERSION = 4;


  static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      // calls the super constructor, requesting the default cursor factory.
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     * Creates the underlying database with table name and column names taken from the
     * NotePad class.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME_TASK + " ("
          + TaskMetadata.COL_ID + " TEXT PRIMARY KEY,"
          + TaskMetadata.COL_CREATE_DATE + " INTEGER,"
          + TaskMetadata.COL_TITLE + " TEXT,"
          + TaskMetadata.COL_STATUS + " TEXT,"
          + TaskMetadata.COL_PARENT_LIST_ID + " TEXT,"
          + TaskMetadata.COL_PARENT_TASK_ID + " TEXT,"
          + TaskMetadata.COL_POSITION + " TEXT"
          + ");");

      db.execSQL("CREATE TABLE " + TABLE_NAME_TASK_LIST + " ("
          + TaskMetadata.COL_TL_ID + " TEXT PRIMARY KEY,"
          + TaskMetadata.COL_TL_TITLE + " TEXT,"
          + TaskMetadata.COL_TL_CREATE_DATE + " INTEGER"
          + ");");
    }

    /**
     *
     * Demonstrates that the provider must consider what happens when the
     * underlying datastore is changed. In this sample, the database is upgraded the database
     * by destroying the existing data.
     * A real application should upgrade the database in place.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

      // Logs that the database is being upgraded
      LogHelper.w("Upgrading database from version " + oldVersion + " to "
          + newVersion + ", which will destroy all old data");

      // Kills the table and existing data
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TASK);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TASK_LIST);

      // Recreates the database with a new version
      onCreate(db);
    }
  }

  private DatabaseHelper mOpenHelper;

  @Override
  public boolean onCreate() {
    mOpenHelper = new DatabaseHelper(getContext());
    return true;
  }

  @Override
  public String getType(Uri uri) {

    EntityMetaInfo metaInfo = TaskMetadata.GetUriMetdata(uri);
    if (metaInfo == null)
      throw new IllegalArgumentException("Unknown URI " + uri);

    int kind = TaskMetadata.CheckUriKind(uri);
    boolean byId = kind == TaskMetadata.URI_KIND_LIST_ID
        || kind == TaskMetadata.URI_KIND_TASK_ID;

    return byId
        ? metaInfo.ITEM_CONTENT_TYPE
        : metaInfo.DIR_CONTENT_TYPE;
  }


  @Override
  public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    int kind = TaskMetadata.CheckUriKind(uri);
    boolean byId = kind == TaskMetadata.URI_KIND_LIST_ID
        || kind == TaskMetadata.URI_KIND_TASK_ID;

    Cursor c = queryTable(uri, byId, sortOrder, projection, selection, selectionArgs);
    // Tells the Cursor what URI to watch, so it knows when its source data changes
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Override
  public Uri insert(Uri uri, ContentValues initialValues) {
    return insertTable(uri, initialValues);
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
    int kind = TaskMetadata.CheckUriKind(uri);
    boolean byId = kind == TaskMetadata.URI_KIND_LIST_ID
        || kind == TaskMetadata.URI_KIND_TASK_ID;

    return deleteTable(uri, where, whereArgs, byId);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    // TODO Auto-generated method stub
    return 0;
  }


  private Cursor queryTable(
      Uri uri,
      boolean useId,
      String sortOrder,
      String[] projection,
      String selection,
      String[] selectionArgs) {

    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    EntityMetaInfo metaInfo = TaskMetadata.GetUriMetdata(uri);
    if (metaInfo == null)
      throw new IllegalArgumentException("Unknown URI " + uri);
    qb.setTables(metaInfo.TABLE_NAME);

    if (useId) {
      qb.appendWhere(
          getWhereClause(metaInfo, uri));
    }

    String orderBy = TextUtils.isEmpty(sortOrder)
        ? metaInfo.DEFAULT_SORT_ORDER
        : sortOrder;

    // Opens the database object in "read" mode, since no writes need to be done.
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
	
	    /*
	     * Performs the query. If no problems occur trying to read the database, then a Cursor
	     * object is returned; otherwise, the cursor variable contains null. If no records were
	     * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
	     */
    return qb.query(
        db,            // The database to query
        projection,    // The columns to return from the query
        selection,     // The columns for the where clause
        selectionArgs, // The values for the where clause
        null,          // don't group the rows
        null,          // don't filter by row groups
        orderBy        // The sort order
    );
  }

  private Uri insertTable(
      Uri uri,
      ContentValues initialValues) {

    int kind = TaskMetadata.CheckUriKind(uri);
    if (kind != TaskMetadata.URI_KIND_LISTS
        && kind != TaskMetadata.URI_KIND_TASKS) {
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    // A map to hold the new record's values.
    ContentValues values;

    if (initialValues != null) {
      values = new ContentValues(initialValues);
    } else {
      values = new ContentValues();
    }

    // Gets the current system time in milliseconds
    // Long now = Long.valueOf(System.currentTimeMillis());
    EntityMetaInfo metaInfo = TaskMetadata.GetUriMetdata(uri);

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    // Performs the insert and returns the ID of the new note.
    long rowId = db.insert(
        metaInfo.TABLE_NAME,
        metaInfo.COL_NULL,
        values
    );

    // If the insert succeeded, the row ID exists.
    if (rowId > 0) {
      // Creates a URI with the note ID pattern and the new row ID appended to it.
      Uri notifyUri = ContentUris.withAppendedId(metaInfo.CONTENT_ITEM_PATTERN, rowId);

      // Notifies observers registered against this provider that the data changed.
      getContext().getContentResolver().notifyChange(notifyUri, null);
      return notifyUri;
    }

    // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
    throw new SQLException("Failed to insert row into " + uri);
  }

  private int deleteTable(Uri uri, String where, String[] whereArgs, boolean useId) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    String finalWhere;

    EntityMetaInfo metaInfo = TaskMetadata.GetUriMetdata(uri);
    if (metaInfo == null)
      throw new IllegalArgumentException("Unknown URI " + uri);

    if (useId) {
      finalWhere = getWhereClause(metaInfo, uri);
      if (where != null) {
        finalWhere = finalWhere + " AND " + where;
      }
    } else {
      finalWhere = where;
    }

    int count = db.delete(
        metaInfo.TABLE_NAME,  // The database table name.
        finalWhere, // The final WHERE clause
        whereArgs   // The incoming where clause values.
    );

    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows deleted.
    return count;
  }

  private String getWhereClause(EntityMetaInfo metaInfo, Uri uri){
    return metaInfo.COL_ID +
        "= '" +
        uri.getPathSegments().get(metaInfo.PATH_ID_POS) +
        "'";
  }
}
