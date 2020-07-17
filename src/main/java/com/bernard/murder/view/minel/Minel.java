package com.bernard.murder.view.minel;

import javax.swing.JPanel;

import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.game.GameManager;

public abstract class Minel{
	
	GameManager manager;
	
	
	
	public Minel(GameManager manager) {
		this.manager = manager;
	}
	
	public Minel(GameManager manager, YamlMapping node) {
		this.manager = manager;
	}
	
	public abstract JPanel genContentPane();
	
	public abstract YamlMappingBuilder saveToYaml();
	
}
