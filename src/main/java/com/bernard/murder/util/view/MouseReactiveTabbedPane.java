package com.bernard.murder.util.view;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

import javax.swing.JTabbedPane;

import com.bernard.murder.view.minel.objetDnD.NotADropTarget;

public class MouseReactiveTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = -4985256252193243545L;
	long waitTime = 500_000_000;
	Thread plannedThread;
	int dropAcceptance;
	
	int hoverIndex = -1;
	long enteredTime = -1;

	public MouseReactiveTabbedPane(int tabPlacement) {
		super(tabPlacement);
		
		final MouseReactiveTabbedPane pane = this;
		
		this.setDropTarget(new NotADropTarget());

		try {
			this.getDropTarget().addDropTargetListener(new DropTargetListener() {

				
				@Override
				public void dragOver(DropTargetDragEvent dtde) {
					int tab = getTab(dtde);
					System.out.println(">"+tab+"/"+hoverIndex+"-"+enteredTime);
					if(tab==-1) {
						enteredTime=-1;
						hoverIndex=-1;
						return;
					}
					if(tab!=hoverIndex) {
						//On viens d'entrer dans un nouveau tab !
						enteredTime=System.nanoTime();
						hoverIndex=tab;
						if(plannedThread==null){
							plannedThread = new Thread(() -> {
								while(System.nanoTime()>enteredTime+waitTime) {
									try {
										Thread.sleep(2);
									} catch (InterruptedException e1) {}
								}
								if(getTab() == hoverIndex) 
									pane.setSelectedIndex(hoverIndex);
								hoverIndex = -1;
								enteredTime = -1;
								plannedThread = null;
							});
							plannedThread.start();
						}
					}
					dtde.acceptDrag(dropAcceptance);
				}


				@Override
				public void dropActionChanged(DropTargetDragEvent dtde) {}
				@Override
				public void drop(DropTargetDropEvent dtde) {}
				@Override
				public void dragExit(DropTargetEvent dte) {
					enteredTime=-1;
					hoverIndex=-1;
					return;
				}
				@Override
				public void dragEnter(DropTargetDragEvent e) {}
			});
		} catch (TooManyListenersException e1) {
			e1.printStackTrace();
		}

	}
	
	public int getTab(DropTargetDragEvent dtde) {
		for (int i = 0; i < this.getTabCount(); i++) 
			if (this.getBoundsAt(i).contains(dtde.getLocation())) 
				return i;
		return -1;
			
	}
	public int getTab() {
		Point loc = MouseInfo.getPointerInfo().getLocation();
		Point screen = this.getLocationOnScreen();
		Point point = new Point(loc.x - screen.x, loc.y - screen.y);
		for (int i = 0; i < this.getTabCount(); i++)
			if (this.getBoundsAt(i).contains(point)) 
				return i;
		return -1;
			
	}

}