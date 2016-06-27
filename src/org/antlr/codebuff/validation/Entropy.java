package org.antlr.codebuff.validation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.HashBag;
import org.antlr.codebuff.misc.LangDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.log;
import static org.antlr.codebuff.misc.BuffUtils.filter;
import static org.antlr.codebuff.misc.BuffUtils.mean;

/**

 Walk corpus of single file, grouping by feature vectors (formatting context).
 Then consider the diversity index / entropy of the predicted categories
 and diversity of contexts themselves.

 First goal: Measure how well the features capture the structure of the input.

 Assumption: ignore generality.

 If each specific context predicts many different categories and mostly at
 same likelihood, diversity/entropy is high and the predictor/formatter will likely
 perform poorly. In contrast, if each context predicts a single category,
 the model potentially captures the input structure well.  Call that
 context-category diversity.

 Low context-category diversity could be a result of model "getting lucky".
 Imagine an input file like Java input enum type with almost all one context
 and one category. Define context diversity as ratio of number of contexts
 over number of tokens.  If context-cat diversity is low AND context diversity
 is high, we could have more confidence the model was capturing the input.

 That only gives a do not exceed speed. Imagine a model whose context is
 just the token index. Context-category diversity would be 0 (or close to it)
 and diversity would be 1 as there'd be 1 context per token.  The model could
 reproduce the input perfectly but would not generalize in any way.

 Assuming we don't use a stupid model like "token index -> formatting directive",

 model capture index = (1.0 - context-category diversity) * context diversity

 gives useful index values for the files in a corpus.

 If context-category diversity is high, context diversity doesn't matter.
 A predictor cannot choose better than the entropy in the choices. This
 could be either or both of:

	a) model does not distinguish between contexts well enough
	b) corpus is highly inconsistent in formatting

 If we assume a single file is internally consistent (not always true;
 in principle, we could do divide up files into regions such as methods.) then
 high context-category diversity implies a bad model.

 Now consider context-category diversity across corpus files. High
 context-category diversity in one file but low overall context-category
 diversity in other files combined, implies that file is inconsistent
 with corpus.

 Even with low context-category diversity in one file and in corpus, they
 may differ in predicted category so must check.  If context-category diversity
 is low in file and in corpus and they are same, good consistency. If
 context-category diversity is low in both but not same, bad consistency.
 If context-category diversity is high in file but not corpus, file
 is internally inconsistent or with corpus or both. If context-category diversity
 is low in file, high in corpus, inconsistent.

 It just occurred to me that the number of categories predicted for a specific
 feature vector is like the diversity index in that we could sort by it.
 In this case, it favors diversity with lots of choices rather than relative
 likelihood.

 Important to point out: a perfect diversity of 0 for a file doesn't mean
 we can perfectly reproduce. The formatting directives may not be correct
 or rich enough. Low diversity just says we'll be able to pick what the
 formatting model thinks is correct.
 */
public class Entropy {

	public static void main(String[] args) throws Exception {
		runCaptureForOneLanguage(Tool.ANTLR4_DESCR);
	}

	public static void runCaptureForOneLanguage(LangDescriptor language) throws Exception {
		List<String> filenames = Tool.getFilenames(new File(language.corpusDir), language.fileRegex);
		List<InputDocument> documents = Tool.load(filenames, language);
		for (String fileName : filenames) {
			// Examine info for this file in isolation
			Corpus fileCorpus = new Corpus(fileName, language);
			fileCorpus.train();
			System.out.println(fileName);
//			examineCorpus(corpus);
			ListMultimap<FeatureVectorAsObject, Integer> ws = getWSContextCategoryMap(fileCorpus);
			ListMultimap<FeatureVectorAsObject, Integer> hpos = getHPosContextCategoryMap(fileCorpus);

			// Compare with corpus minus this file
			final String path = new File(fileName).getCanonicalPath();
			List<InputDocument> others = filter(documents, d -> !d.fileName.equals(path));
			Corpus corpus = new Corpus(others, language);
			corpus.train();
//			examineCorpus(corpus);
			ListMultimap<FeatureVectorAsObject, Integer> corpus_ws = getWSContextCategoryMap(corpus);
			ListMultimap<FeatureVectorAsObject, Integer> corpus_hpos = getHPosContextCategoryMap(corpus);

			for (FeatureVectorAsObject x : ws.keySet()) {
				HashBag<Integer> fwsCats = getCategoriesBag(ws.get(x));
				List<Float> fwsRatios = getCategoryRatios(fwsCats.values());
				HashBag<Integer> wsCats = getCategoriesBag(corpus_ws.get(x));
				List<Float> wsRatios = getCategoryRatios(wsCats.values());
				// compare file predictions with corpus predictions
				if ( !fwsRatios.equals(wsRatios) ) {
					System.out.println(fwsRatios+" vs "+wsRatios);
				}

				HashBag<Integer> fhposCats = getCategoriesBag(hpos.get(x));
				HashBag<Integer> hposCats = getCategoriesBag(corpus_hpos.get(x));
			}

			break;
		}
	}

	public static ListMultimap<FeatureVectorAsObject, Integer> getWSContextCategoryMap(Corpus corpus) {
		ListMultimap<FeatureVectorAsObject, Integer> wsByFeatureVectorGroup = ArrayListMultimap.create();
		int numContexts = corpus.featureVectors.size();
		for (int i = 0; i<numContexts; i++) {
			int[] X = corpus.featureVectors.get(i);
			int y = corpus.injectWhitespace.get(i);
			wsByFeatureVectorGroup.put(new FeatureVectorAsObject(X, Trainer.FEATURES_INJECT_WS), y);
		}

		return wsByFeatureVectorGroup;
	}

	public static ListMultimap<FeatureVectorAsObject, Integer> getHPosContextCategoryMap(Corpus corpus) {
		ListMultimap<FeatureVectorAsObject, Integer> hposByFeatureVectorGroup = ArrayListMultimap.create();
		int numContexts = corpus.featureVectors.size();
		for (int i = 0; i<numContexts; i++) {
			int[] X = corpus.featureVectors.get(i);
			int y = corpus.hpos.get(i);
			hposByFeatureVectorGroup.put(new FeatureVectorAsObject(X, Trainer.FEATURES_HPOS), y);
		}

		return hposByFeatureVectorGroup;
	}

	public static void examineCorpus(Corpus corpus) {
		ListMultimap<FeatureVectorAsObject, Integer> wsByFeatureVectorGroup = ArrayListMultimap.create();
		ListMultimap<FeatureVectorAsObject, Integer> hposByFeatureVectorGroup = ArrayListMultimap.create();
		int numContexts = corpus.featureVectors.size();
		for (int i = 0; i<numContexts; i++) {
			int[] X = corpus.featureVectors.get(i);
			int y1  = corpus.injectWhitespace.get(i);
			int y2  = corpus.hpos.get(i);
			wsByFeatureVectorGroup.put(new FeatureVectorAsObject(X, Trainer.FEATURES_INJECT_WS), y1);
			hposByFeatureVectorGroup.put(new FeatureVectorAsObject(X, Trainer.FEATURES_HPOS), y2);
		}
		List<Double> wsEntropies = new ArrayList<>();
		List<Double> hposEntropies = new ArrayList<>();
		for (FeatureVectorAsObject x : wsByFeatureVectorGroup.keySet()) {
			List<Integer> cats = wsByFeatureVectorGroup.get(x);
			List<Integer> cats2 = hposByFeatureVectorGroup.get(x);
			HashBag<Integer> wsCats = getCategoriesBag(cats);
			HashBag<Integer> hposCats = getCategoriesBag(cats2);
			double wsEntropy = getNormalizedCategoryEntropy(getCategoryRatios(wsCats.values()));
			double hposEntropy = getNormalizedCategoryEntropy(getCategoryRatios(hposCats.values()));
			wsEntropies.add(wsEntropy);
			hposEntropies.add(hposEntropy);
			System.out.printf("%130s : %s,%s %s,%s\n", x,
			                  wsCats, wsEntropy,
			                  hposCats, hposEntropy);
		}
		System.out.println("MEAN "+mean(wsEntropies));
		System.out.println("MEAN "+mean(hposEntropies));
		float contextRichness = wsEntropies.size()/(float) numContexts; // 0..1 where 1 means every token had different context
		System.out.println("Context richness = "+contextRichness+
			                   " uniq ctxs="+wsEntropies.size()+", nctxs="+numContexts);
	}

	/** Return diversity index, e^entropy.
	 *  https://en.wikipedia.org/wiki/Diversity_index
	 *  "Shannon entropy is the logarithm of 1D, the true diversity
	 *   index with parameter equal to 1."
	 */
	public static double getCategoryDiversityIndex(Collection<Float> ratios) {
		return Math.exp(getNormalizedCategoryEntropy(ratios));
	}

//	public static double getCategoryDiversityIndex(double entropy) {
//		return Math.exp(entropy);
//	}

	/** Return Shannon's diversity index (entropy) normalized to 0..1
	 *  https://en.wikipedia.org/wiki/Diversity_index
	 *  "Shannon entropy is the logarithm of 1D, the true diversity
	 *   index with parameter equal to 1."
	 *
	 *  "When all types in the dataset of interest are equally common,
	 *   all pi values equal 1 / R, and the Shannon index hence takes
	 *   the value ln(R)."
	 *
	 *   So, normalize to 0..1 by dividing by log(R). R here is the number
	 *   of different categories.
	 */
	public static double getNormalizedCategoryEntropy(Collection<Float> ratios) {
		double entropy = 0.0;
		int R = ratios.size();
		for (Float r : ratios) {
			entropy += r * log(r);
		}
		entropy = -entropy;
		return R==1 ? 0.0 : entropy / log(R);
//		return R==1 ? 0.0 : entropy;
	}

	public static List<Float> getCategoryRatios(Collection<Integer> catCounts) {
		List<Float> ratios = new ArrayList<>();
		int n = BuffUtils.sum(catCounts);
		for (Integer count : catCounts) {
			float probCat = count/(float) n;
			ratios.add(probCat);
		}
		return ratios;
	}

	public static HashBag<Integer> getCategoriesBag(Collection<Integer> categories) {
		HashBag<Integer> votes = new HashBag<>();
		for (Integer category : categories) {
			votes.add(category);
		}
		return votes;
	}
}
