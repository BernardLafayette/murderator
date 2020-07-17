package com.bernard.murder;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;

public class ParseUtils {
	
	
	static Pattern timeLengthPattern = Pattern.compile("^(([0-9]+)h)?(([0-9]+)m)?(([0-9]+)s?)?$");
	
	public static long parseTimeLength(String tl) {
		Matcher mtch = timeLengthPattern.matcher(tl);
		if(!mtch.matches())throw new IllegalArgumentException("La chaine de caractères «"+tl+"» ne décrit pas un intervalle de temps normalisé");
		int h = mtch.group(2)==null?0:Integer.parseInt(mtch.group(2));
		int m = mtch.group(4)==null?0:Integer.parseInt(mtch.group(4));
		int s = mtch.group(6)==null?0:Integer.parseInt(mtch.group(6));
		return (h*3600+m*60+s)*1000;
	}
	
	public static <T> T watch(T el) {
		System.out.println(el);
		return el;
	}
	
	
	public static String dumpTimeLength(long t) {
		long h=t/1000/3600, m=t/1000/60%60, s=t/1000%60;
		return (h!=0?h+"h":"" )+ (m!=0?m+"m":"")+(s!=0?s+"s":"");
	}
	
	
	public static String dumpHourDate(long t) {
		return DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.ofInstant(Instant.ofEpochMilli(t-t%1000), ZoneId.systemDefault()));
	}
	
	
	public static <T>  Set<T> union(Collection<T> c1, Collection<T> c2){
		Set<T> out = c1.stream().collect(Collectors.toSet());
		out.addAll(c2);
		return out;
	}
	
	public static boolean isSubWord(String word, String subword) {
		int i=0,j=0;
		while(true) {
			if(i==subword.length())return true;
			if(j==word.length())return false;
			if(subword.charAt(i)==word.charAt(j))i++;
			j++;
		}
	}
	
	public static boolean and(boolean[] bb) {
		for(boolean b : bb)
			if(!b)return false;
		return true;
	}
	
	
	public static Set<String> mappingKeys(YamlMapping mapping) throws IOException{
		return mapping.keys().stream().map(n ->n.asScalar().value()).collect(Collectors.toSet());
	}
	
	
	public static Stream<YamlNode> sequenceStream(YamlSequence sequence){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(sequence.iterator(),Spliterator.ORDERED),false);
	}
	
	public static <T> Function<YamlNode,T> wetherMapping(Function<YamlNode,T> fnot,Function<YamlMapping,T> fyes){
		return n -> (n instanceof YamlMapping)?fyes.apply((YamlMapping)n):fnot.apply(n);
	}
	
	public static Color randColor() {
		return Color.getHSBColor((float) Math.random(), 1.0f, 1.0f);
	}
	public static Color randDarkColor() {
		return Color.getHSBColor((float) Math.random(), 1.0f, 0.3f);
	}
	public static Color randDarkBlueColor() {
		// 180° to 240°
		return Color.getHSBColor( (float) (0.5f + 0.2f*Math.random()), 1.0f, 0.3f);
	}
	public static Color getContrastColor(Color color) {
		  double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
		  return y >= 128 ? Color.black : Color.white;
	}
	
	
	public static final <M> YamlMapping setToMapSS(Set<M> nodes, Function<M,String> key, Function<M,String> value) {
		YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
		for(M n : nodes)
			builder = builder.add(key.apply(n), value.apply(n));
		return builder.build();
	}
	public static final <M> YamlMapping setToMapSY(Set<M> nodes, Function<M,String> key, Function<M,YamlNode> value) {
		YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
		for(M n : nodes)
			builder = builder.add(key.apply(n), value.apply(n));
		return builder.build();
	}
	public static final <M> YamlMapping setToMapYS(Set<M> nodes, Function<M,YamlNode> key, Function<M,String> value) {
		YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
		for(M n : nodes)
			builder = builder.add(key.apply(n), value.apply(n));
		return builder.build();
	}
	public static final <M> YamlMapping setToMapYY(Set<M> nodes, Function<M,YamlNode> key, Function<M,YamlNode> value) {
		YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
		for(M n : nodes)
			builder = builder.add(key.apply(n), value.apply(n));
		return builder.build();
	}
	public static final <M> YamlSequence setToSeqY(Set<YamlNode> nodes) {
		YamlSequenceBuilder builder = Yaml.createYamlSequenceBuilder();
		for(YamlNode n : nodes)
			builder = builder.add(n);
		return builder.build();
	}
	public static final <M> YamlSequence setToSeqS(Set<String> nodes) {
		YamlSequenceBuilder builder = Yaml.createYamlSequenceBuilder();
		for(String n : nodes)
			builder = builder.add(n);
		return builder.build();
	}
	
}
