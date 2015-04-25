/*
 * libjingle
 * Copyright 2015 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.zingaya.voximplant.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.hardware.Camera;

import com.zingaya.voximplant.VoxImplantClient;


/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends Activity
{

	public static final String EXTRA_CALL_ID = "org.zingaya.voximplant.demo.CALL_ID";

	// Controls
	private GLSurfaceView videoView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set window styles for fullscreen-window size. Needs to be done before
	// adding content.
	/*requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
		setContentView(R.layout.call_activity);

		// Create UI controls.
		this.videoView = (GLSurfaceView) findViewById(R.id.glview_call);
		VoxImplantClient.instance().setRemoteView(videoView);
		VoxImplantClient.instance().setLocalPreview(videoView);
		VoxImplantClient.instance().setUseLoudspeaker(this.loudSpeaker);
		VoxImplantClient.instance().sendVideo(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.videoView.onPause();
		VoxImplantClient.instance().sendVideo(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.videoView.onResume();
		VoxImplantClient.instance().sendVideo(true);
	}

	@Override
	protected void onDestroy() {
		VoxImplantClient.instance().setRemoteView(null);
		VoxImplantClient.instance().setLocalPreview(null);
		super.onDestroy();
	}

	public void hangupClick(View view) {
		String callID = getIntent().getStringExtra(EXTRA_CALL_ID);
		VoxImplantClient.instance().disconnectCall(callID);
		setResult(RESULT_OK);
		finish();
	}
	
	public void sendVideoClick(View view) {
		VoxImplantClient.instance().sendVideo(true);
	}
	
	public void stopVideoClick(View view) {
		VoxImplantClient.instance().sendVideo(false);
	}

	private int currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;

	public void switchCameraClick(View view) {
		if (this.currentCamera == Camera.CameraInfo.CAMERA_FACING_FRONT)
			this.currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
		else
			this.currentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;

		VoxImplantClient.instance().setCamera(this.currentCamera);
	}

	private boolean loudSpeaker = true;

	public void switchSpeakersClick(View view) {
		this.loudSpeaker = !this.loudSpeaker;
		VoxImplantClient.instance().setUseLoudspeaker(this.loudSpeaker);
	}
}
