package com.bernard.murder.model;

import java.util.Set;

public interface Inventaire {

	Set<Objet> getObjects();

	void removeObjet(Objet o);

	void addObjet(Objet o);

	String getInventoryName();

}
