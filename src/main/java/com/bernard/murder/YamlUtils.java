package com.bernard.murder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.amihaiemil.eoyaml.Node;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;


public class YamlUtils {
	
	
	public static final YamlSequence listToSeq(List<YamlNode> nodes) {
		YamlSequenceBuilder ysb = Yaml.createYamlSequenceBuilder();
		for(YamlNode n : nodes)
			ysb = ysb.add(n);
		return ysb.build();
	}
	
	public static final YamlSequence listToSeqString(List<String> nodes) {
		YamlSequenceBuilder ysb = Yaml.createYamlSequenceBuilder();
		for(String n : nodes)
			ysb = ysb.add(n);
		return ysb.build();
	}
	
	public static final YamlMapping mapToMapping(Map<YamlNode,YamlNode> nodes) {
		YamlMappingBuilder ysb = Yaml.createYamlMappingBuilder();
		for(Entry<YamlNode, YamlNode> n : nodes.entrySet())
			ysb = ysb.add(n.getKey(),n.getValue());
		return ysb.build();
	}
	
	public static final YamlSequence getSequence(YamlNode node) {
		if(node.type()==Node.SEQUENCE)return node.asSequence();
		if(node.type()==Node.SCALAR && node.asScalar().value().contentEquals("[]"))return Yaml.createYamlSequenceBuilder().build();
		throw new IllegalArgumentException("Le noeud n'est pas une séquence");
	}
	
	public static final YamlMapping getMapping(YamlNode node) {
		if(node.type()==Node.MAPPING)return node.asMapping();
		if(node.type()==Node.SCALAR && node.asScalar().value().contentEquals("{}"))return Yaml.createYamlMappingBuilder().build();
		throw new IllegalArgumentException("Le noeud n'est pas une séquence");
	}
	
	public static final boolean isMapping(YamlNode node) {
		return (node.type()==Node.MAPPING) || (node.type()==Node.SCALAR && node.asScalar().value().contentEquals("{}"));
	}
	
	public static final boolean isSequence(YamlNode node) {
		return (node.type()==Node.SEQUENCE) || (node.type()==Node.SCALAR && node.asScalar().value().contentEquals("[]"));
	}
	
	
	
	 /**
	 * (Copied from Collectors class)
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Function<A,R> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }
	
}
