package edu.cmu.lti.f13.hw4.hw4_jgee1.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_jgee1.similarity.ISimilarityMeasure;
import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_jgee1.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

  
  public String SIMILARITY_MEASURE="edu.cmu.lti.f13.hw4.hw4_jgee1.similarity.DiceCoefficientSimilarityMeasure";
  
	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	/** token list **/
	public ArrayList<Map<String, Integer>> tokensList;
	
	

	public Map<Integer, Integer> queryIndices;
	public Map<Integer, List<Integer>> answerIndices;

		
	public void initialize() throws ResourceInitializationException {

		this.qIdList = new ArrayList<Integer>();

		this.relList = new ArrayList<Integer>();
		
		this.tokensList=new ArrayList<Map<String, Integer>>();
		
		this.queryIndices=new HashMap<Integer,Integer>();
		
		this.answerIndices=new HashMap<Integer, List<Integer>>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			Map<String, Integer> docVector=new HashMap<String, Integer>();
			for(Token token:tokenList)
			{
			  docVector.put(token.getText(), token.getFrequency());
			}
			
			this.tokensList.add(docVector);
			
			int qID=doc.getQueryID();
			int relevance=doc.getRelevanceValue();
			
		
//			this.rawText.add(doc.getText());
			qIdList.add(qID);
			relList.add(relevance);
			//Do something useful here
			
			if(relevance==99)
			{
			  this.queryIndices.put(qID, qIdList.size()-1);
			}
			else
			{
			  if(!this.answerIndices.containsKey(qID))
			  {
			    this.answerIndices.put(qID, new ArrayList<Integer>());
			  }
			  this.answerIndices.get(qID).add(qIdList.size()-1);
			}
			
		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		List<Integer> ranks=new ArrayList<Integer>();
		
		
	  ISimilarityMeasure similarityMeasure = null;
		
		try {
      similarityMeasure=(ISimilarityMeasure) Class.forName(SIMILARITY_MEASURE).newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
		
		for(Entry<Integer, Integer> query:this.queryIndices.entrySet())
		{
		  System.out.println(this.tokensList.get(query.getValue()));
		   double goldStandardScore=0D;
		   int goldStandardIndex=0;
		  
		   TreeMap<Double, Integer> answerScores=new TreeMap<Double, Integer>();
		   
		   List<Integer> answerIndices=this.answerIndices.get(query.getKey());
		   
		   
		   
		   
		   for(Integer index: answerIndices)
		   {
		     
		     double answerScore=similarityMeasure.calculateSimilarity(this.tokensList.get(query.getValue()), this.tokensList.get(index));
		     System.out.println(this.tokensList.get(index)+"===="+answerScore);
		     answerScores.put(answerScore, index);
		     if(this.relList.get(index)==1)
         {
           goldStandardScore=answerScore;
           goldStandardIndex=index;
         }
		   }
		   
		   
		   int goldStandardRank=answerScores.size()-answerScores.subMap(0D, goldStandardScore).size();
		   ranks.add(goldStandardRank);
		   System.out.println("Score: "+goldStandardScore+"\trank="+goldStandardRank+"\trel="+this.relList.get(goldStandardIndex)+" qid="+this.qIdList.get(goldStandardIndex)+" sent"+(goldStandardIndex+1));
		}
		
		
		
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr(ranks);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	
	
	
	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		
		double dotProduct=0.0;
		for(String term:queryVector.keySet())
		{
		  if(docVector.containsKey(term))
		  {
		    dotProduct+=queryVector.get(term)*docVector.get(term);
		  }
		}
		
		double sumSquareQuery=0.0;
		for(Integer freqQuery:queryVector.values())
		{
		  sumSquareQuery+=Math.pow(freqQuery,2);
		}

	  double sumSquareDoc=0.0;
    for(Integer freqDoc:docVector.values())
    {
      sumSquareDoc+=Math.pow(freqDoc,2);
    }
		
    cosine_similarity=dotProduct/(Math.sqrt(sumSquareQuery)*Math.sqrt(sumSquareDoc));
    
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr(List<Integer> ranks) {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		double sum=0D;
		
		for(Integer rank: ranks)
		{
		  sum+=(1D/rank);
		}
		System.out.println(sum);
		metric_mrr=sum/ranks.size();
		return metric_mrr;
	}

}
