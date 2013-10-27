package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.Map;

public class TanimotoDistanceMeasure implements ISimilarityMeasure{

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
    
    double sumQuery=0.0;
    for(Integer freqQuery:vector1.values())
    {
      sumQuery+=freqQuery;
    }

    double sumDoc=0.0;
    for(Integer freqDoc:vector2.values())
    {
      sumDoc+=freqDoc;
    }
    
    double tanimoto_distance=dotProduct/(sumQuery+sumDoc-dotProduct);
   
    return tanimoto_distance;
  }

}
