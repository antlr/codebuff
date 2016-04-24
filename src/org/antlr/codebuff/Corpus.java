package org.antlr.codebuff;

import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.codebuff.walkers.CollectSiblingLists;
import org.antlr.codebuff.walkers.CollectTokenDependencies;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.getLexer;
import static org.antlr.codebuff.Tool.getParser;
import static org.antlr.codebuff.Tool.load;
import static org.antlr.codebuff.Tool.parse;
import static org.antlr.codebuff.Tool.showFileNames;

public class Corpus {
	public static final int FEATURE_VECTOR_RANDOM_SEED = 314159; // need randomness but use same seed to get reproducibility

	public static final int NUM_DEPENDENT_VARS = 2;
	public static final int INDEX_FEATURE_NEWLINES = 0;
	public static final int INDEX_FEATURE_ALIGN_WITH_PREVIOUS = 1;

	List<InputDocument> documents; // A list of all input docs to train on

	List<InputDocument> documentsPerExemplar; // an entry for each featureVector
	List<int[]> featureVectors;
	List<Integer> injectWhitespace;
	List<Integer> align;

	public String rootDir;
	public String fileRegex;
	public LangDescriptor language;

	/** an index to narrow down the number of vectors we compute distance() on each classification.
	 *  The key is (previous token's rule index, current token's rule index). It yields
	 *  a list of vectors with same key. Created by {@link #buildTokenContextIndex}.
	 */
	public Map<Pair<Integer,Integer>, List<Integer>> curAndPrevTokenRuleIndexToVectorsMap;

	public Map<String, List<Pair<Integer, Integer>>> ruleToPairsBag = null;
	public Map<ParentSiblingListKey, SiblingListStats> rootAndChildListStats;
	public Map<ParentSiblingListKey, SiblingListStats> rootAndSplitChildListStats;
	public Map<ParentSiblingListKey, Integer> splitListForms;
	public Map<Token, Pair<Boolean, Integer>> tokenToListInfo;

	public Corpus(String rootDir, String fileRegex, LangDescriptor language) {
		this.rootDir = rootDir;
		this.fileRegex = fileRegex;
		this.language = language;
	}

	public Corpus(List<InputDocument> documents,
	              List<int[]> featureVectors,
	              List<Integer> injectWhitespace,
	              List<Integer> align)
	{
		this.documents = documents;
		this.featureVectors = featureVectors;
		this.injectWhitespace = injectWhitespace;
		this.align = align;
	}

	public void train() throws Exception { train(true); }

	public void train(boolean shuffleFeatureVectors) throws Exception {
		List<String> allFiles = getFilenames(new File(rootDir), fileRegex);
		documents = load(allFiles, language.tabSize);

		// Parse all documents into parse trees before training begins
		for (InputDocument doc : documents) {
			if ( showFileNames ) System.out.println(doc);
			parse(doc, language);
		}

		collectTokenPairsAndSplitListInfo();

		processSampleDocs();

		if ( shuffleFeatureVectors ) randomShuffleInPlace();

		buildTokenContextIndex();
	}

	/** Walk all documents to compute matching token dependencies (we need this for feature computation)
	 *  While we're at it, find sibling lists.
	 */
	public void collectTokenPairsAndSplitListInfo() throws NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
		Vocabulary vocab = getLexer(language.lexerClass, null).getVocabulary();
		String[] ruleNames = getParser(language.parserClass, null).getRuleNames();
		CollectTokenDependencies collectTokenDependencies = new CollectTokenDependencies(vocab, ruleNames);
		CollectSiblingLists collectSiblingLists = new CollectSiblingLists();
		for (InputDocument doc : documents) {
			collectSiblingLists.setTokens(doc.tokens, doc.tree);
			ParseTreeWalker.DEFAULT.walk(collectTokenDependencies, doc.tree);
			ParseTreeWalker.DEFAULT.walk(collectSiblingLists, doc.tree);
		}
		ruleToPairsBag = collectTokenDependencies.getDependencies();
		rootAndChildListStats = collectSiblingLists.getListStats();
		rootAndSplitChildListStats = collectSiblingLists.getSplitListStats();
		splitListForms = collectSiblingLists.getSplitListForms();
		tokenToListInfo = collectSiblingLists.getTokenToListInfo();

		if ( false ) {
			for (String ruleName : ruleToPairsBag.keySet()) {
				List<Pair<Integer, Integer>> pairs = ruleToPairsBag.get(ruleName);
				System.out.print(ruleName+": ");
				for (Pair<Integer, Integer> p : pairs) {
					System.out.print(vocab.getDisplayName(p.a)+","+vocab.getDisplayName(p.b)+" ");
				}
				System.out.println();
			}
		}

		if ( false ) {
			for (ParentSiblingListKey siblingPairs : rootAndChildListStats.keySet()) {
				String parent = ruleNames[siblingPairs.parentRuleIndex];
				parent = parent.replace("Context","");
				String siblingListName = ruleNames[siblingPairs.childRuleIndex];
				siblingListName = siblingListName.replace("Context","");
				System.out.println(parent+":"+siblingPairs.parentRuleAlt+"->"+siblingListName+":"+siblingPairs.childRuleAlt+
					                   " (min,median,var,max)="+rootAndChildListStats.get(siblingPairs));
			}
			for (ParentSiblingListKey siblingPairs : rootAndSplitChildListStats.keySet()) {
				String parent = ruleNames[siblingPairs.parentRuleIndex];
				parent = parent.replace("Context","");
				String siblingListName = ruleNames[siblingPairs.childRuleIndex];
				siblingListName = siblingListName.replace("Context","");
				System.out.println("SPLIT " +parent+":"+siblingPairs.parentRuleAlt+"->"+siblingListName+":"+siblingPairs.childRuleAlt+
					                   " (min,median,var,max)="+rootAndSplitChildListStats.get(siblingPairs)+
				                  " form "+splitListForms.get(siblingPairs));
			}
		}
	}

	public void processSampleDocs() throws Exception {
		documentsPerExemplar = new ArrayList<>();
		featureVectors = new ArrayList<>();
		injectWhitespace = new ArrayList<>();
		align = new ArrayList<>();

		for (InputDocument doc : documents) {
			if ( showFileNames ) System.out.println(doc);
			process(doc);

			// TODO: use addAll etc... instead

			featureVectors.addAll(doc.featureVectors);
			injectWhitespace.addAll(doc.injectWhitespace);
			align.addAll(doc.align);

			// replicate doc ptr for each feature vector we just added
			for (int i=0; i<doc.featureVectors.size(); i++) {
				documentsPerExemplar.add(doc);
			}
		}
		System.out.printf("%d feature vectors\n", featureVectors.size());
	}

	/** Parse document, save feature vectors to the doc */
	public void process(InputDocument doc) {
		Trainer trainer = new Trainer(this, doc);
		trainer.computeFeatureVectors();

		doc.featureVectors = trainer.getFeatureVectors();
		doc.injectWhitespace = trainer.getInjectWhitespace();
		doc.align = trainer.getAlign();
	}

	/** Feature vectors in X are lumped together as they are read in each
	 *  document. In kNN, this tends to find features from the same document
	 *  rather than from across the corpus since we grab k neighbors.
	 *  For k=11, we might only see exemplars from a single corpus document.
	 *  If all exemplars fit in k, this wouldn't be an issue.
	 *
	 *  Fisher-Yates / Knuth shuffling
	 *  "To shuffle an array a of n elements (indices 0..n-1)":
	 *  https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 */
	public void randomShuffleInPlace() {
		Random r = new Random();
		r.setSeed(FEATURE_VECTOR_RANDOM_SEED);
		// for i from n−1 downto 1 do
		int n = featureVectors.size();
		for (int i=n-1; i>=1; i--) {
			// j ← random integer such that 0 ≤ j ≤ i
			int j = r.nextInt(i+1);
			// exchange a[j] and a[i]
			// Swap X
			int[] tmp = featureVectors.get(i);
			featureVectors.set(i, featureVectors.get(j));
			featureVectors.set(j, tmp);
			// And now swap all prediction lists
			Integer tmpI = injectWhitespace.get(i);
			injectWhitespace.set(i, injectWhitespace.get(j));
			injectWhitespace.set(j, tmpI);
			tmpI = align.get(i);
			align.set(i, align.get(j));
			align.set(j, tmpI);
			// Finally, swap documents
			InputDocument tmpD = documentsPerExemplar.get(i);
			documentsPerExemplar.set(i, documentsPerExemplar.get(j));
			documentsPerExemplar.set(j, tmpD);
		}
	}

	public void buildTokenContextIndex() {
		curAndPrevTokenRuleIndexToVectorsMap = new HashMap<>();
		for (int i = 0; i<featureVectors.size(); i++) {
			int curTokenRuleIndex = featureVectors.get(i)[Trainer.INDEX_PREV_EARLIEST_RIGHT_ANCESTOR];
			int prevTokenRuleIndex = featureVectors.get(i)[Trainer.INDEX_EARLIEST_LEFT_ANCESTOR];
			int pr = Trainer.unrulealt(prevTokenRuleIndex)[0];
			int cr = Trainer.unrulealt(curTokenRuleIndex)[0];
			Pair<Integer, Integer> key = new Pair<>(pr, cr);
			List<Integer> vectorIndexes = curAndPrevTokenRuleIndexToVectorsMap.get(key);
			if ( vectorIndexes==null ) {
				vectorIndexes = new ArrayList<>();
				curAndPrevTokenRuleIndexToVectorsMap.put(key, vectorIndexes);
			}
			vectorIndexes.add(i);
		}
	}
}
