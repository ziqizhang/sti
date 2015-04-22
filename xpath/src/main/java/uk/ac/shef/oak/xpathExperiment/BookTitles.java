package uk.ac.shef.oak.xpathExperiment;

import java.util.HashSet;
import java.util.Set;

public class BookTitles {
	

	public BookTitles(String pathToFile) {
		Gazeteer fileExt = new Gazeteer(pathToFile);
		
		this.bookTitles = fileExt.getWords();
		
//		for (String s :fileExt.getWords()){
//			System.out.print(s+"|");
//		}
	}




	public Set<String> getBookTitles(){
		return this.bookTitles;
	}

	private Set<String> bookTitles = new HashSet<String>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BookTitles bt = new BookTitles("./gazeteers/book_titles.txt");
		System.out.println(bt.bookTitles);
	}

}
