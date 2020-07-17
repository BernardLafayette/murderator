package com.bernard.murder.model;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Partie {
	
	
	Set<Personnage> personnages;
	Set<Status> statuz;
	Set<Piece> piece;
	
	
	
	public Partie(Set<Personnage> personnages, Set<Status> statuz, Set<Piece> piece) {
		this.personnages = personnages;
		this.statuz = statuz;
		this.piece = piece;
	}

	public int persoCount() {
		return personnages.size();
	}
	
	public Stream<Personnage> personnagesStream() {
		return personnages.stream();
	}

	public Stream<Piece> piecesStream() {
		return piece.stream();
	}

	@Override
	public String toString() {
		return "Partie [personnages=" + personnages + ", statuz=" + statuz + ", piece=" + piece + "]";
	}

	public Inventaire findObjectInventory(Objet o) {
		Optional<Personnage> persoInv = personnages.stream().filter(p -> p.inventaire.contains(o)).findAny();
		if(persoInv.isPresent())return persoInv.get();
		
		Optional<Piece> persoInvRoom = personnages.stream()
				.map(p -> p.espacesPersos.stream()
					.filter(ep -> ep.contenu.keySet().contains(o)).findAny())
				.filter(Optional::isPresent).map(Optional::get).findAny();
		if(persoInvRoom.isPresent())return persoInvRoom.get();
		
		Optional<Piece> invRoom = piece.stream()
					.filter(ep -> ep.contenu.keySet().contains(o)).findAny();
		if(invRoom.isPresent())return invRoom.get();
		
		return null;
	}
	
	public Stream<Status> statuzStream() {
		return statuz.stream();
	}

	public Set<Piece> pieces() {
		return piece;
	}
	
	public Set<Personnage> personnages() {
		return personnages;
	}
	
	
	
}
