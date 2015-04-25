package com.zingaya.voximplant.demo;

import java.util.HashMap;
import java.util.Map;

import com.zingaya.voximplant.VoxImplantCallback;
import com.zingaya.voximplant.VoxImplantClient;
import com.zingaya.voximplant.VoxImplantClient.LoginFailureReason;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.content.Intent;
import android.hardware.Camera;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.zingaya.voximplant.demo.CallActivity;

public class DemoActivity extends Activity implements VoxImplantCallback{

	private VoxImplantClient client;
	private Call activeCall = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		client = VoxImplantClient.instance();
		loginInput = (TextView)findViewById(R.id.loginInput);
		passwordInput = (TextView)findViewById(R.id.passwordInput);
		remoteNumberInput = (TextView)findViewById(R.id.callToInput);
		logText = (TextView)findViewById(R.id.logText);
		client.setAndroidContext(getApplicationContext());
		client.setCallback(this);
		client.setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
		client.setCameraResolution(320, 240);
		//client.sendVideo(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_demo, menu);
		return true;
	}

	public void callClick(View view) {
		CheckBox p2p = (CheckBox)findViewById(R.id.p2pcheckBox);
		CheckBox videoCall = (CheckBox)findViewById(R.id.videoCallCheckBox);
		String callId = client.createCall(
				remoteNumberInput.getText().toString(), videoCall.isChecked(), null);
		Map<String, String> headers = new HashMap<String, String>();
		if (p2p.isChecked())
			headers.put("X-DirectCall", "true");
		client.startCall(callId, headers);
		this.activeCall = new Call(callId, false, videoCall.isChecked());
		//call.attachMedia();
	}

	public void muteClick(View view) {
		CheckBox mute = (CheckBox)findViewById(R.id.muteCheckBox);
		client.setMute(mute.isChecked());
	}
	
	public void connectClick(View view) {
		client.connect();
	}
	
	public void hangupClick(View view) {
		if (this.activeCall != null) {
			client.disconnectCall(this.activeCall.id);
		}
	}
	
	public void loginClick(View view) {
		client.login(loginInput.getText().toString(), passwordInput.getText().toString());
	}
	
	private TextView loginInput;
	private TextView passwordInput;
	private TextView remoteNumberInput;
	private TextView logText;
	private AlertDialog incomingCallAlertDlg = null;

	private void logMessage(String s) {
		logText.append(s.endsWith("\n") ? s : s+"\n");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	@Override
	public void onCallConnected(String callId, Map<String, String> headers) {
		if (DemoActivity.this.activeCall.video) {
			Intent intent = new Intent(DemoActivity.this, CallActivity.class);
			intent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
			startActivityForResult(intent, 1);
		}
	}

	@Override
	public void onCallDisconnected(String callId, Map<String, String> headers) {
		DemoActivity.this.activeCall = null;
		if (DemoActivity.this.incomingCallAlertDlg != null) {
			DemoActivity.this.incomingCallAlertDlg.cancel();
			DemoActivity.this.incomingCallAlertDlg = null;
		}
		finishActivity(1);
	}

	@Override
	public void onCallFailed(String callId, int code, String reason, Map<String, String> headers) {
		DemoActivity.this.activeCall = null;
		if (DemoActivity.this.incomingCallAlertDlg != null) {
			DemoActivity.this.incomingCallAlertDlg.cancel();
			DemoActivity.this.incomingCallAlertDlg = null;
		}
	}

	@Override
	public void onCallRinging(String callId, Map<String, String> headers) {
		logMessage("Call ringing");
	}

	@Override
	public void onCallAudioStarted(String callId) {
		logMessage("Call audio started");
	}

	@Override
	public void onConnectionSuccessful() {
		logMessage("Connected to server");
	}

	@Override
	public void onConnectionClosed() {
		logMessage("Connection to server closed");
	}

	@Override
	public void onConnectionFailedWithError(String reason) {
		logMessage("Connection can't be established. Reason: " + reason);
	}

	@Override
	public void onLoginSuccessful(String displayName) {
		logMessage("Logged in successfully");
	}

	@Override
	public void onLoginFailed(LoginFailureReason reason) {
		logMessage(String.format("Login failed: %s", reason));
	}

	@Override
	public void onMessageReceivedInCall(String callId, String text, Map<String, String> headers) {
		logMessage(String.format("Message received: %s with headers %s", text, headers.toString()));
		/*HashMap<String, String> m = new HashMap<String, String>();
		m.put("X-HeaderB", "X-ValueB");
		arg0.sendMessage("Test Android Message", m);*/
	}

	@Override
	public void onSIPInfoReceivedInCall(String callId,  String type, String content, Map<String, String> headers) {
		logMessage(String.format("SIP Info received: %s/%s, %s with headers %s",
				callId, type, content, headers.toString()));
		/*HashMap<String, String> m = new HashMap<String, String>();
		m.put("X-HeaderA", "X-ValueA");
		arg0.sendInfo("test", "android", "Test Android Info", m);*/
	}

	@Override
	public void onIncomingCall(final String callId, String from, String displayName, final boolean videoCall, Map<String, String> headers) {

		DemoActivity.this.incomingCallAlertDlg = new AlertDialog.Builder(DemoActivity.this)
			.setTitle("Incoming call")
			.setMessage(displayName + " is calling. Answer?")
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					client.answerCall(callId);
					DemoActivity.this.activeCall = new Call(callId, true, videoCall);
					DemoActivity.this.incomingCallAlertDlg = null;
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					client.declineCall(callId);
					DemoActivity.this.incomingCallAlertDlg = null;
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
	}
}
