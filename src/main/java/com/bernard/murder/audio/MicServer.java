package com.bernard.murder.audio;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.bernard.murder.BytesUtils;

public class MicServer {
	
	TargetDataLine micLine;
	
	int packetLength = 9728;
	
	int micId;
	
	String micName;
	
	UUID askedUUID;
	
	Serveur serveur;
	
	SocketAddress masterAddress;
	
	Thread streamingThread;
	
	volatile boolean shouldStream = false;
	
	Runnable serverAnswered;
	
	
	public MicServer(SocketAddress adresse,String micName,TargetDataLine tdl) {
		this.micName = micName;
		this.masterAddress = adresse;
		this.micLine = tdl;
		try {
			initServer();
			initializeAudioId();
			
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void initializeMicDevice() throws LineUnavailableException {
		micLine.open(AudioServer.formatAudio);
		packetLength = micLine.getBufferSize()/5;
	}
	
	public void initServer() throws SocketException, UnknownHostException {
		serveur = new Serveur(this::receiveCommand, AudioServer.communicationPort);
	}
	
	public void initializeAudioId() {
		ByteBuffer buffer = ByteBuffer.allocate(AudioServer.packetMaxSize);
		
		
		askedUUID = UUID.randomUUID();
		
		buffer.put(AudioServer.AUDIO_STREAM);
		buffer.putLong(askedUUID.getMostSignificantBits());
		buffer.putLong(askedUUID.getLeastSignificantBits());
		buffer.put(AudioServer.MIC_DEVICE);
		
		BytesUtils.writeString(buffer, micName);
		
		try {
			serveur.sendData(buffer,masterAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void receiveCommand(ByteBuffer data) {
		byte commande = data.get();
		switch (commande) {
		
		case AudioServer.START_STREAMING:
			int micId = data.getInt();
			if(micId != this.micId)return;
			shouldStream = true;
			launchDataStream();
			break;
		case AudioServer.STOP_STREAMING:
			int micId2 = data.getInt();
			if(micId2 != this.micId)return;
			shouldStream = false;
			break;
			
		case AudioServer.OK_ID:
			UUID uuid = new UUID(data.getLong(), data.getLong());
			byte deviceType = data.get();
			int deviceId = data.getInt();
			
			if(!askedUUID.equals(uuid) || deviceType!=AudioServer.MIC_DEVICE)
				return;
			
			micId = deviceId;
			
			serverAnswered.run();
			
			if(micLine==null)
				try {
					initializeMicDevice();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			break;

		default:
			break;
		}
	}
	
	public void launchDataStream() {
		if(streamingThread!=null)return;
		streamingThread = new Thread(()->{
			byte[] packetData = new byte[1+4+packetLength];
			ByteBuffer audioPacket = ByteBuffer.wrap(packetData);
			audioPacket.put(AudioServer.AUDIO_STREAM);
			audioPacket.putInt(micId);
			while(shouldStream) {
				micLine.read(packetData, 5, packetLength);
				try {
					serveur.sendData(audioPacket,masterAddress);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		streamingThread.start();
	}
	
	public void dispose() {
		micLine.close();
		serveur.dispose();
	}
	
	public void setServerAnswered(Runnable serverAnswered) {
		this.serverAnswered = serverAnswered;
	}
	
}
