package org.rfcx.guardian.api.receiver;

import java.util.Calendar;

import org.rfcx.guardian.api.RfcxGuardian;
import org.rfcx.guardian.utility.RfcxConstants;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class AirplaneModeReceiver extends BroadcastReceiver {

	private static final String TAG = "Rfcx-"+RfcxConstants.ROLE_NAME+"-"+AirplaneModeReceiver.class.getSimpleName();
	
	private RfcxGuardian app = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (app == null) app = (RfcxGuardian) context.getApplicationContext();

		Log.v(TAG,
				"AirplaneMode " + ( app.airplaneMode.isEnabled(context) ? "Enabled" : "Disabled" )
				+ " at "+(Calendar.getInstance()).getTime().toLocaleString());
		
	}

}
