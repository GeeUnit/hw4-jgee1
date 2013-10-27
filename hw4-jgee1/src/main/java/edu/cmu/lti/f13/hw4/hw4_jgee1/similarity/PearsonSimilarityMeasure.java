package edu.cmu.lti.f13.hw4.hw4_jgee1.similarity;

import java.util.Map;

public class PearsonSimilarityMeasure implements ISimilarityMeasure{

  @Override
  public double calculateSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
    
    int lengthV1=0;
    int lengthV2=0;
    
    for(int i:vector1.values())
    {
      lengthV1+=i;
    }   
    double v1mean=(double)lengthV1/vector1.values().size();
    
    for(int i:vector2.values())
    {
      lengthV2+=i;
    }
    double v2mean=(double)lengthV2/vector2.values().size();
    
    
    
    return 0D;
  }

}
