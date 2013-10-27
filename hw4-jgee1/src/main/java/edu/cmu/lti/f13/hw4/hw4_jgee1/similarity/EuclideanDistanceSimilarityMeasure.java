package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EuclideanDistanceSimilarityMeasure implements ISimilarityMeasure{

  @Override
  public double calculateSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
    
    Set<String> termUnion=new HashSet<String>(vector1.keySet());
    termUnion.addAll(vector2.keySet());
    
    double sum=0D;
    
    for(String term: termUnion)
    {
      int vector1Value=vector1.containsKey(term)?vector1.get(term):0;
      int vector2Value=vector2.containsKey(term)?vector2.get(term):0;
      sum+=Math.pow((vector2Value-vector1Value), 2D);
    }
    
    double euclideanDistance=Math.sqrt(sum);
    
    return 1/(1+euclideanDistance);
  }

}
