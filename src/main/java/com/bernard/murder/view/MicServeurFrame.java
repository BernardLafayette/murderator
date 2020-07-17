package com.bernard.murder.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bernard.murder.audio.AudioServer;
import com.bernard.murder.audio.MicServer;

public class MicServeurFrame extends JFrame{

	private static final long serialVersionUID = -2023529082781210475L;
	
	MicServer serveur;
	String deviceName;
	
	public MicServeurFrame(String deviceName) {
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
		
		
		
		InformedTargetDataline[] marray = getEnceinteList();
		JList<InformedTargetDataline> micListe = new JList<InformedTargetDataline>(marray);
		
		
		JPanel masterPanel = new JPanel(new BorderLayout());
		JTextField masterIP = new JTextField("192.168.1.1");
		JButton serverControl = new JButton("Lancer");
		masterPanel.add(serverControl,BorderLayout.EAST);
		masterPanel.add(masterIP,BorderLayout.CENTER);
		
		serverControl.addActionListener(e->{
			if(micListe.getSelectedValue()==null)return;
			serverControl.setEnabled(false);
			if(serveur!=null) {
				serveur.dispose();
				serveur = null;
				serverControl.setText("Lancer");
			}else {
				serveur = new MicServer(new InetSocketAddress(masterIP.getText(), AudioServer.communicationPort), deviceName,micListe.getSelectedValue().tdl);
				serveur.setServerAnswered(()->{
					serverControl.setText("Arrêter");
				});
				serverControl.setText("Lancement");
			}
			serverControl.setEnabled(true);
		});
		
		panel.add(masterPanel,BorderLayout.NORTH);
		panel.add(micListe,BorderLayout.CENTER);
		return panel;
	}
	
	public static final InformedTargetDataline[] getEnceinteList() {
		List<InformedTargetDataline> linfo = new ArrayList<>();
		Line.Info srcInfo = new Line.Info(TargetDataLine.class);
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInfos) {
			Mixer mixer = AudioSystem.getMixer(info);
			try {
				TargetDataLine tdl = (TargetDataLine)mixer.getLine(srcInfo);
				linfo.add(new InformedTargetDataline(tdl, info));
			} catch (LineUnavailableException|IllegalArgumentException e) {}
		}
		InformedTargetDataline[] marray = new InformedTargetDataline[linfo.size()];
		linfo.toArray(marray);
		return marray;
	}
	
	public static class InformedTargetDataline {
		
		TargetDataLine tdl;
		Mixer.Info mixerInfo;
		
		public InformedTargetDataline(TargetDataLine tdl, Info mixerInfo) {
			this.tdl = tdl;
			this.mixerInfo = mixerInfo;
		}
		
		@Override
		public String toString() {
			return mixerInfo.getDescription();
		}
		
	}
	
}
