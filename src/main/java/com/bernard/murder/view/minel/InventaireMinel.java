package com.bernard.murder.view.minel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Inventaire;
import com.bernard.murder.model.Objet;
import com.bernard.murder.view.minel.objetDnD.ObjetDropTarget;
import com.bernard.murder.view.minel.objetDnD.ObjetTransferHandler;

public class InventaireMinel extends Minel {

	public InventaireMinel(GameManager manager, Inventaire inv) {
		super(manager);
		this.inv = inv;
	}
	
	public InventaireMinel(GameManager gm, YamlMapping ym) {
		this(gm,gm.getInventoryByName(ym.string("inventaire")));
	}

	JList<Objet> objets;
	Inventaire inv;

	@Override
	public JPanel genContentPane() {
		JPanel globalpan = new JPanel(new BorderLayout());
		
		
		if(inv.getInventoryName()!=null) {
			JLabel invName = new JLabel(inv.getInventoryName());
			globalpan.add(invName, BorderLayout.NORTH);
		}
		
		JPanel inventaire = new JPanel();

		JButton voler = new JButton("RandomItem");
		voler.addActionListener(e -> {
			objets.setSelectedIndex((int) (Math.random() * objets.getModel().getSize()));
		});
		
		objets = new JList<Objet>();
		objets.setCellRenderer(new ObjetListCellRenderer());
		objets.setDragEnabled(true);
		objets.setTransferHandler(new ObjetTransferHandler());
		updateObjets();
		final ObjetDropTarget odt = new ObjetDropTarget(manager, inv, this::updateObjets);
		objets.setDropTarget(odt);
		globalpan.setDropTarget(odt);
		
		manager.addInventoryUpdateListener(inv, this::updateObjets);
		
		inventaire.add(objets);
		
		globalpan.add(voler, BorderLayout.SOUTH);
		globalpan.add(inventaire, BorderLayout.CENTER);

		return globalpan;
	}

	private void updateObjets() {
		System.out.print("Updating "+inv+" with");
		manager.dumpCurrentState();
		Objet[] objz = new Objet[inv.getObjects().size()];
		objz = inv.getObjects().toArray(objz);
		objets.setListData(objz);
	}
	
	@Override
	public YamlMappingBuilder saveToYaml() {
		return Yaml.createYamlMappingBuilder().add("inventaire", inv.getInventoryName());
	}
	
	public static class ObjetListCellRenderer extends JLabel implements ListCellRenderer<Objet> {
		
		private static final long serialVersionUID = -7176962839330435585L;
		
		@Override
	    public Component getListCellRendererComponent(JList<? extends Objet> list, Objet objet, int index,
	            boolean isSelected, boolean cellHasFocus) {
			
	        setText(objet.getNom());
	        
	        if (isSelected) {
	        	setFont(getFont().deriveFont(Font.BOLD));
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else {
	        	setFont(getFont().deriveFont(Font.PLAIN));
	            setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }
	 
	        return this;
	    } 
	}
	
}
