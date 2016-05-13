package org.rfcx.guardian.utility.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.rfcx.guardian.utility.DateTimeUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RfcxServiceHandler {

	public RfcxServiceHandler(Context context, String roleName) {
		this.context = context;
		this.logTag = "Rfcx-"+roleName+"-"+RfcxServiceHandler.class.getSimpleName();
	}	

	Context context;
	private String logTag = "Rfcx-Utils-"+RfcxServiceHandler.class.getSimpleName();

	private Map<String, Class<?>> svcClasses = new HashMap<String, Class<?>>();
	private Map<String, boolean[]> svcRunStates = new HashMap<String, boolean[]>();
	private Map<String, boolean[]> svcAbsoluteRunStates = new HashMap<String, boolean[]>();

	public void triggerService(String[] svcToTrigger, boolean forceReTrigger) {

		if (!this.svcClasses.containsKey(svcToTrigger[0].toLowerCase(Locale.US))) {
			Log.e(logTag, "There is no service named '"+svcToTrigger[0]+"'.");
		} else if (!this.isRunning(svcToTrigger[0]) || forceReTrigger) {
			try {
				// this means it's likely an intent service
				if (svcToTrigger.length > 1) {
					
					long startTimeMillis = ((svcToTrigger[1] == null) || (svcToTrigger[1].equals("0"))) ? System.currentTimeMillis() : (long) Long.parseLong(svcToTrigger[1]);
					long repeatIntervalMillis = ((svcToTrigger[2] == null) || (svcToTrigger[2].equals("0"))) ? 0 : (long) Long.parseLong(svcToTrigger[2]);
					
					AlarmManager alarmMgr = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
					PendingIntent svcIntent = PendingIntent.getService(this.context, -1, new Intent(this.context, svcClasses.get(svcToTrigger[0].toLowerCase(Locale.US))), PendingIntent.FLAG_UPDATE_CURRENT);
					if (repeatIntervalMillis == 0) { 
						alarmMgr.set(AlarmManager.RTC, startTimeMillis, svcIntent);
						Log.i(logTag,"Scheduled IntentService '"+svcToTrigger[0]+"' (begins at "+DateTimeUtils.getDateTime(new Date(startTimeMillis))+")");
					} else { 
						alarmMgr.setInexactRepeating(AlarmManager.RTC, startTimeMillis, repeatIntervalMillis, svcIntent); 
						Log.i(logTag,"Scheduled IntentService '"+svcToTrigger[0]+"' (begins at "+DateTimeUtils.getDateTime(new Date(startTimeMillis))+", repeats approx. every "+DateTimeUtils.milliSecondsAsMinutes(repeatIntervalMillis)+")");
					}

				// this means it's likely a service
				} else if (svcToTrigger.length == 1) {
					
					this.context.stopService(new Intent(this.context, svcClasses.get(svcToTrigger[0].toLowerCase(Locale.US))));
					this.context.startService(new Intent(this.context, svcClasses.get(svcToTrigger[0].toLowerCase(Locale.US))));
					Log.i(logTag,"Triggered Service '"+svcToTrigger[0]+"'");
					
				}
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			}
		} else { 
//			Log.w(logTag, "Service '"+svcToTrigger[0]+"' is already running...");
		}
	}
	
	public void triggerService(String svcToTrigger, boolean forceReTrigger) {
		triggerService(new String[] { svcToTrigger }, forceReTrigger);
	}
	
	public void triggerIntentService(String svcToTrigger, long startTimeMillis, long repeatIntervalMillis) {
		triggerService(new String[] { svcToTrigger, ""+startTimeMillis, ""+repeatIntervalMillis }, false);
	}
	
	public void stopService(String svcToStop) {
		
		if (!this.svcClasses.containsKey(svcToStop.toLowerCase(Locale.US))) {
			Log.e(logTag, "There is no service named '"+svcToStop+"'.");
		} else { 
			try {
				this.context.stopService(new Intent(this.context, svcClasses.get(svcToStop.toLowerCase(Locale.US))));
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			}
		}
	}	
	
	public void triggerServiceSequence(String sequenceName, String[] serviceSequenceSerialized, boolean forceReTrigger) {
		
		if (!hasRun(sequenceName.toLowerCase(Locale.US))) {
			
			Log.i(logTag, "Launching ServiceSequence '"+sequenceName+"'.");
			
			for (String serviceItemSerialized : serviceSequenceSerialized) {
				String[] serviceItem = new String[] { serviceItemSerialized };
				if (serviceItemSerialized.contains("|")) { serviceItem = serviceItemSerialized.split("\\|");  }
				triggerService(serviceItem, forceReTrigger);
			}		 
			
		} else {
			Log.w(logTag, "ServiceSequence '"+sequenceName+"' has already run.");
		}
	}
	
	// Getters and Setters
	
	public boolean isRunning(String svcName) {
		if (this.svcRunStates.containsKey(svcName.toLowerCase(Locale.US))) {
			try {
				return this.svcRunStates.get(svcName.toLowerCase(Locale.US))[0];
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			}
		}
		return false;
	}

	public boolean hasRun(String svcName) {
		if (this.svcAbsoluteRunStates.containsKey(svcName.toLowerCase(Locale.US))) {
			try {
				return this.svcAbsoluteRunStates.get(svcName.toLowerCase(Locale.US))[0];
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			}
		}
		return false;
	}
	
	public void setRunState(String svcName, boolean isRunning) {
		this.svcRunStates.put(svcName.toLowerCase(Locale.US), new boolean[] { isRunning } );
		if (isRunning) setAbsoluteRunState(svcName, true);
	}
	
	public void setAbsoluteRunState(String svcName, boolean hasRun) {
		this.svcAbsoluteRunStates.put(svcName.toLowerCase(Locale.US), new boolean[] { hasRun } );
	}
	
	public void addService(String svcName, Class<?> svcClass) {
		this.svcClasses.put(svcName.toLowerCase(Locale.US), svcClass);
		setRunState(svcName, false);
		setAbsoluteRunState(svcName, false);
	}
	
}