package com.bernard.murder.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bernard.murder.audio.AudioServer;
import com.bernard.murder.audio.SpeakerServer;

public class EnceinteServeurFrame extends JFrame{

	private static final long serialVersionUID = -2023529082781210475L;
	
	SpeakerServer serveur;
	String deviceName;
	
	public EnceinteServeurFrame(String deviceName) {
		this.setSize(300, 500);
		this.setMinimumSize(new Dimension(100, 200));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Serveur audio");
		this.deviceName = deviceName;
		
		this.setContentPane(genContentPan());
		this.setVisible(true);
	}
	
	public JPanel genContentPan() {
		JPanel panel = new JPanel(new BorderLayout());
		
		
		
		InformedSourceDataline[] marray = getEnceinteList();
		JList<InformedSourceDataline> enceinteListe = new JList<InformedSourceDataline>(marray);
		
		
		JPanel masterPanel = new JPanel(new BorderLayout());
		JTextField masterIP = new JTextField("192.168.1.1");
		JButton serverControl = new JButton("Lancer");
		masterPanel.add(serverControl,BorderLayout.EAST);
		masterPanel.add(masterIP,BorderLayout.CENTER);
		
		JList<NamedMicrophone> mics = new JList<>();
		
		serverControl.addActionListener(e->{
			if(enceinteListe.getSelectedValue()==null)return;
			serverControl.setEnabled(false);
			if(serveur!=null) {
				serveur.dispose();
				serveur = null;
				serverControl.setText("Lancer");
			}else {
				serveur = new SpeakerServer(new InetSocketAddress(masterIP.getText(), AudioServer.communicationPort), deviceName,enceinteListe.getSelectedValue().tdl);
				serveur.setServerAnswered(()->{
					serverControl.setText("Arrêter");
					List<NamedMicrophone> list = serveur.getAudioList().entrySet().stream().map(et -> new NamedMicrophone(et.getKey(), et.getValue())).collect(Collectors.toList());
					NamedMicrophone[] micarray = new NamedMicrophone[list.size()];
					list.toArray(micarray);
					mics.setListData(micarray);
				});
				serverControl.setText("Lancement");
			}
			serverControl.setEnabled(true);
		});
		
		panel.add(masterPanel,BorderLayout.NORTH);
		panel.add(mics,BorderLayout.SOUTH);
		panel.add(enceinteListe,BorderLayout.CENTER);
		return panel;
	}
	
	public static final InformedSourceDataline[] getEnceinteList() {
		List<InformedSourceDataline> linfo = new ArrayList<>();
		Line.Info srcInfo = new Line.Info(SourceDataLine.class);
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInfos) {
			Mixer mixer = AudioSystem.getMixer(info);
			try {
				SourceDataLine tdl = (SourceDataLine)mixer.getLine(srcInfo);
				linfo.add(new InformedSourceDataline(tdl, info));
			} catch (LineUnavailableException|IllegalArgumentException e) {}
		}
		InformedSourceDataline[] marray = new InformedSourceDataline[linfo.size()];
		linfo.toArray(marray);
		return marray;
	}
	
	
	public static class InformedSourceDataline {
		
		SourceDataLine tdl;
		Mixer.Info mixerInfo;
		
		public InformedSourceDataline(SourceDataLine tdl, Info mixerInfo) {
			this.tdl = tdl;
			this.mixerInfo = mixerInfo;
		}
		
		@Override
		public String toString() {
			return mixerInfo.getDescription();
		}
		
	}
	
	public static class NamedMicrophone {
		
		int micId;
		String micName;
		
		public NamedMicrophone(int micId, String micName) {
			this.micId = micId;
			this.micName = micName;
		}
		
		@Override
		public String toString() {
			return micName;
		}
		
	}
}
