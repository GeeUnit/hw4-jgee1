package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.Map;

public class CosineSimilarityMeasure implements ISimilarityMeasure{

  @Override
  public double calculateSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
    double dotProduct=0.0;
    for(String term:vector1.keySet())
    {
      if(vector2.containsKey(term))
      {
        dotProduct+=vector1.get(term)*vector2.get(term);
      }
    }
    
    double sumSquareQuery=0.0;
    for(Integer freqQuery:vector1.values())
    {
      sumSquareQuery+=Math.pow(freqQuery,2);
    }

    double sumSquareDoc=0.0;
    for(Integer freqDoc:vector2.values())
    {
      sumSquareDoc+=Math.pow(freqDoc,2);
    }
    
    double cosine_similarity=dotProduct/(Math.sqrt(sumSquareQuery)*Math.sqrt(sumSquareDoc));
    
    return cosine_similarity;
  }

  
  
}
