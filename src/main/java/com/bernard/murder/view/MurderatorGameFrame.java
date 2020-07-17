package com.bernard.murder.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.bernard.murder.ParseUtils;
import com.bernard.murder.game.GameCreator.QuicksavedPartie;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Partie;
import com.bernard.murder.model.Personnage;
import com.bernard.murder.util.view.MouseReactiveTabbedPane;
import com.bernard.murder.view.minel.Minel;

public class MurderatorGameFrame extends JFrame{
	
	private static final long serialVersionUID = -4512350072325470066L;
		
	
	Map<String,List<Minel>> minelsSup;
	Map<Personnage,List<Minel>> minels;
	
	public MurderatorGameFrame(String frameName, Partie partie, GameManager manager,Map<String,List<Minel>> minelsSup,Map<Personnage,List<Minel>> minels) {
		this.setSize(700, 500);
		this.setMinimumSize(new Dimension(200, 100));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle(frameName);
		this.minelsSup = minelsSup;
		this.minels = minels;
		manager.bindMinelQuicksaver(() -> MinelsCreator.createMinelQuicksave(minels, minelsSup));
		
		this.setContentPane(genGamePane(partie,manager,minelsSup,minels));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public MurderatorGameFrame(String frameName, QuicksavedPartie qpartie, GameManager manager) {
		this(frameName,qpartie.getPartie(),manager,MinelsCreator.genSupMinels(qpartie, manager),MinelsCreator.genPersoMinels(qpartie, manager));
	}
	
	public MurderatorGameFrame(String frameName, Partie partie, GameManager manager) {
		this(frameName,partie,manager,MinelsCreator.genSupMinels(partie, manager),MinelsCreator.genPersoMinels(partie, manager));
	}

	public JPanel genGamePane(Partie partie,GameManager manager,Map<String,List<Minel>> minelsSup,Map<Personnage,List<Minel>> minels) {
		
		JPanel globalPan = new JPanel(new BorderLayout());
		
		//Center Panel
		MouseReactiveTabbedPane centerPan = new MouseReactiveTabbedPane(JTabbedPane.TOP);
		
		int j = 0;
		
		for(String s : minelsSup.keySet()) {
			JPanel centralLocalBpanPan = new JPanel(new GridLayout(2,(minelsSup.get(s).size()+1)/2,-1,-1));
			minelsSup.get(s).stream().map(m -> m.genContentPane()).forEach(mpan -> {centralLocalBpanPan.add(mpan);mpan.setBorder(BorderFactory.createLineBorder(ParseUtils.randDarkBlueColor(),2));});
			JScrollPane centralLocalPan = new JScrollPane(centralLocalBpanPan);
			centerPan.insertTab(s,null,centralLocalPan,null,j++);
			System.out.println(j);
		}
		
		for(Personnage p : minels.keySet()) {
			JPanel centralLocalBpanPan = new JPanel(new GridLayout(2, (minels.get(p).size()+1)/2,-1,-1));
			minels.get(p).stream().map(m -> m.genContentPane()).forEach(mpan -> {centralLocalBpanPan.add(mpan);mpan.setBorder(BorderFactory.createLineBorder(ParseUtils.randDarkBlueColor(),2));});
			JScrollPane centralLocalPan = new JScrollPane(centralLocalBpanPan);
			centerPan.insertTab(p.getNom(),null,centralLocalPan,null,j++);
			System.out.println(j);
		}
		
		for (int i = 0; i < centerPan.getTabCount(); i++) {
			System.out.println(i);
		
		}

		/*
		//Left Panel
		JPanel leftPan = new JPanel(new GridLayout(minels.size() + minelsSup.size(), 1));
		
		for(String s : minelsSup.keySet()) {
			JButton localButton = new JButton(s);
			localButton.addActionListener(e -> centerLayout.show(centerPan, s));
			try {
				localButton.setDropTarget(new NotADropTarget());
				localButton.getDropTarget().addDropTargetListener(new HoverSelect(centerLayout, centerPan, s));
			} catch (TooManyListenersException e1) {
				e1.printStackTrace();
			}
			leftPan.add(localButton);
		}
		for(Personnage p : minels.keySet()) {
			JButton localButton = new JButton(p.getNom());
			localButton.addActionListener(e -> centerLayout.show(centerPan, personnageIdentifier(p)));
			try {
				localButton.setDropTarget(new ObjetDropTarget(manager, p, ()->manager.inventoryUpdate(p)));
				localButton.getDropTarget().addDropTargetListener(new HoverSelect(centerLayout, centerPan, personnageIdentifier(p),DnDConstants.ACTION_MOVE));
			} catch (TooManyListenersException e1) {
				e1.printStackTrace();
			}
			leftPan.add(localButton);
		}
		
		globalPan.add(leftPan, BorderLayout.WEST);
		*/
		globalPan.add(centerPan, BorderLayout.CENTER);
		return globalPan;
	}
	
	
	public String personnageIdentifier(Personnage personnage) {
		return String.format("%08X",System.identityHashCode(personnage))+personnage.getNom();
	}
	
}
