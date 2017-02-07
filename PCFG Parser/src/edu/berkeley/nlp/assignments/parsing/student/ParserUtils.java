package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;

public class ParserUtils {
	  public static List<String> getBaselineTagging(List<String> sentence, SimpleLexicon lexicon) {
	    List<String> tags = new ArrayList<String>();
	    for (String word : sentence) {
	      String tag = getBestTag(word, lexicon);
	      tags.add(tag);
	    }
	    return tags;
	  }

	  private static String getBestTag(String word, SimpleLexicon lexicon) {
	    double bestScore = Double.NEGATIVE_INFINITY;
	    String bestTag = null;
	    for (String tag : lexicon.getAllTags()) {
	      double score = lexicon.scoreTagging(word, tag);
	      if (bestTag == null || score > bestScore) {
	        bestScore = score;
	        bestTag = tag;
	      }
	    }
	    return bestTag;
	  }
	  
	  public static double getScoreTag(String word, String tag, SimpleLexicon lexicon){
		  if(lexicon.getAllTags().contains(tag)){
			  return lexicon.scoreTagging(word, tag);
		  }
		  return Double.NEGATIVE_INFINITY;
	  }
}
