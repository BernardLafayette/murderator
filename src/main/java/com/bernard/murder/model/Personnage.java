package com.bernard.murder.model;

import java.util.Set;
import java.util.stream.Stream;

public class Personnage implements Inventaire{
	
	String nom;
	Set<Objet> inventaire;
	Set<Action> actions;
	Set<Status> status;
	Set<Piece> espacesPersos;
	
	
	
	public Personnage(String nom, Set<Objet> inventaire, Set<Action> actions, Set<Status> status,
			Set<Piece> espacesPersos) {
		super();
		this.nom = nom;
		this.inventaire = inventaire;
		this.actions = actions;
		this.status = status;
		this.espacesPersos = espacesPersos;
	}



	public String getNom() {
		return nom;
	}
	
	
	public Set<Objet> getInventaire() {
		return inventaire;
	}
	
	public Set<Action> getActions() {
		return actions;
	}
	
	public Stream<Piece> streamEspacesPersos(){
		return espacesPersos.stream();
	}



	@Override
	public String toString() {
		return "Personnage [nom=" + nom + ", inventaire=" + inventaire + ", actions=" + actions + ", status=" + status
				+ ", espacesPersos=" + espacesPersos + "]";
	}



	@Override
	public Set<Objet> getObjects() {
		return inventaire;
	}



	@Override
	public void removeObjet(Objet o) {
		System.out.println("Avant :"+inventaire);
		this.inventaire.remove(o);
		System.out.println("Après :"+inventaire);
		
	}



	@Override
	public void addObjet(Objet o) {
		this.inventaire.add(o);
	}



	public Stream<Status> streamStatus() {
		return status.stream();
	}



	@Override
	public String getInventoryName() {
		return "Inventaire de "+getNom();
	}



	public Set<Piece> espacePersos() {
		return espacesPersos;
	} 
	
	
	
	
}
