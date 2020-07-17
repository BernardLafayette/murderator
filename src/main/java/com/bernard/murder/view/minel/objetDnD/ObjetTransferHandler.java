package com.bernard.murder.view.minel.objetDnD;

import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import com.bernard.murder.model.Objet;

public class ObjetTransferHandler extends TransferHandler {

	private static final long serialVersionUID = -8817518488023807932L;
	
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}
	
	@Override
	protected Transferable createTransferable(JComponent c) {
		
		@SuppressWarnings("unchecked")
		JList<Objet> liste = (JList<Objet>) c;
		
		List<Objet> selected = liste.getSelectedValuesList();
		
		ObjetTransferable transferable = new ObjetTransferable(selected);
		
		return transferable;
	}
	
}
