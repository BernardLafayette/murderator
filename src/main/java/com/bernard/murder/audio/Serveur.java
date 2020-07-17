package com.bernard.murder.audio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.bernard.murder.ParseUtils;

public class Serveur {
	
	int packetMaxLength = 9600;
	// (commande 1, uuid 16, short sliceCount 4, short sliceId 4, int totalSize 8, int offset 8, data ~)
	byte dataMergeCommand = (byte) 0xFF;
	DatagramSocket socket;
	Thread packetReceiver;
	Map<UUID, byte[]> byteArrays = new HashMap<>();
	Map<UUID, boolean[]> receivedSlicesArray = new HashMap<>();
	
	volatile boolean isReceiving = false;
	
	BiConsumer<ByteBuffer,SocketAddress> consumer;
	
	public Serveur(Consumer<ByteBuffer> dataEater, int port) throws UnknownHostException, SocketException {
		this((b,i)->dataEater.accept(b),new InetSocketAddress(port));
	}
	public Serveur(Consumer<ByteBuffer> dataEater,SocketAddress addresse) throws UnknownHostException, SocketException {
		this((b,i)->dataEater.accept(b),addresse);
	}
	public Serveur(BiConsumer<ByteBuffer,SocketAddress> dataEater,int port) throws UnknownHostException, SocketException {
		this(dataEater,new InetSocketAddress(port));
	}
	
	public Serveur(BiConsumer<ByteBuffer,SocketAddress> dataEater,SocketAddress adresse) throws SocketException {
		
		byteArrays = new HashMap<UUID, byte[]>();
		receivedSlicesArray = new HashMap<UUID, boolean[]>();
		this.consumer = dataEater;
		this.socket = new DatagramSocket(adresse);
		
		packetReceiver = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(isReceiving) {
					
					byte[] data = new byte[packetMaxLength];
					DatagramPacket paquet = new DatagramPacket(data, packetMaxLength);
					
					try {
						try {
							socket.receive(paquet);
						}catch(SocketException e) {
							return;//probably socket closed
						}
						if(data[paquet.getOffset()]==0xFF) {
							ByteBuffer bb = ByteBuffer.wrap(data, paquet.getOffset() + 1, 41);
							UUID uuid = new UUID(bb.getLong(), bb.getLong());
							short sliceCount = bb.getShort();
							short sliceId = bb.getShort();
							int totalSize = bb.getInt();
							int offset = bb.getInt();
							
							
							if(!receivedSlicesArray.containsKey(uuid)) {
								byte[] bigData = new byte[totalSize];
								boolean[] recievedArray = new boolean[sliceCount];
								byteArrays.put(uuid, bigData);
								receivedSlicesArray.put(uuid, recievedArray);
							}
							byte[] bigData = byteArrays.get(uuid);
							boolean[] recievedArray = receivedSlicesArray.get(uuid);
							System.arraycopy(data, paquet.getOffset()+41, bigData, offset, paquet.getLength()-41);
							recievedArray[sliceId] = true;
							if(!ParseUtils.and(recievedArray)) {
								ByteBuffer dataBuffer = ByteBuffer.wrap(bigData);
								consumer.accept(dataBuffer,paquet.getSocketAddress());
								byteArrays.remove(uuid);
								receivedSlicesArray.remove(uuid);
							}
						}else {
							ByteBuffer dataBuffer = ByteBuffer.wrap(data,paquet.getOffset(),paquet.getLength());
							consumer.accept(dataBuffer,paquet.getSocketAddress());
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		});
		isReceiving = true;
		packetReceiver.start();
	}
	
	public void sendData(ByteBuffer buffer,SocketAddress address) throws IOException {
		byte[] data = new byte[buffer.position()];
		buffer.clear();
		buffer.get(data);
		sendData(data,address);
	}
	public void sendData(byte[] data, SocketAddress address) throws IOException {
		if(data.length < packetMaxLength) {
			DatagramPacket packet = new DatagramPacket(data, data.length,address);
			socket.send(packet);
		}else {
			short packetCount = (short) (data.length / (packetMaxLength-42));
			short packetLength = (short) (data.length / packetCount);
			short lastPacketLength = (short) (data.length - (packetCount-1)*packetLength);
			
			UUID uuid = UUID.randomUUID();
			byte[][] dataz = new byte[packetCount][];
			for (short i = 0; i < dataz.length-1; i++) {
				byte[] packagePayload = new byte[packetLength+41];
				ByteBuffer buffer = ByteBuffer.wrap(packagePayload, 0, 41);
				buffer.put(dataMergeCommand);
				buffer.putLong(uuid.getMostSignificantBits());
				buffer.putLong(uuid.getLeastSignificantBits());
				buffer.putShort(packetCount);
				buffer.putShort(i);
				buffer.putInt(data.length);
				buffer.putInt(i*packetLength);
				System.arraycopy(data, 0, packagePayload, 41, packetLength);
				dataz[i]=packagePayload;
			}
			byte[] packagePayload = new byte[packetLength+41];
			ByteBuffer buffer = ByteBuffer.wrap(packagePayload, 0, 41);
			buffer.put(dataMergeCommand);
			buffer.putLong(uuid.getMostSignificantBits());
			buffer.putLong(uuid.getLeastSignificantBits());
			buffer.putShort(packetCount);
			buffer.putShort((short) (packetCount-1));
			buffer.putInt(data.length);
			buffer.putInt((packetCount-1)*packetLength);
			System.arraycopy(data, 0, packagePayload, 41, lastPacketLength);
			dataz[packetCount-1]=packagePayload;
			
			
			
		}
	}
	public void dispose() {
		isReceiving = false;
		socket.close();
		packetReceiver.interrupt();
	}
	
}
