package com.bernard.murder.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

public class Piece implements Inventaire{
	
	String nom;
	
	Map<Objet,Integer> contenu;
	
	public Piece(String nom) {
		this(nom, new HashMap<Objet, Integer>());
	}
	
	public Piece(String nom, Map<Objet, Integer> contenu) {
		this.nom = nom;
		this.contenu = contenu;
	}

	@Override
	public String toString() {
		return "Piece [nom=" + nom + ", contenu=" + contenu + "]";
	}

	public String getNom() {
		return nom;
	}

	@Override
	public Set<Objet> getObjects() {
		return contenu.keySet();
	}

	public void insertObjects(Map<Objet, Integer> objs) {
		contenu.putAll(objs);
	}

	@Override
	public void removeObjet(Objet o) {
		this.contenu.remove(o);
	}

	@Override
	public void addObjet(Objet o) {
		this.contenu.put(o,-1);
	}
	
	public Stream<Entry<Objet, Integer>> streamHiddenObjects() {
		return contenu.entrySet().stream();
	}

	@Override
	public String getInventoryName() {
		return nom;
	}
	
	
	
}
