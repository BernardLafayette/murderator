package com.bernard.murder.model;

public class Objet {
	
	String nom;

	public Objet(String nom) {
		this.nom = nom;
	}
	
	public String getNom() {
		return nom;
	}

	@Override
	public String toString() {
		return "Objet [nom=" + nom + "]";
	}
	
	
	
}
