package com.bernard.murder.game;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Timer;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.bernard.murder.model.Inventaire;
import com.bernard.murder.model.Objet;
import com.bernard.murder.model.Partie;
import com.bernard.murder.model.Personnage;

public class GameManager {
	
	Partie partie;
	Map<Inventaire,Set<Runnable>> inventoryUpdateListeners;
	
	long startTime;
	
	Timer quickSaver;
	
	Supplier<YamlMapping> minelsQuicksaver;
	
	public GameManager(Partie partie) {
		this.partie = partie;
		this.inventoryUpdateListeners = new HashMap<Inventaire, Set<Runnable>>();
		this.minelsQuicksaver = () -> Yaml.createYamlMappingBuilder().build();
		startTime = System.currentTimeMillis();
		
		quickSaver = new Timer(10_000, e -> this.quickSave());
		quickSaver.start();
	}

	public synchronized void moveObjet(Objet o, Inventaire toInv) {
		Inventaire inv = partie.findObjectInventory(o);
		System.out.println("Moving "+o+" from "+inv+" to "+toInv);
		inv.removeObjet(o);
		toInv.addObjet(o);
		inventoryUpdate(inv);
		inventoryUpdate(toInv);
	}
	
	public void inventoryUpdate(Inventaire inv) {
		if(!inventoryUpdateListeners.containsKey(inv))return;
		for(Runnable r : inventoryUpdateListeners.get(inv))
			r.run();
	}

	public void dumpCurrentState() {
		System.out.println(partie);
	}
	
	public void addInventoryUpdateListener(Inventaire inv, Runnable runnable) {
		if(!inventoryUpdateListeners.containsKey(inv)) 
			inventoryUpdateListeners.put(inv, new HashSet<Runnable>());
		inventoryUpdateListeners.get(inv).add(runnable);
	}
	
	public void quickSave() {
		System.out.println("Quicksaving");
		File toSave = new File(quickSaveFilename());
		File tempOldSave = new File(quickSaveFilename()+".tmp");
		if(toSave.exists())toSave.renameTo(tempOldSave);
		
		try {
			GameCreator.quickSave(toSave, partie,minelsQuicksaver.get());
			System.out.println("Quicksaved");
			if(tempOldSave.exists())tempOldSave.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String quickSaveFilename() {
		return "murder-"+DateTimeFormatter.ofPattern("uu-MM-dd_HH'h'mm").withZone(ZoneId.systemDefault()).withLocale(Locale.getDefault()).format(Instant.ofEpochMilli(startTime))+".bernard.quickmurder";
	}

	public Set<Inventaire> getEveryInventaire() {
		Set<Inventaire> inventaires = new HashSet<Inventaire>();
		inventaires.addAll(partie.pieces());
		inventaires.addAll(partie.personnages());
		partie.personnagesStream().forEach(p -> inventaires.addAll(p.espacePersos()));
		return inventaires;
	}
	
	public Set<Inventaire> getEveryInventaireByName(Set<String> names) {
		Set<Inventaire> inventaires = new HashSet<Inventaire>();
		inventaires.addAll(partie.pieces());
		inventaires.addAll(partie.personnages());
		partie.personnagesStream().forEach(p -> inventaires.addAll(p.espacePersos()));
		return inventaires.stream().filter(i -> names.contains(i.getInventoryName())).collect(Collectors.toSet());
	}

	public Personnage getPersoByName(String key) {
		return partie.personnagesStream().filter(p -> key.equalsIgnoreCase(p.getNom())).findAny().orElse(null);
	}
	
	public void bindMinelQuicksaver(Supplier<YamlMapping> minelsQuicksaver) {
		this.minelsQuicksaver = minelsQuicksaver;
	}

	public Inventaire getInventoryByName(String name) {
		Set<Inventaire> inventaires = new HashSet<Inventaire>();
		inventaires.addAll(partie.pieces());
		inventaires.addAll(partie.personnages());
		partie.personnagesStream().forEach(p -> inventaires.addAll(p.espacePersos()));
		return inventaires.stream().filter(i -> name.equalsIgnoreCase(i.getInventoryName())).findAny().orElseGet(()->{
			System.out.println("JE n'ai pas trouvé l'inventaire "+name+" dans la liste "+inventaires.stream().map(Inventaire::getInventoryName).collect(Collectors.joining(",")));
			return null;
		});
	}
	
}
