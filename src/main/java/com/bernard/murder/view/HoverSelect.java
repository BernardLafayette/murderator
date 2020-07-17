package com.bernard.murder.view;

import java.awt.CardLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class HoverSelect implements DropTargetListener,Runnable{

	long enteredTime = -1;
	long waitTime = 500_000_000;
	Runnable onSelect;
	Thread plannedThread;
	int dropAcceptance;
	
	public HoverSelect(Runnable onSelect,long waitTime,int shouldAcceptDrop){
		this.onSelect = onSelect;
		this.waitTime = waitTime;
		this.dropAcceptance = shouldAcceptDrop;
	}
	public HoverSelect(CardLayout layout, JPanel panel, String identifier, long waitTime,int shouldAcceptDrop) {
		this(() -> layout.show(panel, identifier),waitTime,shouldAcceptDrop);
	}
	
	public HoverSelect(JTabbedPane tabbedPan, int index, long waitTime,int shouldAcceptDrop) {
		this(() -> tabbedPan.setSelectedIndex(index),waitTime,shouldAcceptDrop);
	}
	
	public HoverSelect(Runnable onSelect,int shouldAcceptDrop){
		this.onSelect = onSelect;
		this.dropAcceptance = shouldAcceptDrop;
	}
	
	public HoverSelect(CardLayout layout, JPanel panel, String identifier,int shouldAcceptDrop) {
		this(() -> layout.show(panel, identifier),shouldAcceptDrop);
	}
	public HoverSelect(Runnable onSelect,long waitTime){
		this(onSelect,waitTime,DnDConstants.ACTION_NONE);
	}
	public HoverSelect(CardLayout layout, JPanel panel, String identifier, long waitTime) {
		this(() -> layout.show(panel, identifier),waitTime,DnDConstants.ACTION_NONE);
	}
	public HoverSelect(JTabbedPane tabbedPan, int index, int shouldAcceptDrop) {
		this(() -> tabbedPan.setSelectedIndex(index),shouldAcceptDrop);
	}
	public HoverSelect(JTabbedPane tabbedPan, int index, long waitTime) {
		this(() -> tabbedPan.setSelectedIndex(index),waitTime,DnDConstants.ACTION_NONE);
	}
	
	public HoverSelect(Runnable onSelect){
		this(onSelect, DnDConstants.ACTION_NONE);
	}
	public HoverSelect(CardLayout layout, JPanel panel, String identifier) {
		this(() -> layout.show(panel, identifier),DnDConstants.ACTION_NONE);
	}
	public HoverSelect(JTabbedPane tabbedPan, int index) {
		this(() -> tabbedPan.setSelectedIndex(index),DnDConstants.ACTION_NONE);
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent e) {
		enteredTime = System.nanoTime();
		System.out.println("entré");
		plannedThread = new Thread(()-> {
			try {
				Thread.sleep(waitTime/1_000_000, (int)(waitTime%1_000_000));
				onSelect.run();
			} catch (InterruptedException e1) {}
		});
		plannedThread.run();
		e.acceptDrag(dropAcceptance);
	}

	@Override
	public void dragExit(DropTargetEvent e) {
		if(plannedThread!=null)plannedThread.interrupt();
		plannedThread = null;
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {}
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}
	@Override
	public void drop(DropTargetDropEvent dtde) {}
	@Override
	public void run() {
		enteredTime = System.nanoTime();
		System.out.println("entré");
		plannedThread = new Thread(()-> {
			try {
				Thread.sleep(waitTime/1_000_000, (int)(waitTime%1_000_000));
				onSelect.run();
			} catch (InterruptedException e1) {}
		});
		plannedThread.run();
	}
	
}
