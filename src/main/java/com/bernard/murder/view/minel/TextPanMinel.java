package com.bernard.murder.view.minel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.game.GameManager;

public class TextPanMinel extends Minel {
	
	String initTexte = "";
	JTextArea textArea;
	
	public TextPanMinel(GameManager manager) {
		super(manager);
	}
	
	public TextPanMinel(GameManager gm, YamlMapping ym) {
		super(gm);
		initTexte = ym.string("texte");
	}
	
	@Override
	public JPanel genContentPane() {
		JPanel globalPan = new JPanel(new BorderLayout());
		//globalPan.setBackground(ParseUtils.randColor());
		textArea = new JTextArea();
		textArea.setBorder(new EmptyBorder(23,23,23,23));
		textArea.setText(initTexte);
		
		//Color col = ParseUtils.randColor();
		//textArea.setBackground(col);
		//textArea.setForeground(ParseUtils.getContrastColor(col));
		
		globalPan.add(new JScrollPane(textArea),BorderLayout.CENTER);
		return globalPan;
	}

	@Override
	public YamlMappingBuilder saveToYaml() {
		return Yaml.createYamlMappingBuilder().add("texte", textArea!=null?textArea.getText():initTexte);
	}
	
}