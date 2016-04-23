package org.rfcx.guardian.audio.encode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.rfcx.guardian.audio.service.AudioEncodeIntentService;
import org.rfcx.guardian.audio.service.CheckInTriggerIntentService;
import org.rfcx.guardian.utility.RfcxConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class AudioEncode {

	private static final String TAG = "Rfcx-"+RfcxConstants.ROLE_NAME+"-"+AudioEncode.class.getSimpleName();

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM/dd-a", Locale.US);

	public String sdCardFilesDir = Environment.getExternalStorageDirectory().toString()+"/rfcx";
	public String finalFilesDir = Environment.getDownloadCacheDirectory().toString()+"/rfcx";
	public String postZipDir = null;
	public String encodeDir = null;

	public static final boolean ENCODE_ON_CAPTURE = true;

	public final static int ENCODING_BIT_RATE = 16384;
	public final static String ENCODING_CODEC = "aac";
	
	public String getAudioFileLocation_PreEncode(long timestamp, String fileExtension) {
		return this.encodeDir+"/"+timestamp+"."+fileExtension;
	}
	
	public String getAudioFileLocation_PostEncode(long timestamp, String fileExtension) {
		return this.encodeDir+"/_"+timestamp+"."+fileExtension;
	}

	public String getAudioFileLocation_Complete_PostZip(long timestamp, String fileExtension) {
		return this.postZipDir+"/"+dateFormat.format(new Date(timestamp))+"/"+timestamp+"."+fileExtension+".gz";
	}
	
	public void cleanupEncodeDirectory() {
		for (File file : (new File(this.encodeDir)).listFiles()) {
			try { file.delete(); } catch (Exception e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC); }
		}
	}
	
	public void triggerAudioEncodeAfterCapture(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent audioEncodeIntentService = PendingIntent.getService(context, -1, new Intent(context, AudioEncodeIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), audioEncodeIntentService);
	}
	
	public void triggerCheckInAfterEncode(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent checkInTriggerIntentService = PendingIntent.getService(context, -1, new Intent(context, CheckInTriggerIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), checkInTriggerIntentService);
	}
	
	public void purgeSingleAudioAssetFromDisk(String audioTimestamp, String audioFileExtension) {
		try {
			(new File(getAudioFileLocation_Complete_PostZip((long) Long.parseLong(audioTimestamp),audioFileExtension))).delete();
			Log.d(TAG, "Purging single audio asset: "+audioTimestamp+"."+audioFileExtension);
		} catch (Exception e) {
			Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC);
		}
	}

	
}
