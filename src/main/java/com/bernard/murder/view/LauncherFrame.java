package com.bernard.murder.view;

import java.awt.GridLayout;
import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.bernard.murder.game.GameCreator;
import com.bernard.murder.game.GameCreator.QuicksavedPartie;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Partie;
import com.bernard.murder.model.Personnage;
import com.bernard.murder.view.minel.Minel;

public class LauncherFrame extends JFrame{
	
	private static final long serialVersionUID = 5831232688024137883L;
	
	public static void main(String[] args) {
		new LauncherFrame();
	}

	public LauncherFrame() {
		try {
			//TODO implement flatlaf look&feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		this.setTitle("Configuration du terminal");
		this.setSize(300, 200);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JPanel contentPan = new JPanel(new GridLayout(5,1));
		
		
		JButton creerMaitre = new JButton("Terminal maître");
		creerMaitre.addActionListener(e -> {
			this.setEnabled(false);
			JFileChooser chooser = new JFileChooser();
			int returnState = chooser.showOpenDialog(this);
			if(returnState==JFileChooser.APPROVE_OPTION) {
				File toread = chooser.getSelectedFile();
				try {
					Partie partie = GameCreator.genFromFile(toread);
					GameManager manager = new GameManager(partie);
					Map<String,List<Minel>> minelsSup = MinelsCreator.genSupMinels(partie,manager);
					Map<Personnage,List<Minel>> minels = MinelsCreator.genPersoMinels(partie,manager);
					
					new MurderatorGameFrame("Murder du "+DateFormat.getDateInstance().format(Calendar.getInstance().getTime()),partie,manager,minelsSup,minels);
					this.setVisible(false);
					dispose();
				
				}catch(Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Impossible de lire le fichier", JOptionPane.ERROR_MESSAGE);
				}
			}
			this.setEnabled(true);
		});
		
		JButton creerEsclave = new JButton("Terminal esclave");
		creerEsclave.setEnabled(false);
		
		JButton creerEnceinte = new JButton("Enceinte");
		creerEnceinte.addActionListener(e -> {
			this.setEnabled(false);
			String name = JOptionPane.showInputDialog(this, "Nom de l'enceinte", "Nom de l'enceinte", JOptionPane.PLAIN_MESSAGE);
			if(name != null) {
				new EnceinteServeurFrame(name);
				this.setVisible(false);
				dispose();
			} else {
				this.setEnabled(true);
				
			}
		});
		
		JButton creerMicro = new JButton("Microphone");
		creerMicro.addActionListener(e -> {
			this.setEnabled(false);
			String name = JOptionPane.showInputDialog(this, "Nom du microphone", "Nom du microphone", JOptionPane.PLAIN_MESSAGE);
			if(name != null) {
				new MicServeurFrame(name);
				this.setVisible(false);
				dispose();
			} else {
				this.setEnabled(true);
			}
		});
		
		
		JButton chargerSauvegarde = new JButton("Charger Sauvegarde");
		chargerSauvegarde.addActionListener(e -> {
			this.setEnabled(false);
			JFileChooser chooser = new JFileChooser();
			int returnState = chooser.showOpenDialog(this);
			if(returnState==JFileChooser.APPROVE_OPTION) {
				File toread = chooser.getSelectedFile();
				try {
					QuicksavedPartie qpartie = GameCreator.readQuickSave(toread);
					GameManager manager = new GameManager(qpartie.getPartie());
					
					new MurderatorGameFrame("Murder du "+DateFormat.getDateInstance().format(Calendar.getInstance().getTime()),qpartie,manager);
					this.setVisible(false);
					dispose();
				
				}catch(Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Impossible de lire le fichier", JOptionPane.ERROR_MESSAGE);
				}
			}
			this.setEnabled(true);
		});
		
		contentPan.add(creerMaitre);
		contentPan.add(creerEsclave);
		contentPan.add(creerMicro);
		contentPan.add(creerEnceinte);
		contentPan.add(chargerSauvegarde);
		
		this.setContentPane(contentPan);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
