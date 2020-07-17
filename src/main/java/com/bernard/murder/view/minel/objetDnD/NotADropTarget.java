package com.bernard.murder.view.minel.objetDnD;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

public class NotADropTarget extends DropTarget {

	private static final long serialVersionUID = -2448755310779755579L;
	
	public NotADropTarget() {}
	
	@Override
	public synchronized void drop(DropTargetDropEvent dtde) {
		dtde.rejectDrop();
	}
	
}
