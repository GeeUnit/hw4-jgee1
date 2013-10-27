package edu.cmu.lti.f13.hw4.hw4_jgee1.annotators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_jgee1.utils.Utils;


public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  private Set<String> stopwords;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // load stopword list
    this.stopwords = new HashSet<String>();

    URL stopwordLocation = DocumentVectorAnnotator.class.getResource("/stopwords.txt");

    if (stopwordLocation == null) {
      System.err.println("Could not find a stopword list. Proceeding without one.");
    } else {
      BufferedReader br;
      try {
        br = new BufferedReader(new InputStreamReader(stopwordLocation.openStream()));
        String newLine = "";

        while ((newLine = br.readLine()) != null) {
          if (newLine.startsWith("#")) {
            continue;
          }
          stopwords.add(newLine.trim());
        }
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    }

  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      createTermFreqVector(jcas, doc);
    }

  }

  /**
   * 
   * @param jcas
   * @param doc
   */

  private void createTermFreqVector(JCas jcas, Document doc) {

    String docText = doc.getText().toLowerCase();
    String[] whiteSpaceTokens=docText.split("[\\W&&[^'-]]");
  
    Map<String,Integer> counts=new HashMap<String,Integer>();
    
    for(String w:whiteSpaceTokens)
    {
      if(w.trim().length()<1 || this.stopwords.contains(w))
      {
        continue;
      }
      if(counts.containsKey(w))
      {
        counts.put(w, counts.get(w)+1);
      }
      else
      {
        counts.put(w, 1);
      }  
    }
    
    List<Token> tokenList=new ArrayList<Token>();
    for(Entry<String,Integer> termFreq: counts.entrySet())
    {
      Token token=new Token(jcas);
      token.setText(termFreq.getKey());
      token.setFrequency(termFreq.getValue());
      tokenList.add(token);
    }
    doc.setTokenList(Utils.fromCollectionToFSList(jcas, tokenList));
    
    doc.addToIndexes();
  }
}
