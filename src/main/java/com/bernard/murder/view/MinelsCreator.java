package com.bernard.murder.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.bernard.murder.YamlUtils;
import com.bernard.murder.game.GameCreator.QuicksavedPartie;
import com.bernard.murder.game.GameManager;
import com.bernard.murder.model.Partie;
import com.bernard.murder.model.Personnage;
import com.bernard.murder.view.minel.ActionsMinel;
import com.bernard.murder.view.minel.InventaireMinel;
import com.bernard.murder.view.minel.Minel;
import com.bernard.murder.view.minel.ObjetSearchMinel;
import com.bernard.murder.view.minel.ServeurMinel;
import com.bernard.murder.view.minel.TextPanMinel;

public class MinelsCreator {

	public static Map<String, List<Minel>> genSupMinels(Partie qpartie, GameManager manager) {
		
		Map<String, List<Minel>> outPute = new HashMap<String, List<Minel>>();
		List<Minel> generalMinels = new ArrayList<Minel>();
		
		List<Minel> piecesMinels = new ArrayList<>();
		qpartie.piecesStream().map(p -> new InventaireMinel(manager, p)).forEach(m -> piecesMinels.add(m));
		
		generalMinels.add(new TextPanMinel(manager));
		generalMinels.add(new ObjetSearchMinel(manager, manager.getEveryInventaire()));
		generalMinels.add(new ServeurMinel(manager));
		
		outPute.put("Général", generalMinels);
		outPute.put("Pièces", piecesMinels);
		return outPute;
	}
	
	public static Map<Personnage, List<Minel>> genPersoMinels(Partie partie, GameManager manager) {
		return partie.personnagesStream().collect(Collectors.toMap(Function.identity(), p -> genMinelsForPerso(partie,manager,p)));
	}
	
	private static List<Minel> genMinelsForPerso(Partie partie, GameManager manager, Personnage personnage){
		List<Minel> minels = new ArrayList<Minel>();
		
		minels.add(new ActionsMinel(manager, personnage));
		
		minels.add(new InventaireMinel(manager, personnage));
		personnage.streamEspacesPersos().map(p -> new InventaireMinel(manager, p)).forEach(m -> minels.add(m));
		
		minels.add(new TextPanMinel(manager));
		
		
		return minels;
	}
	
	public static Map<String, List<Minel>> genSupMinels(QuicksavedPartie qpartie, GameManager manager) {
		return genMinels("sups", qpartie, manager);
	}
	
	public static Map<Personnage, List<Minel>> genPersoMinels(QuicksavedPartie qpartie, GameManager manager) {
		return genMinels("persos", qpartie, manager).entrySet().stream().collect(Collectors.toMap(e -> manager.getPersoByName(e.getKey()), Map.Entry::getValue));
	}
	
	
	private static Map<String, List<Minel>> genMinels(String minelType,QuicksavedPartie qpartie, GameManager manager) {
		Map<String, List<Minel>> outPute = new HashMap<String, List<Minel>>();
		
		YamlMapping supMap = qpartie.getMinelsMap().yamlMapping(minelType);
		
		for(YamlNode n : supMap.keys()) {
			YamlSequence minels = supMap.yamlSequence(n);
			String minelName = n.asScalar().value();
			List<Minel> minelList = new ArrayList<Minel>();
			for(YamlNode minelNode : minels) {
				YamlMapping minel = minelNode.asMapping();
				String className = minel.value("minelClass").asScalar().value();
				try {
					Class<?> minelClass = Class.forName(className);
					Constructor<?> minelConstructor = minelClass.getConstructor(GameManager.class,YamlMapping.class);
					Object minelObj = minelConstructor.newInstance(manager,minel);
					if(minelObj instanceof Minel) {
						minelList.add((Minel) minelObj);
					}else {
						throw new ClassCastException();
					}
				} catch (InstantiationException e) {
					System.err.println("La classe "+className+" n'est pas prévu pour être instanciable !");
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					System.err.println("Le constructeur (GameManager,YamlMapping) de la classe "+className+" n'est pas public");
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					System.err.println("Euhhhhhhhhh, ce doit être une erreur de code interne");
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					System.err.println("La recréation du minel de classe "+className+" a foiré");
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					System.err.println("La classe "+className+" n'a pas de constructeur (GameManager,YamlMapping)");
					e.printStackTrace();
				} catch (SecurityException e) {
					System.err.println("La classe "+className+" n'est pas accessible");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.err.println("La classe "+className+" n'est pas chargée !");
					e.printStackTrace();
				} catch (ClassCastException e) {
					System.err.println("La classe "+className+" n'est pas un Minel !");
					e.printStackTrace();
				}
			}
			outPute.put(minelName, minelList);
		}
		
		return outPute;
	}

	public static YamlMapping createMinelQuicksave(Map<Personnage, List<Minel>> persoMinels, Map<String, List<Minel>> supMinels) {
		YamlMappingBuilder supMapBuilder = Yaml.createYamlMappingBuilder();
		for(String supName : supMinels.keySet()) {
			List<YamlNode> seq = supMinels.get(supName).stream().map(m -> m.saveToYaml().add("minelClass", m.getClass().getName()).build()).collect(Collectors.toList());
			supMapBuilder = supMapBuilder.add(supName, YamlUtils.listToSeq(seq));
		}
		YamlMappingBuilder persoBuilder = Yaml.createYamlMappingBuilder();
		for(Personnage perso : persoMinels.keySet())
			persoBuilder = persoBuilder.add(perso.getNom(), YamlUtils.listToSeq(persoMinels.get(perso).stream().map(m -> m.saveToYaml().add("minelClass", m.getClass().getName()).build()).collect(Collectors.toList())));
		
		
		return Yaml.createYamlMappingBuilder()
			.add("sups", supMapBuilder.build())
			.add("persos", persoBuilder.build())
			.build();
	}
	
}
