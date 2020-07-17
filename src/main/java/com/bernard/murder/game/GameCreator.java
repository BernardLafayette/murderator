package com.bernard.murder.game;

import static com.bernard.murder.ParseUtils.mappingKeys;
import static com.bernard.murder.ParseUtils.parseTimeLength;
import static com.bernard.murder.ParseUtils.sequenceStream;

import static com.bernard.murder.ParseUtils.watch;

import static com.bernard.murder.YamlUtils.getMapping;
import static com.bernard.murder.YamlUtils.isMapping;
import static com.bernard.murder.YamlUtils.getSequence;
import static com.bernard.murder.YamlUtils.isSequence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amihaiemil.eoyaml.Node;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlInput;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlPrinter;
import com.amihaiemil.eoyaml.YamlSequence;
import com.bernard.murder.ParseUtils;
import com.bernard.murder.model.Action;
import com.bernard.murder.model.Objet;
import com.bernard.murder.model.Partie;
import com.bernard.murder.model.Personnage;
import com.bernard.murder.model.Piece;
import com.bernard.murder.model.Status;

public class GameCreator {
	
	
	public static Partie genFromFile(File toread) throws IOException{
		
		YamlInput input = Yaml.createYamlInput(toread);
		YamlMapping globalMap = input.readYamlMapping();
		
		YamlMapping yjoueurs = getMapping(globalMap.value("joueurs"));
		
		// Pour pouvoir créer les objets et les espaces personnels
		Set<String> playerNames = mappingKeys(yjoueurs).stream().map(n -> n.toString()).collect(Collectors.toSet());
		
		
		YamlMapping yactions = getMapping(globalMap.value("actions"));
		Set<Action> actions = yactions.keys()
				.stream()
				.map(n -> new Action(n.asScalar().value(), parseTimeLength(yactions.string(n))))
				.collect(Collectors.toSet());
		Map<String,Set<Action>> persActions = playerNames.stream()
				.collect(Collectors.toMap(Function.identity(),  s -> actions.stream().map(Action::clone).collect(Collectors.toSet())));
		
		YamlSequence yinventory = getSequence(globalMap.value("inventaire"));
		
		Set<String> objets = StreamSupport.stream(Spliterators.spliteratorUnknownSize(yinventory.iterator(),Spliterator.ORDERED),false)
				.map(n ->n.asScalar().value())
				.collect(Collectors.toSet());
		Map<String,Set<Objet>> persObjets = playerNames.stream()
				.collect(Collectors.toMap(Function.identity(),p -> objets.stream().map(o ->new Objet(String.format(o,p))).collect(Collectors.toSet())));
		
		YamlSequence ystatus = getSequence(globalMap.value("status"));
		Set<Status> status = sequenceStream(ystatus).map(n -> new Status(n.asScalar().value())).collect(Collectors.toSet());
		
		YamlMapping yespaces = getMapping(globalMap.value("espaces"));
		
		Map<String, Map<Objet, Integer>> objetsDansEspaces = yespaces.keys().stream().collect(Collectors.toMap(
			n -> n.asScalar().value(),
			n-> parseHiddenObjects(getSequence(yespaces.value(n)))
		));
		Set<Piece> espaceObjets = yespaces.keys().stream()
			.map(n -> new Piece(n.asScalar().value(), objetsDansEspaces.get(n.asScalar().value())))
			.collect(Collectors.toSet());
		
		YamlMapping yespacesPersos = getMapping(globalMap.value("espacesPersos"));
		
		Map<String,Set<Piece>> persespacesPersos = playerNames.stream().collect(Collectors.toMap(
			Function.identity(),
			p -> yespacesPersos.keys().stream()
				.map(e -> new Piece(
						String.format(e.asScalar().value(), p),
						parseHiddenObjects(getSequence(yespacesPersos.value(e)),p)
				))
			.collect(Collectors.toSet())
		));
		
		//Per perso settings
		for(YamlNode pn : yjoueurs.keys()) {
			String pname = pn.asScalar().value();
			persActions.get(pname).addAll(
					getMapping(getMapping(yjoueurs.value(pn)).value("actions")).keys()
				.stream()
				.map(n -> new Action(n.asScalar().value(), parseTimeLength(getMapping(getMapping(yjoueurs.value(pn)).value("actions")).string(n))))
				.collect(Collectors.toSet())
			);
			persObjets.get(pname).addAll(
				StreamSupport.stream(Spliterators.spliteratorUnknownSize(getSequence(getMapping(yjoueurs.value(pn)).value("inventaire")).iterator(),Spliterator.ORDERED),false)
				.map(n ->n.asScalar().value())
				.map(o ->new Objet(o))
				.collect(Collectors.toSet())
			);
			if(isMapping(getMapping(yjoueurs.value(pn)).value("espacePerso"))) 
				// Plusieurs espaces
				getMapping(getMapping(yjoueurs.value(pn)).value("espacePerso")).keys().forEach(n ->
					persespacesPersos.get(pname)
					.stream()
					.filter(p -> p.getNom().contentEquals(n.asScalar().value()))
					.findAny()
					.orElseGet(() -> new Piece(n.asScalar().value()))
					.insertObjects(parseHiddenObjects(getSequence(getMapping(getMapping(yjoueurs.value(pn)).value("espacePerso")).value(n))))
						
				);
			else 
				((persespacesPersos.get(pname).isEmpty())?
					Stream.of(new Piece(String.format("Espace de %s",pname))):persespacesPersos.get(pname).stream())
					.forEach(p -> p.insertObjects(parseHiddenObjects(getSequence(getMapping(yjoueurs.value(pn)).value("espacePerso")))));
					
			
		}
		
		
		
		Set<Personnage> personnages = playerNames.stream().map(p -> new Personnage(
				p,
				persObjets.get(p),
				persActions.get(p),
				status.stream().filter(st -> sequenceStream(ystatus)
						.filter(n -> n instanceof YamlMapping)
						.filter(n -> getMapping(n).string(st.getName())!=null)
						.filter(n -> getMapping(getMapping(n).value(st.getName())).string("onStart").contentEquals("true"))
						.findAny().isPresent())
						.collect(Collectors.toSet()),
				persespacesPersos.get(p)
			)).collect(Collectors.toSet());
		
		return new Partie(personnages, status, espaceObjets);
	}
	
	private static Map<Objet,Integer> parseHiddenObjects(YamlSequence sequence,Object... nameFormat){
		return sequenceStream(sequence).collect(Collectors.toMap(
				on -> new Objet(String.format((on.type()==Node.MAPPING)?(on.asMapping().keys().stream().map(nn -> nn.asScalar().value()).findAny().get()):on.asScalar().value(),nameFormat)),
				on -> ((on.type()==Node.MAPPING)?(on.asMapping().integer(on.asMapping().keys().stream().findAny().get())):-1)
			));
	}
	private static Map<Objet,Integer> parseHiddenObjects(YamlSequence sequence){
		return parseHiddenObjects(sequence, new Object[0]);
	}
	
	public static final void quickSave(File f, Partie p, YamlMapping minelsMap) throws IOException {
		YamlMappingBuilder globalMapping = Yaml.createYamlMappingBuilder()
		
		.add("personnages", 
			ParseUtils.setToMapSY(p.personnagesStream().collect(Collectors.toSet()), 
				perso -> perso.getNom(), 
				perso ->
				Yaml.createYamlMappingBuilder()
					
				.add("actions",ParseUtils.setToMapSY(perso.getActions(),
					act -> act.getName(),
					act -> Yaml.createYamlMappingBuilder()
						.add("basetime", Long.toString(act.getBasetime()))
						.add("triggerTime", Long.toString(act.getTriggertime()))
						.build()))
			
				.add("espacePerso",ParseUtils.setToMapSY(perso.streamEspacesPersos().collect(Collectors.toSet()),
						esp -> esp.getNom(),
						esp -> ParseUtils.setToSeqY(esp.streamHiddenObjects().map(etr -> Yaml.createYamlMappingBuilder().add(etr.getKey().getNom(), Integer.toString(etr.getValue())).build()).collect(Collectors.toSet()))
				))
				
				.add("status", ParseUtils.setToSeqS(perso.streamStatus().map(Status::getName).collect(Collectors.toSet())))
				
				.add("inventaire", ParseUtils.setToSeqS(perso.getInventaire().stream().map(Objet::getNom).collect(Collectors.toSet()))).build()
		))
		
		.add("status", ParseUtils.setToSeqS(p.statuzStream().map(Status::getName).collect(Collectors.toSet())))
		
		.add("pieces",ParseUtils.setToMapSY(p.piecesStream().collect(Collectors.toSet()),
				Piece::getNom, 
				esp -> ParseUtils.setToSeqY(esp.streamHiddenObjects().map(etr -> Yaml.createYamlMappingBuilder().add(etr.getKey().getNom(), Integer.toString(etr.getValue())).build()).collect(Collectors.toSet()))
		))
		.add("minels", minelsMap);
		
		
		YamlPrinter printer = Yaml.createYamlPrinter(new FileWriter(f));
		
		printer.print(globalMapping.build());
		
	}
	
	
	public static final QuicksavedPartie readQuickSave(File f) throws IOException {
		YamlInput input = Yaml.createYamlInput(f);
		YamlMapping globalMap = input.readYamlMapping();
		
		YamlSequence ystatus = getSequence(globalMap.value("status"));
		Set<Status> status = sequenceStream(ystatus).map(n -> new Status(n.asScalar().value())).collect(Collectors.toSet());
		
		
		YamlMapping yespaces = getMapping(globalMap.value("pieces"));
		Map<String, Map<Objet, Integer>> objetsDansEspaces = yespaces.keys().stream().collect(Collectors.toMap(
			n -> watch(watch(n).asScalar().value()),
			n-> parseHiddenObjects(getSequence(yespaces.value(n)))
		));
		Set<Piece> espaceObjets = yespaces.keys().stream()
			.map(n -> new Piece(n.asScalar().value(), objetsDansEspaces.get(n.asScalar().value())))
			.collect(Collectors.toSet());
		
		
		YamlMapping yjoueurs = getMapping(globalMap.value("personnages"));
		
		Set<Personnage> personnages = new HashSet<Personnage>();
		for(YamlNode pn : yjoueurs.keys()) {
			String pname = pn.asScalar().value();
			
			Set<Action> actions = getMapping(getMapping(yjoueurs.value(pn)).value("actions")).keys()
				.stream()
				.map(n -> new Action(n.asScalar().value(), Long.parseLong(getMapping(getMapping(getMapping(yjoueurs.value(pn)).value("actions")).value(n)).string("basetime")),
						Long.parseLong(getMapping(getMapping(getMapping(yjoueurs.value(pn)).value("actions")).value(n)).string("triggerTime"))))
				.collect(Collectors.toSet());
			
			Set<Objet> inventaire = 
					StreamSupport.stream(Spliterators.spliteratorUnknownSize(getSequence(getMapping(yjoueurs.value(pn)).value("inventaire")).iterator(),Spliterator.ORDERED),false)
					.map(n ->n.asScalar().value())
					.map(o ->new Objet(o))
					.collect(Collectors.toSet());
				
			Set<Piece> espacesPerso = getMapping(getMapping(yjoueurs.value(pn)).value("espacePerso")).keys().stream().map(n ->
				new Piece(n.asScalar().value(), parseHiddenObjects(getSequence(getMapping(getMapping(yjoueurs.value(pn)).value("espacePerso")).value(n))))).collect(Collectors.toSet());
				
			Set<Status> persoStatus = status.stream().filter(
					s -> !isSequence(getMapping(yjoueurs.value(pn)).value("status"))?false:getSequence(getMapping(yjoueurs.value(pn)).value("status")).values().stream().anyMatch(n -> n.asScalar().value().equals(s.getName()))
					).collect(Collectors.toSet());
			
					
			personnages.add(new Personnage(pname, inventaire, actions, persoStatus, espacesPerso));
		}
		
		YamlMapping minelsMap = getMapping(globalMap.value("minels"));
		
		return new QuicksavedPartie(new Partie(personnages, status, espaceObjets),minelsMap);
		
	}
	
	public static class QuicksavedPartie{
		Partie partie;
		YamlMapping minelsMap;
		
		
		public QuicksavedPartie(Partie partie, YamlMapping minelsMap) {
			this.partie = partie;
			this.minelsMap = minelsMap;
		}

		public Partie getPartie() {
			return partie;
		}


		public YamlMapping getMinelsMap() {
			return minelsMap;
		}
		
		
	}


}
