package com.bernard.murder;

import java.nio.ByteBuffer;

public class BytesUtils {
	
	public static String readString(ByteBuffer buffer) {
		int stringLength = buffer.getInt();
		byte[] stringData = new byte[stringLength];
		buffer.get(stringData);
		return new String(stringData);
	}
	
	public static void writeString(ByteBuffer buffer, String s) {
		byte[] data = s.getBytes();
		buffer.putInt(data.length);
		buffer.put(data);
	}
	
}
