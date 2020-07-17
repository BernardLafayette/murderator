package com.bernard.murder.view.minel;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.Timer;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.ParseUtils;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Action;
import com.bernard.murder.model.Personnage;

public class ActionsMinel extends Minel {
	
	public static final String availableText = "Disponible";
	public static final String waitingTimeText = "Reste %s, reset à %s";
	
	Personnage personnage;
	
	Collection<Action> updatingActions;
	Map<Action,JLabel> actionStatusTexts = new HashMap<>(); 
	Map<Action,JButton> actionButtons = new HashMap<>(); 

	public ActionsMinel(GameManager manager, Personnage perso) {
		super(manager);
		this.personnage = perso;
		updatingActions = new HashSet<Action>();
		updatingActions.addAll(perso.getActions());
	}
	
	public ActionsMinel(GameManager manager, YamlMapping ym) {
		this(manager,manager.getPersoByName(ym.string("personnage")));
	}

	@Override
	public JPanel genContentPane() {
		JPanel globalPan = new JPanel(new BorderLayout());
		
		JPanel actionsListPan = new JPanel();
		actionsListPan.setLayout(new BoxLayout(actionsListPan, BoxLayout.PAGE_AXIS));
		for(Action a : personnage.getActions()) {
			JPanel actionControlPanel = new JPanel(new BorderLayout());
			JLabel actionName = new JLabel(a.getName());
			JButton actionButton = new JButton("GO");
			JLabel actionStatusText = new JLabel(availableText);
			
			actionButton.addActionListener(e->launchAction(a));
			actionButtons.put(a, actionButton);
			actionStatusTexts.put(a, actionStatusText);
			
			actionControlPanel.add(actionButton, BorderLayout.EAST);
			actionControlPanel.add(actionName, BorderLayout.NORTH);
			actionControlPanel.add(actionStatusText, BorderLayout.SOUTH);
			
			actionsListPan.add(actionControlPanel);
			actionsListPan.add(new JSeparator());
			
			Timer timer = new Timer(100, e->updateTexts());
			timer.start();
		}
		JScrollPane globalScroll = new JScrollPane(actionsListPan);
		JLabel titleLabel = new JLabel("Actions de "+personnage.getNom());
		globalPan.add(titleLabel,BorderLayout.NORTH);
		globalPan.add(globalScroll,BorderLayout.CENTER);
		
		updateTexts();
		
		return globalPan;
	}
	
	private void launchAction(Action a) {
		if(!a.canBeLaunched())return;
		a.launch();
		actionButtons.get(a).setEnabled(false);
		updateText(a);
		updatingActions.add(a);
	}
	
	@Override
	public YamlMappingBuilder saveToYaml() {
		return Yaml.createYamlMappingBuilder().add("personnage", personnage.getNom());
	}
	
	public void updateTexts() {
		updatingActions.stream().filter(Action::hasFinished).collect(Collectors.toSet())
			.stream().forEach(a -> {
				actionStatusTexts.get(a).setText(availableText);
				actionButtons.get(a).setEnabled(true);
				updatingActions.remove(a);
			});
		updatingActions.forEach(this::updateText);
	}
	
	public void updateText(Action a) {
		actionStatusTexts.get(a).setText(String.format(waitingTimeText, ParseUtils.dumpTimeLength(a.timeToWaitLeft()), ParseUtils.dumpHourDate(a.dateReset())));
	}
	
	

}
