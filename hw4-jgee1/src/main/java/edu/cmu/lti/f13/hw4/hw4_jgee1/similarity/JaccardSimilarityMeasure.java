package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JaccardSimilarityMeasure implements ISimilarityMeasure{

  @Override
  public double calculateSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
    
    Set<String> vector1Terms=vector1.keySet();
    Set<String> vector2Terms=vector2.keySet();
    
    Set<String> intersection=new HashSet<String>(vector1Terms);
    intersection.retainAll(vector2Terms);
 
    Set<String> union=new HashSet<String>(vector1Terms);
    union.addAll(vector2Terms);
  
    double jaccardCoefficient= (double)intersection.size()/union.size();
    return jaccardCoefficient;
  }

}
