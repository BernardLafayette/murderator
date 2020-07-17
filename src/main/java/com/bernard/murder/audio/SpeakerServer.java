package com.bernard.murder.audio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.bernard.murder.BytesUtils;


public class SpeakerServer {
	
	SourceDataLine speakerLine;
	
	int packetLength = 9728;
	
	int speakerId;
	
	String speakerName;
	
	UUID askedUUID;
	
	Serveur serveur;
	
	Map<Integer,String> mics;
	volatile boolean isMicListUpToDate = false;
	
	int listeningTo = -1;
	
	SocketAddress masterAddress;
	
	Runnable serverAnswered;
	
	
	public SpeakerServer(SocketAddress serveur,String speakerName,SourceDataLine speaker) {
		this.speakerName = speakerName;
		this.masterAddress = serveur;
		this.speakerLine = speaker;
		try {
			initServer();
			initializeAudioId();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void initServer() throws SocketException, UnknownHostException {
		serveur = new Serveur(this::receiveCommand,AudioServer.communicationPort);
	}
	
	public void initializeSpeakerDevice() throws LineUnavailableException {
		speakerLine.open(AudioServer.formatAudio);
		packetLength = speakerLine.getBufferSize()/5;
	}
	
	public void initializeAudioId() {
		ByteBuffer buffer = ByteBuffer.allocate(AudioServer.packetMaxSize);
		
		
		askedUUID = UUID.randomUUID();
		
		buffer.put(AudioServer.DECLARE_NUMBER);
		buffer.putLong(askedUUID.getMostSignificantBits());
		buffer.putLong(askedUUID.getLeastSignificantBits());
		buffer.put(AudioServer.SPEAKER_DEVICE);
		
		BytesUtils.writeString(buffer, speakerName);
		
		
		try {
			serveur.sendData(buffer,masterAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void askForStream(int micId) {
		ByteBuffer buffer = ByteBuffer.allocate(AudioServer.packetMaxSize);
		
		
		buffer.put(AudioServer.START_STREAMING);
		buffer.putInt(micId);
		buffer.putInt(speakerId);
		
		try {
			serveur.sendData(buffer,masterAddress);
			listeningTo = micId;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopStreaming() {
		ByteBuffer buffer = ByteBuffer.allocate(AudioServer.packetMaxSize);
		
		
		buffer.put(AudioServer.STOP_STREAMING);
		buffer.putInt(listeningTo);
		buffer.putInt(speakerId);
		
		try {
			serveur.sendData(buffer,masterAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void askAudioList() {
		ByteBuffer buffer = ByteBuffer.allocate(AudioServer.packetMaxSize);
		buffer.put(AudioServer.ASK_AUDIO_LIST);
		try {
			serveur.sendData(buffer,masterAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Map<Integer,String> getAudioList() {
		return getAudioList(false);
	}
	
	public Map<Integer,String> getAudioList(boolean invalidate) {
		isMicListUpToDate = !invalidate && isMicListUpToDate;
		if(!isMicListUpToDate)askAudioList();
		while(!isMicListUpToDate) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Voici les "+mics);
		return mics;
	}
	
	public void receiveCommand(ByteBuffer data) {
		byte commande = data.get();
		System.out.println("Commande recue : "+commande);
		switch (commande) {
		
		case AudioServer.AUDIO_STREAM:
			int micId = data.getInt();
			if(micId != listeningTo)return;
			data.compact();
			byte[] audioData=new byte[data.remaining()];
			data.get(audioData);//XXX Check wether audio data starts at position and not at 0
			speakerLine.write(audioData, 0, packetLength);
			break;
			
		case AudioServer.OK_ID:
			UUID uuid = new UUID(data.getLong(), data.getLong());
			byte deviceType = data.get();
			int deviceId = data.getInt();
			
			if(!askedUUID.equals(uuid) || deviceType!=AudioServer.SPEAKER_DEVICE)
				return;
			
			speakerId = deviceId;
			serverAnswered.run();
			if(speakerLine==null)
				try {
					initializeSpeakerDevice();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			break;
		case AudioServer.GIVE_AUDIO_LIST:
			int micCount = data.getInt();
			mics = new HashMap<Integer, String>(micCount);
			for(int i = 0; i<micCount;i++) {
				int thisMicId = data.getInt();
				mics.put(thisMicId,BytesUtils.readString(data));
			}
			isMicListUpToDate=true;
			break;

		default:
			break;
		}
	}
	public void dispose() {
		speakerLine.close();
		serveur.dispose();
	}
	
	public void setServerAnswered(Runnable serverAnswered) {
		this.serverAnswered = serverAnswered;
	}
	
	
	
	
	
}
