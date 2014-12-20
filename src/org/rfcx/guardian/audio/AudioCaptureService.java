package org.rfcx.guardian.audio;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.rfcx.guardian.RfcxGuardian;
import org.rfcx.guardian.database.AudioDb;
import org.rfcx.guardian.utility.ExtAudioRecorderModified;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

public class AudioCaptureService extends Service {

	private static final String TAG = AudioCaptureService.class.getSimpleName();

	private boolean runFlag = false;
	private AudioCapture audioCapture;

	private RfcxGuardian app = null;
	private Context context = null;
	MediaRecorder mediaRecorder = null;
    ExtAudioRecorderModified audioRecorder = null;
	
	private int captureSampleRate;
	
	private int encodingBitRate;
	private String fileExtension;
	
	private long[] captureTimeStamps = {0,0};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.audioCapture = new AudioCapture();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		this.runFlag = true;
		
		app = (RfcxGuardian) getApplication();
		context = app.getApplicationContext();
		
		app.audioCore.initializeAudioDirectories(app);
		app.audioCore.cleanupCaptureDirectory();
		if (app.audioCore.purgeAudioAssetsOnStart) { app.audioCore.purgeAudioAssets(app.audioDb); }
		
		if (app.verboseLogging) Log.d(TAG, "Starting service: "+TAG);
		
		app.isServiceRunning_AudioCapture = true;
		this.audioCapture.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.isServiceRunning_AudioCapture = false;
		this.audioCapture.interrupt();
		this.audioCapture = null;
	}

	private class AudioCapture extends Thread {

		public AudioCapture() {
			super("AudioCaptureService-AudioCapture");
		}

		@Override
		public void run() {
			AudioCaptureService audioCaptureService = AudioCaptureService.this;
			app = (RfcxGuardian) getApplication();
			AudioCore audioCore = app.audioCore;
			captureSampleRate = audioCore.CAPTURE_SAMPLE_RATE_HZ;
			encodingBitRate = audioCore.aacEncodingBitRate;
			fileExtension = (app.audioCore.encodeLossyOnCapture) ? "m4a" : "wav";
			try {
				while (audioCaptureService.runFlag) {
					try {
						captureLoopStart();
				        processCompletedCaptureFile();
				        Thread.sleep(audioCore.CAPTURE_LOOP_PERIOD_SECS*1000);
						captureLoopEnd();					
					} catch (Exception e) {
						e.printStackTrace();
						audioCaptureService.runFlag = false;
						app.isServiceRunning_AudioCapture = false;
					}
				}
				if (app.verboseLogging) Log.d(TAG, "Stopping service: "+TAG);
				captureLoopEnd();
				
			} catch (Exception e) {
				e.printStackTrace();
				audioCaptureService.runFlag = false;
				app.isServiceRunning_AudioCapture = false;
			}
		}
	}
	
	private void captureLoopStart() throws IllegalStateException, IOException {
		long timeStamp = Calendar.getInstance().getTimeInMillis();
		String filePath = app.audioCore.captureDir+"/"+timeStamp+"."+fileExtension;
		if (app.audioCore.encodeLossyOnCapture) {
			mediaRecorder = setAacCaptureRecorder();
			mediaRecorder.setOutputFile(filePath);
	        mediaRecorder.prepare();
	        mediaRecorder.start();
		} else {
			audioRecorder = ExtAudioRecorderModified.getInstance();
			audioRecorder.setOutputFile(filePath);
	        audioRecorder.prepare();
	        audioRecorder.start();
		}
        captureTimeStamps[0] = captureTimeStamps[1];
        captureTimeStamps[1] = timeStamp;
	}
	
	private void captureLoopEnd() {
		if (app.audioCore.encodeLossyOnCapture) {
			mediaRecorder.stop();
			mediaRecorder.release();
		} else {
			audioRecorder.stop();
			audioRecorder.release();
		}
	}
	
	private MediaRecorder setAacCaptureRecorder() {
		MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(captureSampleRate);
        mediaRecorder.setAudioEncodingBitRate(encodingBitRate);
        mediaRecorder.setAudioChannels(1);
        return mediaRecorder;
	}
	
	private void processCompletedCaptureFile() {
		File completedCapture = new File(app.audioCore.captureDir+"/"+captureTimeStamps[0]+"."+fileExtension);
		if (completedCapture.exists()) {
			completedCapture.renameTo(new File(
					((app.audioCore.encodeLossyOnCapture) ? app.audioCore.aacDir : app.audioCore.wavDir)
					+"/"+captureTimeStamps[0]+"."+fileExtension));
	        app.audioDb.dbCaptured.insert(captureTimeStamps[0]+"", fileExtension);
			if (app.verboseLogging) Log.d(TAG, "Capture file created ("+app.audioCore.CAPTURE_LOOP_PERIOD_SECS+"s): "+captureTimeStamps[0]+"."+fileExtension);
	        app.audioCore.queueAudioCaptureFollowUp(context);
		}
	}

}