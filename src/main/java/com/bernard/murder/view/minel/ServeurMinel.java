package com.bernard.murder.view.minel;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.audio.AudioServer;
import com.bernard.murder.game.GameManager;

public class ServeurMinel extends Minel {

	AudioServer serveur;
	
	public ServeurMinel(GameManager manager) {
		super(manager);
		serveur = new AudioServer();
	}

	public ServeurMinel(GameManager manager,YamlMapping ym) {
		super(manager);
		serveur = new AudioServer();
	}

	@Override
	public JPanel genContentPane() {
		JPanel pan = new JPanel();
		JLabel label = new JLabel("Rien pour l'instant");
		pan.add(label);
		return pan;
	}
	
	@Override
	public YamlMappingBuilder saveToYaml() {
		return Yaml.createYamlMappingBuilder();
	}

}
