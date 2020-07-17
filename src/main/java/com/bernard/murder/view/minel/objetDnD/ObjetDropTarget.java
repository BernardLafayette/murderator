package com.bernard.murder.view.minel.objetDnD;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;

import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Inventaire;
import com.bernard.murder.model.Objet;

public class ObjetDropTarget extends DropTarget {

	private static final long serialVersionUID = 4114342978115089000L;
	
	GameManager manager;
	Inventaire toInv;
	Runnable[] toUpdate;
	
	public ObjetDropTarget(GameManager manager,Inventaire toInventaire,Runnable... update) {
		this.manager = manager;
		this.toInv = toInventaire;
		this.toUpdate = update;
	}
	
	@Override
	public synchronized void drop(DropTargetDropEvent e) {
		try {
            e.acceptDrop(DnDConstants.ACTION_COPY);
            
            @SuppressWarnings("unchecked")
			List<Objet> droppedObjets = (List<Objet>) e.getTransferable().getTransferData(ObjetTransferable.objetDataFlavor);
            
            for(Objet o : droppedObjets) {
            	manager.moveObjet(o,toInv);
            }
            for(Runnable runnable : toUpdate)
            	runnable.run();
            manager.dumpCurrentState();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
