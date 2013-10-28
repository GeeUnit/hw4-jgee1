package edu.cmu.lti.f13.hw4.hw4_jgee1.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_jgee1.similarity.ISimilarityMeasure;
import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_jgee1.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_jgee1.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/**
	 * Relevance to denote query
	 */
	private static final int QUERY_REL = 99;

	/**
	 * Relevance to denote correct answer
	 */
	private static final int CORRECT_REL = 1;

	/**
	 * Similarity classname. All similarity implementations can be found in the
	 * edu.cmu.lti.f13.hw4.hw4_jgee1.similarity package.
	 */
	public String SIMILARITY_MEASURE = "edu.cmu.lti.f13.hw4.hw4_jgee1.similarity.EuclideanDistanceSimilarityMeasure";

	/**
	 * query id number
	 */
	public ArrayList<Integer> qIdList;

	/**
	 * query and text relevant values
	 */
	public ArrayList<Integer> relList;

	/**
	 * token list
	 */
	public ArrayList<Map<String, Integer>> tokensList;

	/**
	 * pointers to sentences that are queries for a given qID
	 */
	public Map<Integer, Integer> queryIndices;

	/**
	 * pointers to sentences that are answers for a given qID
	 */
	public Map<Integer, List<Integer>> answerIndices;

	/**
	 * pointers to sentences that are the goldstandard answers for a given qID
	 */
	public Map<Integer, List<Integer>> goldAnswerIndices;

	public void initialize() throws ResourceInitializationException {

		// initialize all the lists
		this.qIdList = new ArrayList<Integer>();
		this.relList = new ArrayList<Integer>();
		this.tokensList = new ArrayList<Map<String, Integer>>();
		this.queryIndices = new HashMap<Integer, Integer>();
		this.answerIndices = new HashMap<Integer, List<Integer>>();
		this.goldAnswerIndices = new HashMap<Integer, List<Integer>>();

	}

	/**
	 * Creates document vectors for each of the input sentences.
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator<Annotation> it = jcas.getAnnotationIndex(Document.type)
				.iterator();

		if (it.hasNext()) {
			Document doc = (Document) it.next();

			// Make sure that your previous annotators have populated this in
			// CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(
					fsTokenList, Token.class);

			// Convert each tokenList object back into a map-based BOW vector
			Map<String, Integer> docVector = new HashMap<String, Integer>();
			for (Token token : tokenList) {
				docVector.put(token.getText(), token.getFrequency());
			}

			/**
			 * each document should share the same index across each of the
			 * lists
			 **/

			// Add each vector to a list
			this.tokensList.add(docVector);

			// Add each qID to a list
			int qID = doc.getQueryID();
			this.qIdList.add(qID);

			// Add each relevance to a list
			int relevance = doc.getRelevanceValue();
			this.relList.add(relevance);

			// Check to see if the index is the query. If so, store a special
			// pointer to its index in the query list.
			if (relevance == QUERY_REL) {
				this.queryIndices.put(qID, qIdList.size() - 1);
			} else if (relevance == CORRECT_REL) {
				// If it is the correct answer to the query, store a special
				// pointer to the index.
				if (!this.goldAnswerIndices.containsKey(qID)) {
					this.goldAnswerIndices.put(qID, new ArrayList<Integer>());
				}
				this.goldAnswerIndices.get(qID).add(qIdList.size() - 1);
			} else {

				// If not a query or the correct answer to one, store a pointer
				// to the index in the answers list.
				if (!this.answerIndices.containsKey(qID)) {
					this.answerIndices.put(qID, new ArrayList<Integer>());
				}
				this.answerIndices.get(qID).add(qIdList.size() - 1);
			}

		}

	}

	/**
	 * Computes the Cosine Similarity for each query-answer pair, and computes
	 * the MRR metric at the end.
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// Store the ranks for all the correct answers.
		List<Integer> ranks = new ArrayList<Integer>();

		// Create an instance of the similarity measure.
		ISimilarityMeasure similarityMeasure = null;
		try {
			similarityMeasure = (ISimilarityMeasure) Class.forName(
					SIMILARITY_MEASURE).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Begin scoring all answers against the corresponding query.
		for (Entry<Integer, Integer> query : this.queryIndices.entrySet()) {

			// These values store the score and index of the highest-scored
			// correct answer.
			double goldStandardScore = -1D;
			int goldStandardIndex = 0;

			// Retrieve all correct answers for this query
			List<Integer> goldAnswerIndices = this.goldAnswerIndices.get(query
					.getKey());

			for (Integer index : goldAnswerIndices) {

				double answerScore = similarityMeasure.calculateSimilarity(
						this.tokensList.get(query.getValue()),
						this.tokensList.get(index));

				// Store the best scoring correct answer for the MRR calculation
				if (answerScore > goldStandardScore) {
					goldStandardScore = answerScore;
					goldStandardIndex = index;
				}
			}

			// Set gold standard rank to the highest
			int goldStandardRank = 1;

			// Get a list of wrong answers for the given query.
			List<Integer> answerIndices = this.answerIndices
					.get(query.getKey());

			double highScore = 0D;

			for (Integer index : answerIndices) {

				// Compute an similarity score for each answer against the
				// query;
				double answerScore = similarityMeasure.calculateSimilarity(
						this.tokensList.get(query.getValue()),
						this.tokensList.get(index));

				if (answerScore > highScore) {
					highScore = answerScore;
				}
				// If a wrong answer has a better score, lower the rank
				if (answerScore > goldStandardScore) {
					goldStandardRank++;
				}
			}

			ranks.add(goldStandardRank);
			System.out.println("Score: " + goldStandardScore + "\trank="
					+ goldStandardRank + "\trel="
					+ this.relList.get(goldStandardIndex) + " qid="
					+ this.qIdList.get(goldStandardIndex) + " sent"
					+ (goldStandardIndex + 1));
		}
		// Compute MRR
		double metric_mrr = compute_mrr(ranks);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * Calculate Mean Reciprocal Rank for a list of ranks
	 * 
	 * @return mrr
	 */
	private double compute_mrr(List<Integer> ranks) {
		double metric_mrr = 0.0;
		double sum = 0D;

		// Sum reciprocal ranks for each query
		for (Integer rank : ranks) {
			sum += (1D / rank);
		}
		// Average the reciprocals.
		metric_mrr = sum / ranks.size();
		return metric_mrr;
	}

}
