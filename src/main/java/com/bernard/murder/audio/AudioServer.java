package com.bernard.murder.audio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.sound.sampled.AudioFormat;

import com.bernard.murder.BytesUtils;

public class AudioServer {
	
	// Format des paquets [commande 1, UUID identifier, byte deviceType, String name]
	public static final byte DECLARE_NUMBER = 0x02;
	// Format des paquets [commande 1, UUID identifier, byte deviceType, int id]
	public static final byte OK_ID = 0x03;
	// Format des paquets [commande 1]
	public static final byte ASK_AUDIO_LIST= 0x04;
	// Format des paquets [commande 1, int Count, {int id, String name}]
	public static final byte GIVE_AUDIO_LIST = 0x05;
	// Format des paquets: [commande 1, int listenId, int myId]
	public static final byte ASK_STREAMING = 0x06;
	// Format des paquets: [commande 1, int id]
	public static final byte START_STREAMING = 0x07;
	// Format des paquets: [commande 1, int listenId, int myId]
	public static final byte ASK_STOP_STREAMING = 0x09;
	// Format des paquets: [commande 1, int id]
	public static final byte STOP_STREAMING = 0x08;
	// Format des paquets [commande 1, int id, ~ data]
	public static final byte AUDIO_STREAM = 0x01;
	
	
	public static final byte SPEAKER_DEVICE = 0x01;
	public static final byte MIC_DEVICE = 0x02;
	
	
	public static AudioFormat formatAudio = new AudioFormat(8000f, 16, 1, true, true);
	public static int packetMaxSize = 97282;
	public static int communicationPort = 35295;
	
	public Serveur serveur;
	
	int micId = 0;
	int speakerId = 0;
	
	Map<Integer,String> mics;
	Map<Integer,String> speakers;
	Map<Integer,SocketAddress> micsAddr;
	Map<Integer,SocketAddress> speakersAddr;
	Map<Integer,List<Integer>> listening; // micId, List<speakerId>
	
	public AudioServer() {
		mics = new HashMap<Integer, String>();
		micsAddr = new HashMap<Integer, SocketAddress>();
		speakers = new HashMap<Integer, String>();
		speakersAddr = new HashMap<Integer, SocketAddress>();
		listening = new HashMap<Integer, List<Integer>>();
		
		
		try {
			initServer();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void initServer() throws SocketException, UnknownHostException {
		serveur = new Serveur(this::receiveCommand, AudioServer.communicationPort);
	}
	
	public void receiveCommand(ByteBuffer data,SocketAddress senderAddress) {
		byte commande = data.get();
		System.out.println("Commande reçue : "+commande);
		switch (commande) {
		
		case AudioServer.DECLARE_NUMBER:
			
			UUID uuid = new UUID(data.getLong(), data.getLong());
			byte deviceType = data.get();
			String deviceName = BytesUtils.readString(data);
			int newId;
			switch (deviceType) {
				case AudioServer.MIC_DEVICE:
					newId = micId++;
					mics.put(newId, deviceName);
					micsAddr.put(newId, senderAddress);
					listening.put(newId, new ArrayList<>());
					break;
				case AudioServer.SPEAKER_DEVICE:
					newId = speakerId++;
					speakers.put(newId, deviceName);
					speakersAddr.put(newId, senderAddress);
					break;
				default:return;
			}
			ByteBuffer out = ByteBuffer.allocate(AudioServer.packetMaxSize);
			out.put(AudioServer.OK_ID);
			out.putLong(uuid.getMostSignificantBits());
			out.putLong(uuid.getLeastSignificantBits());
			out.put(deviceType);
			out.putInt(newId);
			try {
				serveur.sendData(out, senderAddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			break;
		case AudioServer.ASK_STREAMING:
			int listened = data.getInt();
			int listener = data.getInt();
			listening.get(listened).add(listener);
			ByteBuffer out3 = ByteBuffer.allocate(AudioServer.packetMaxSize);
			out3.put(AudioServer.START_STREAMING);
			out3.putInt(listened);
			try {
				serveur.sendData(out3, micsAddr.get(listened));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			break;
		
		case AudioServer.STOP_STREAMING:
			int listened2 = data.getInt();
			int listener2 = data.getInt();
			listening.get(listener2).remove(listened2);
			ByteBuffer out4 = ByteBuffer.allocate(AudioServer.packetMaxSize);
			out4.put(AudioServer.STOP_STREAMING);
			out4.putInt(listened2);
			try {
				serveur.sendData(out4, micsAddr.get(listened2));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			break;

		case AudioServer.AUDIO_STREAM:
			int micId = data.getInt();
			byte[] audioData = new byte[data.remaining()];
			data.get(audioData);
			
			
			for(int spck : listening.get(micId)) {
				data.clear();
				SocketAddress dest = speakersAddr.get(spck);
				try {
					serveur.sendData(data, dest);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			break;
	

		case AudioServer.ASK_AUDIO_LIST:
			ByteBuffer out2 = ByteBuffer.allocate(AudioServer.packetMaxSize);
			out2.put(AudioServer.GIVE_AUDIO_LIST);
			out2.putInt(mics.size());
			for(Entry<Integer, String> e : mics.entrySet()) {
				out2.putInt(e.getKey());
				BytesUtils.writeString(out2, e.getValue());
			}
			try {
				serveur.sendData(out2, senderAddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

}
