package uk.ac.shef.oak.gazetteers;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.shef.oak.xpathExperiment.BookTitles;

public class TextOperations {

	private String matchesPattern(Pattern p, String sentence) {
		Matcher m = p.matcher(sentence);

		if (m.find()) {
			return m.group();
		}
		return null;
	}

	private Set<String> cleanBookGazeteer(String gaz) {
		Pattern alphaNum = Pattern.compile("[a-zA-Z0-9]");
		BookTitles bt = new BookTitles(gaz);
		Set<String> tokens = new HashSet<String>();
		// remove single word annotatons
		for (String t : bt.getBookTitles()) {
			if (this.matchesPattern(alphaNum, t) != null
					&& (t.split(" ").length > 1)) {
				tokens.add(t.trim());
			}
		}
		return tokens;
	}

	
	public static String normalizeString(String r){
		r = StringEscapeUtils.unescapeHtml(r);
//		r = r.trim().toLowerCase().replaceAll("( )+", " ");
		r = r.replaceAll("\\u00A0", " ");		
		r = r.trim().toLowerCase().replaceAll("\\s+", " ");		
		return r;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("unescape Weight: 245&nbsp; ["+ StringEscapeUtils.unescapeHtml("Weight: 245&nbsp;") +"]");
		System.out.println("normalize &Weight: 245&nbsp; ["+ normalizeString("Weight: 245&nbsp;") +"]");
		
		
	}

}
