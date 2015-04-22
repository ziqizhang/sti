package uk.ac.shef.oak.xpath.collectiveExperiment;

import java.util.Set;
import java.util.TreeSet;

public class SetOperations {

	

	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		if (setA==null||setB==null)
			return null;
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}

	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA)
			tmp.add(x);
		for (T x : setB)
			tmp.add(x);
		return tmp;
	}

	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA) {
			if (!setB.contains(x))
				tmp.add(x);
		}
		return tmp;
	}

	// end utility methods
	
}
