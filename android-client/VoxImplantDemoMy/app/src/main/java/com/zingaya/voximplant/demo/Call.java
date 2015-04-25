package com.zingaya.voximplant.demo;

public class Call {

	public Call(String id, boolean incoming, boolean video) {
		this.id = id;
		this.incoming = incoming;
		this.video = video;
	}
	
	public String id;
	public boolean incoming;
	public boolean video;
}
