package guardian.api.checkin;

import java.util.Date;
import java.util.List;

import rfcx.utility.datetime.DateTimeUtils;
import rfcx.utility.database.DbUtils;
import rfcx.utility.rfcx.RfcxLog;
import rfcx.utility.rfcx.RfcxRole;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import guardian.RfcxGuardian;

public class ApiCheckInDb {

	public ApiCheckInDb(Context context, String appVersion) {
		this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
		this.dbQueued = new DbQueued(context);
		this.dbSkipped = new DbSkipped(context);
		this.dbStashed = new DbStashed(context);
		this.dbSent = new DbSent(context);
	}
	
	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, ApiCheckInDb.class);
	private int VERSION = 1;
	static final String DATABASE = "checkin";
	static final String C_CREATED_AT = "created_at";
	static final String C_AUDIO = "audio";
	static final String C_JSON = "json";
	static final String C_ATTEMPTS = "attempts";
	static final String C_FILEPATH = "filepath";
	private static final String[] ALL_COLUMNS = new String[] { C_CREATED_AT, C_AUDIO, C_JSON, C_ATTEMPTS, C_FILEPATH };
	
	private String createColumnString(String tableName) {
		StringBuilder sbOut = new StringBuilder();
		sbOut.append("CREATE TABLE ").append(tableName)
			.append("(").append(C_CREATED_AT).append(" DATETIME")
			.append(", ").append(C_AUDIO).append(" TEXT")
			.append(", ").append(C_JSON).append(" TEXT")
			.append(", ").append(C_ATTEMPTS).append(" TEXT")
			.append(", ").append(C_FILEPATH).append(" TEXT")
			.append(")");
		return sbOut.toString();
	}
	
	public class DbQueued {
		private String TABLE = "queued";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
		}
		final DbHelper dbHelper;
		
		public DbQueued(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		
		public void close() {
			this.dbHelper.close();
		}
		
		public void insert(String audio, String json, String attempts, String filepath) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, DateTimeUtils.getDateTime());
			values.put(C_AUDIO, audio);
			values.put(C_JSON, json);
			values.put(C_ATTEMPTS, attempts);
			values.put(C_FILEPATH, filepath);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		
		public List<String[]> getAllRows() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, null);
		}
		
		public List<String[]> getRowsWithOffset(int rowOffset, int rowLimit) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, rowOffset, rowLimit);
		}
		
		public String[] getLatestRow() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0);
		}
		
		public void deleteSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE substr("+C_AUDIO+",0,14)='"+audioId.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public String[] getSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, " substr("+C_AUDIO+",0,14) = ?", new String[] { audioId.substring(0,13) }, C_CREATED_AT, 0);
		}
		
		public void clearRowsBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+DateTimeUtils.getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public void incrementSingleRowAttempts(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("UPDATE "+TABLE+" SET "+C_ATTEMPTS+"=cast("+C_ATTEMPTS+" as INT)+1 WHERE substr("+C_AUDIO+",0,14)='"+audioFile.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public int getCount() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getCount(db, TABLE, null, null);
		}

	}
	public final DbQueued dbQueued;
	
	public class DbSkipped {
		private String TABLE = "skipped";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
		}
		final DbHelper dbHelper;
		
		public DbSkipped(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		
		public void close() {
			this.dbHelper.close();
		}
		
		public void insert(String created_at, String audio, String json, String attempts, String filepath) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, created_at);
			values.put(C_AUDIO, audio);
			values.put(C_JSON, json);
			values.put(C_ATTEMPTS, attempts);
			values.put(C_FILEPATH, filepath);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		
		public List<String[]> getAllRows() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, null);
		}
		
		public List<String[]> getRowsWithOffset(int rowOffset, int rowLimit) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, rowOffset, rowLimit);
		}
		
		public String[] getLatestRow() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0);
		}
		
		public void deleteSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE substr("+C_AUDIO+",0,14)='"+audioId.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public String[] getSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, " substr("+C_AUDIO+",0,14) = ?", new String[] { audioId.substring(0,13) }, C_CREATED_AT, 0);
		}
		
		public void clearRowsBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+DateTimeUtils.getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public void incrementSingleRowAttempts(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("UPDATE "+TABLE+" SET "+C_ATTEMPTS+"=cast("+C_ATTEMPTS+" as INT)+1 WHERE substr("+C_AUDIO+",0,14)='"+audioFile.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public int getCount() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getCount(db, TABLE, null, null);
		}

	}
	public final DbSkipped dbSkipped;
	
	public class DbStashed {
		private String TABLE = "stashed";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
		}
		final DbHelper dbHelper;
		
		public DbStashed(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		
		public void close() {
			this.dbHelper.close();
		}
		
		public void insert(String audio, String json, String attempts, String filepath) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, DateTimeUtils.getDateTime());
			values.put(C_AUDIO, audio);
			values.put(C_JSON, json);
			values.put(C_ATTEMPTS, attempts);
			values.put(C_FILEPATH, filepath);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		
		public List<String[]> getAllRows() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, null);
		}
		
		public List<String[]> getRowsWithOffset(int rowOffset, int rowLimit) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, rowOffset, rowLimit);
		}
		
		public String[] getLatestRow() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0);
		}
		
		public void deleteSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE substr("+C_AUDIO+",0,14)='"+audioId.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public String[] getSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, " substr("+C_AUDIO+",0,14) = ?", new String[] { audioId.substring(0,13) }, C_CREATED_AT, 0);
		}
		
		public void clearRowsBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+DateTimeUtils.getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public void incrementSingleRowAttempts(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("UPDATE "+TABLE+" SET "+C_ATTEMPTS+"=cast("+C_ATTEMPTS+" as INT)+1 WHERE substr("+C_AUDIO+",0,14)='"+audioFile.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public int getCount() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getCount(db, TABLE, null, null);
		}

	}
	public final DbStashed dbStashed;
	
	public class DbSent {
		private String TABLE = "sent";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { 
					RfcxLog.logExc(logTag, e);
				}
			}
		}
		final DbHelper dbHelper;
		
		public DbSent(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		
		public void close() {
			this.dbHelper.close();
		}
		
		public void insert(String audio, String json, String attempts, String filepath) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, DateTimeUtils.getDateTime());
			values.put(C_AUDIO, audio);
			values.put(C_JSON, json);
			values.put(C_ATTEMPTS, attempts);
			values.put(C_FILEPATH, filepath);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		
		public List<String[]> getAllRows() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, null);
		}
		
		public List<String[]> getRowsWithOffset(int rowOffset, int rowLimit) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getRows(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, rowOffset, rowLimit);
		}
		
		public String[] getLatestRow() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0);
		}
		
		public void deleteSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE substr("+C_AUDIO+",0,14)='"+audioId.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public String[] getSingleRowByAudioAttachmentId(String audioId) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getSingleRow(db, TABLE, ALL_COLUMNS, " substr("+C_AUDIO+",0,14) = ?", new String[] { audioId.substring(0,13) }, C_CREATED_AT, 0);
		}
		
		public void clearRowsBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+DateTimeUtils.getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public void incrementSingleRowAttempts(String audioFile) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("UPDATE "+TABLE+" SET "+C_ATTEMPTS+"=cast("+C_ATTEMPTS+" as INT)+1 WHERE substr("+C_AUDIO+",0,14)='"+audioFile.substring(0,13)+"'");
			} finally { db.close(); }
		}
		
		public int getCount() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			return DbUtils.getCount(db, TABLE, null, null);
		}

	}
	public final DbSent dbSent;
	
}
