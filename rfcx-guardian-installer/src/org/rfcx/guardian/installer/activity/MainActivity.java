package org.rfcx.guardian.installer.activity;

import java.io.File;

import org.rfcx.guardian.installer.RfcxGuardian;
import org.rfcx.guardian.installer.R;
import org.rfcx.guardian.utility.ShellCommands;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		RfcxGuardian app = (RfcxGuardian) getApplication();
		switch (item.getItemId()) {
		
		case R.id.menu_prefs:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
			
		case R.id.menu_cputuner_install:
			String thisAppPath = app.getApplicationContext().getFilesDir().getAbsolutePath();
			String cpuTunerPath = thisAppPath.substring(0,thisAppPath.lastIndexOf("/org.rfcx.guardian"))+"/ch.amana.android.cputuner";
			if (!(new File(thisAppPath.substring(0,thisAppPath.lastIndexOf("/org.rfcx.guardian"))+"/ch.amana.android.cputuner")).isDirectory()) {
				Log.d("Rfcx-Installer","CPUTuner will now be downloaded and installed...");
				app.apiCore.targetAppRoleApiEndpoint = "cputuner";
				app.apiCore.setApiCheckVersionEndpoint(app.getDeviceId());
				app.triggerService("ApiCheckVersion",true);
			} else {
				Log.d("Rfcx-Installer","CPUTuner is already installed...");
			}
			break;
			
		case R.id.menu_check_version:
			app.triggerService("ApiCheckVersion",true);
			break;

		case R.id.menu_root_command:
			(new ShellCommands()).executeCommandAsRoot("pm list features",null,getApplicationContext());
			break;
			
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		((RfcxGuardian) getApplication()).appPause();
	}
	
}