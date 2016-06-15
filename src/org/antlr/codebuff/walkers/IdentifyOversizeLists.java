package org.antlr.codebuff.walkers;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [USED IN FORMATTING ONLY]
 *  Walk tree and find and fill tokenToListInfo with all oversize lists with separators.
 */
public class IdentifyOversizeLists extends VisitSiblingLists {
	Corpus corpus;
	CodeBuffTokenStream tokens;

	/** Map token to ("is oversize", element type). Used to compute feature vector. */
	public Map<Token,Pair<Boolean,Integer>> tokenToListInfo = new HashMap<>();

	public Map<Token, TerminalNode> tokenToNodeMap;

	public IdentifyOversizeLists(Corpus corpus,
	                             CodeBuffTokenStream tokens,
	                             Map<Token, TerminalNode> tokenToNodeMap)
	{
		this.corpus = corpus;
		this.tokens = tokens;
		this.tokenToNodeMap = tokenToNodeMap;
	}

	public void visitNonSingletonWithSeparator(ParserRuleContext ctx,
	                                           List<? extends ParserRuleContext> siblings,
	                                           Token separator)
	{
		boolean oversize = isOversizeList(ctx, siblings, separator);
		Map<Token, Pair<Boolean, Integer>> tokenInfo =
			getInfoAboutListTokens(ctx, tokens, tokenToNodeMap, siblings, oversize);

		// copy sibling list info for associated tokens into overall list
		// but don't overwrite existing so that most general (largest construct)
		// list information is use/retained (i.e., not overwritten).
		for (Token t : tokenInfo.keySet()) {
			if ( !tokenToListInfo.containsKey(t) ) {
				tokenToListInfo.put(t, tokenInfo.get(t));
			}
		}
	}

	/** Return true if we've only seen parent-sibling-separator combo as a split list.
	 *  Return true if we've seen that combo as both list and split list AND
	 *  len of all siblings is closer to split median than to regular nonsplit median.
	 */
	public boolean isOversizeList(ParserRuleContext ctx,
	                              List<? extends ParserRuleContext> siblings,
	                              Token separator)
	{
		ParserRuleContext first = siblings.get(0);
		ParentSiblingListKey pair = new ParentSiblingListKey(ctx, first, separator.getType());
		SiblingListStats stats = corpus.rootAndChildListStats.get(pair);
		SiblingListStats splitStats = corpus.rootAndSplitChildListStats.get(pair);
		boolean oversize = stats==null && splitStats!=null;

		if ( stats!=null && splitStats==null ) {
			// note: if we've never seen a split version of this ctx, do nothing;
			// I used to have oversize failsafe
		}

		int len = Trainer.getSiblingsLength(siblings);
		if ( stats!=null&&splitStats!=null ) {
			// compare distance in units of standard deviations to regular or split means
			// like a one-dimensional Mahalanobis distance.
			// actually i took out the stddev divisor. they are usually very spread out and overlapping.
			double distToSplit = Math.abs(splitStats.median-len);
			double distToSplitSquared = Math.pow(distToSplit,2);
			double distToSplitStddevUnits = distToSplitSquared / Math.sqrt(splitStats.variance);

			double distToRegular = Math.abs(stats.median-len);
			double distToRegularSquared = Math.pow(distToRegular,2);
			double distToRegularStddevUnits = distToRegularSquared / Math.sqrt(stats.variance);

			// consider a priori probabilities as well.
			float n = splitStats.numSamples+stats.numSamples;
			float probSplit = splitStats.numSamples/n;
			float probRegular = stats.numSamples/n;
			double adjDistToSplit   = distToSplitSquared   * (1 - probSplit);   // make distance smaller if probSplit is high
			double adjDistToRegular = distToRegularSquared * (1 - probRegular);
			if ( adjDistToSplit<adjDistToRegular ) {
				oversize = true;
			}
		}
		return oversize;
	}
}
