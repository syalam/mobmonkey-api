package com.MobMonkey.Models;

public class Type {
	enum RequestType {
		Image, Video
	}

	private final RequestType reqType;

	public Type(RequestType reqType) {
		this.reqType = reqType;
	}

	public int getType() {
		switch (this.reqType) {
		case Image:
			return 1;
		case Video:
			return 2;
		default:
			return 0;
		}
	}
}