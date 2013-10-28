package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.Map;

/**
 * The ISimilarityMeasure represents an interface for all similarity measures
 * that will be used in this data pipeline
 * 
 * @author jgee1
 * 
 */
public interface ISimilarityMeasure {

	/**
	 * Calculate the similarity between two vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public double calculateSimilarity(Map<String, Integer> vector1,
			Map<String, Integer> vector2);
}
