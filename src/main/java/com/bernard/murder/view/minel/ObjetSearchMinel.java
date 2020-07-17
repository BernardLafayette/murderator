package com.bernard.murder.view.minel;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.bernard.murder.ParseUtils;
import com.bernard.murder.YamlUtils;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Inventaire;
import com.bernard.murder.model.Objet;
import com.bernard.murder.util.view.SimpleDocumentListener;

public class ObjetSearchMinel extends Minel {
	
	Set<Inventaire> overwatchedInventories;
	
	Map<Objet,Inventaire> objets;
	
	public ObjetSearchMinel(GameManager manager, Set<Inventaire> toOverwatch) {
		super(manager);
		overwatchedInventories = toOverwatch;
		objets = new HashMap<Objet, Inventaire>();
		for(Inventaire i : overwatchedInventories) {
			i.getObjects().stream().forEach(o -> objets.put(o, i));
			manager.addInventoryUpdateListener(i, ()->i.getObjects().stream().forEach(o -> objets.put(o, i)));
		}
	}
	
	public ObjetSearchMinel(GameManager gm,YamlMapping ym) {
		this(gm,
				gm.getEveryInventaireByName(
				ym.yamlSequence("overwatchedInventories")
				.values()
				.stream()
				.map(YamlNode::asScalar)
				.map(Scalar::value)
				.collect(Collectors.toSet())
			)
		);
	}

	@Override
	public JPanel genContentPane() {
		
		JPanel globalPan = new JPanel(new BorderLayout());
		
		JTextField searchField = new JTextField();
		searchField.setToolTipText("Objet à chercher");
		
		JList<InventorizedObject> searchResults = new JList<>();
		
		
		
		searchField.getDocument().addDocumentListener(new SimpleDocumentListener() {
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				System.out.println("Updated to "+e.getDocument().toString());
				String searchText = searchField.getText();
				if(searchText.isBlank()) {
					searchResults.setListData(new InventorizedObject[0]);
					return;
				}
				Set<InventorizedObject> startMatch = new HashSet<>();
				Set<InventorizedObject> anyMatch  = new HashSet<>();
				Set<InventorizedObject> subwordMatch = new HashSet<>();
				
				for(Objet o : objets.keySet()) {
					System.out.println(o+"->"+searchText);
					if(o.getNom().startsWith(searchText))
						startMatch.add(new InventorizedObject(o,objets.get(o)));
					else if(o.getNom().contains(searchText))
						anyMatch.add(new InventorizedObject(o,objets.get(o)));
					else if(ParseUtils.isSubWord(o.getNom(), searchText))
						subwordMatch.add(new InventorizedObject(o,objets.get(o)));
				}
				InventorizedObject[] results = 
				Stream.concat(Stream.concat(
						startMatch.stream().sorted((s,v) -> s.objet.getNom().compareToIgnoreCase(v.objet.getNom())),
						anyMatch.stream().sorted((s,v) -> s.objet.getNom().compareToIgnoreCase(v.objet.getNom()))),
						subwordMatch.stream().sorted((s,v) -> s.objet.getNom().compareToIgnoreCase(v.objet.getNom())))
				.toArray(InventorizedObject[]::new);
				
				searchResults.setListData(results);
				
			}
		});
		
		globalPan.add(searchField, BorderLayout.NORTH);
		globalPan.add(searchResults,BorderLayout.CENTER);
		
		return globalPan;
	}
	
	@Override
	public YamlMappingBuilder saveToYaml() {
		return Yaml.createYamlMappingBuilder().add("overwatchedInventories", 
				YamlUtils.listToSeqString(overwatchedInventories.stream()
				.map(Inventaire::getInventoryName)
				.collect(Collectors.toList())));
	}
	
	public static class InventorizedObject{
		Objet objet;
		Inventaire inventaire;
		
		
		
		public InventorizedObject(Objet objet, Inventaire inventaire) {
			this.objet = objet;
			this.inventaire = inventaire;
		}



		public void changeInventory(Inventaire inventaire) {
			this.inventaire = inventaire;
		}
		
		@Override
		public String toString() {
			return objet.getNom()+" ("+inventaire.getInventoryName()+")";
		}
		
		
	}

}
