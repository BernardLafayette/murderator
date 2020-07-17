package com.bernard.murder.view.minel;

import javax.swing.JPanel;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Personnage;

public class StatusMinel extends Minel {

	public StatusMinel(GameManager manager, Personnage personnage) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JPanel genContentPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public YamlMappingBuilder saveToYaml() {
		//TODO auto-generated thingy
		return Yaml.createYamlMappingBuilder();
	}

}
