package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BaselineParser;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.TreeAnnotations;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.CounterMap;


public class GenerativeParserFactory implements ParserFactory {
	
	public Parser getParser(List<Tree<String>> trainTrees) {
	      return new GenerativeParser(trainTrees);
	}
}
