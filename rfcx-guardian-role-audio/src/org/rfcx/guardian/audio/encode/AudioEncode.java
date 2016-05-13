package org.rfcx.guardian.audio.encode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.rfcx.guardian.audio.RfcxGuardian;
import org.rfcx.guardian.audio.service.AudioEncodeIntentService;
import org.rfcx.guardian.audio.service.CheckInTriggerIntentService;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class AudioEncode {

	private static final String TAG = "Rfcx-"+RfcxGuardian.APP_ROLE+"-"+AudioEncode.class.getSimpleName();
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM/dd-a", Locale.US);
	
	public static String appFilesDir(Context context) {
		return context.getFilesDir().toString();
	}
	
	public static String sdCardFilesDir() {
		return Environment.getExternalStorageDirectory().toString()+"/rfcx";
	}
	
	public static String finalFilesDir() {
		String filesDir = Environment.getDownloadCacheDirectory().toString()+"/rfcx";
		if ((new File(sdCardFilesDir())).isDirectory()) { filesDir = sdCardFilesDir(); }
		return filesDir;
	}

	public static String postZipDir() {
		return finalFilesDir()+"/audio";
	}

	public static String encodeDir(Context context) {
		return appFilesDir(context)+"/encode"; 
	}
	
	public static String getAudioFileLocation_PreEncode(Context context, long timestamp, String fileExtension) {
		return encodeDir(context)+"/"+timestamp+"."+fileExtension;
	}
	
	public static String getAudioFileLocation_PostEncode(Context context, long timestamp, String fileExtension) {
		return encodeDir(context)+"/_"+timestamp+"."+fileExtension;
	}

	public static String getAudioFileLocation_Complete_PostZip(long timestamp, String fileExtension) {
		return postZipDir()+"/"+dateFormat.format(new Date(timestamp))+"/"+timestamp+"."+fileExtension+".gz";
	}
	
	public static void cleanupEncodeDirectory(Context context) {
		for (File file : (new File(encodeDir(context))).listFiles()) {
			try { 
				file.delete(); 
			} catch (Exception e) {
				RfcxLog.logExc(TAG, e);
			}
		}
	}
	
	public static void purgeSingleAudioAssetFromDisk(String audioTimestamp, String audioFileExtension) {
		try {
			(new File(getAudioFileLocation_Complete_PostZip((long) Long.parseLong(audioTimestamp),audioFileExtension))).delete();
			Log.d(TAG, "Purging audio asset: "+audioTimestamp+"."+audioFileExtension);
		} catch (Exception e) {
			RfcxLog.logExc(TAG, e);
		}
	}

	
}
