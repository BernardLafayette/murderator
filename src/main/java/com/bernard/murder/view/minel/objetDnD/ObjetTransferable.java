package com.bernard.murder.view.minel.objetDnD;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.bernard.murder.model.Objet;

public class ObjetTransferable implements Transferable{
	
	private final List<Objet> objets;
	private final DataFlavor[] flavors;
	public static final DataFlavor objetDataFlavor = new DataFlavor(Objet.class, "fichierMurder");

	public ObjetTransferable(Collection<Objet> objets) {
        this.objets = Collections.unmodifiableList(
                new ArrayList<Objet>(objets));
        this.flavors = new DataFlavor[] { ObjetTransferable.objetDataFlavor };
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.stream(flavors).anyMatch(f -> f == flavor);
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if(isDataFlavorSupported(flavor))
			return objets;
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
