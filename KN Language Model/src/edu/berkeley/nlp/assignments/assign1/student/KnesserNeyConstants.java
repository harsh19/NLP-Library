package edu.berkeley.nlp.assignments.assign1.student;

public class KnesserNeyConstants {
	public static final short BIGRAM_COUNT_TYPE = 1;
	
	public static final double D3 = 0.75;
	public static final double D2 = 0.75;
	
	public static final double DEFAULT_PROBABILITY_VALUE = 0.00005;
	public static final double DEFAULT_LOG_PROBABILITY_VALUE = Math.log(DEFAULT_PROBABILITY_VALUE);
	public static final int TRIGRAM_PROBABILITY_CACHE_SIZE = 14000000;
	public static final boolean IGNORE_SMALL_COUNTS = false;
	public static final double LOAD_FACTOR = 0.60;
}
