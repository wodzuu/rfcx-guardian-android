package org.rfcx.guardian.api.api;

import java.util.Calendar;

import org.rfcx.guardian.api.RfcxGuardian;
import org.rfcx.guardian.utility.RfcxConstants;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ApiContentProvider extends ContentProvider {
	
	private static final String TAG = "Rfcx-"+RfcxConstants.ROLE_NAME+"-"+ApiContentProvider.class.getSimpleName();

	private RfcxGuardian app = null;
	private Context context = null;
	
	private static final String AUTHORITY = RfcxConstants.RfcxContentProvider.api.AUTHORITY;
	private static final String ENDPOINT_1 = RfcxConstants.RfcxContentProvider.api.ENDPOINT_1;
	
	private static final int ENDPOINT_1_LIST = 1;
	private static final int ENDPOINT_1_ID = 2;

	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_1, ENDPOINT_1_LIST);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_1+"/#", ENDPOINT_1_ID);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		checkSetApplicationContext();
		
		try {
			MatrixCursor cursor = new MatrixCursor(RfcxConstants.RfcxContentProvider.api.PROJECTION_1);
			
			cursor.addRow(new Object[] { 
					Calendar.getInstance().getTimeInMillis()
				});
			
			return cursor;
			
		} catch (Exception e) {
			Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		checkSetApplicationContext();
		
		return 0;
	}
	
	private void checkSetApplicationContext() {
		if (this.context == null) { this.context = getContext(); }
		if (this.app == null) { this.app = (RfcxGuardian) this.context.getApplicationContext(); }
	}
	
	@Override
	public boolean onCreate() {
		return true;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		checkSetApplicationContext();
		
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		checkSetApplicationContext();
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		checkSetApplicationContext();
		
		if (URI_MATCHER.match(uri) == ENDPOINT_1_LIST) {
			try {
				
				String[] audioInfo = new String[] {
						values.getAsString("created_at"),
						values.getAsString("timestamp"),
						values.getAsString("format"),
						values.getAsString("digest")
				};
				
				if (app.apiWebCheckIn.addCheckInToQueue(audioInfo, values.getAsString("filepath"))) {
					return Uri.parse(
							RfcxConstants.RfcxContentProvider.api.URI_1
								+"/"+values.getAsString("timestamp")
							);
				}
			} catch (Exception e) {
				Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC);
			}
		}
		return null;
	}
	
}
