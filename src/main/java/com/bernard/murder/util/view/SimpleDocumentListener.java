package com.bernard.murder.util.view;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class SimpleDocumentListener implements DocumentListener {

	@Override
	public void insertUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

}
