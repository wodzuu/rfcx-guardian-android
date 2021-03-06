package org.rfcx.guardian.system.api;

import java.io.File;
import java.util.Date;

import org.rfcx.guardian.system.RfcxGuardian;
import org.rfcx.guardian.utility.device.DeviceDiskUsage;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SystemContentProvider extends ContentProvider {
	
	private static final String TAG = "Rfcx-"+RfcxGuardian.APP_ROLE+"-"+SystemContentProvider.class.getSimpleName();
	
	private static final String AUTHORITY = RfcxRole.ContentProvider.system.AUTHORITY;
	
	private static final String ENDPOINT_META = RfcxRole.ContentProvider.system.ENDPOINT_META;
	private static final String ENDPOINT_SCREENSHOT = RfcxRole.ContentProvider.system.ENDPOINT_SCREENSHOT;
	private static final String[] PROJECTION_META = RfcxRole.ContentProvider.system.PROJECTION_META;
	private static final String[] PROJECTION_SCREENSHOT = RfcxRole.ContentProvider.system.PROJECTION_SCREENSHOT;
	
	private static final int ENDPOINT_META_LIST = 1;
	private static final int ENDPOINT_META_ID = 2;
	private static final int ENDPOINT_SCREENSHOT_LIST = 3;
	private static final int ENDPOINT_SCREENSHOT_ID = 4;

	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_META, ENDPOINT_META_LIST);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_META+"/#", ENDPOINT_META_ID);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_SCREENSHOT, ENDPOINT_SCREENSHOT_LIST);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_SCREENSHOT+"/#", ENDPOINT_SCREENSHOT_ID);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		RfcxGuardian app = (RfcxGuardian) getContext().getApplicationContext();
		
		try {
			if (URI_MATCHER.match(uri) == ENDPOINT_META_LIST) {
				
				MatrixCursor cursor = new MatrixCursor(PROJECTION_META);
				
				cursor.addRow(new Object[] {
					app.deviceStateDb.dbBattery.getConcatRows(),		// battery
					app.deviceStateDb.dbCPU.getConcatRows(),			// cpu
					app.deviceStateDb.dbPower.getConcatRows(),			// power
					app.deviceStateDb.dbNetwork.getConcatRows(),		// network
					app.deviceStateDb.dbOffline.getConcatRows(),		// offline
					app.deviceStateDb.dbLightMeter.getConcatRows(),		// lightmeter
					app.dataTransferDb.dbTransferred.getConcatRows(),	// data_transfer
					DeviceDiskUsage.concatDiskStats(),					// disk_usage
					app.deviceStateDb.dbAccelerometer.getConcatRows(),	// accelerometer
					app.rebootDb.dbReboot.getConcatRows()				// reboots
				});
				
				return cursor;
				
			} else if (URI_MATCHER.match(uri) == ENDPOINT_SCREENSHOT_LIST) {
				
				MatrixCursor cursor = new MatrixCursor(PROJECTION_SCREENSHOT);
				
				for (String[] screenShotRow : app.screenShotDb.dbCaptured.getAllRows()) {
					cursor.addRow(new Object[] { 
							screenShotRow[0], screenShotRow[1], screenShotRow[2], screenShotRow[3], screenShotRow[4]});
				}
				return cursor;
			}
		} catch (Exception e) {
			RfcxLog.logExc(TAG, e);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		RfcxGuardian app = (RfcxGuardian) getContext().getApplicationContext();
		
		try {
			if (URI_MATCHER.match(uri) == ENDPOINT_META_ID) {
				
				Date deleteBefore = new Date(Long.parseLong(uri.getLastPathSegment()));
				
				app.deviceStateDb.dbBattery.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbCPU.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbPower.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbNetwork.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbOffline.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbLightMeter.clearRowsBefore(deleteBefore);
				app.deviceStateDb.dbAccelerometer.clearRowsBefore(deleteBefore);
				app.dataTransferDb.dbTransferred.clearRowsBefore(deleteBefore);
				app.rebootDb.dbReboot.clearRowsBefore(deleteBefore);
				
				return 1;
				
			} else if (URI_MATCHER.match(uri) == ENDPOINT_SCREENSHOT_ID) {
				
				long screenShotTimestamp = Long.parseLong(uri.getLastPathSegment());
				String[] screenShotInfo = app.screenShotDb.dbCaptured.getSingleRowByTimestamp(""+screenShotTimestamp);
				app.screenShotDb.dbCaptured.deleteSingleRowByTimestamp(screenShotInfo[1]);
				(new File(screenShotInfo[4])).delete();
		
				return 1;
			}
		} catch (Exception e) {
			RfcxLog.logExc(TAG, e);
		}
		return 0;
	}
	
	@Override
	public boolean onCreate() {
		return true;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		RfcxGuardian app = (RfcxGuardian) getContext().getApplicationContext();
		
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		RfcxGuardian app = (RfcxGuardian) getContext().getApplicationContext();
		
		try {
			if (URI_MATCHER.match(uri) == ENDPOINT_META_LIST) {
				return null;
			} else if (URI_MATCHER.match(uri) == ENDPOINT_SCREENSHOT_LIST) {
				app.rfcxServiceHandler.triggerService(new String[] { "ScreenShot" }, true);
		//		return Uri.parse(RfcxConstants.RfcxContentProvider.system.URI_SCREENSHOT+"/"+screenShotId);
			}
		} catch (Exception e) {
			RfcxLog.logExc(TAG, e);
		}
		return null;
	}
	
}
