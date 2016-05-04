package org.antlr.codebuff.walkers;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.VisitSiblingLists;
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

import static org.antlr.codebuff.Formatter.WIDE_LIST_THRESHOLD;

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
		tokenToListInfo.putAll(tokenInfo);
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

		int len = Trainer.getSiblingsLength(siblings);
		if ( stats!=null && splitStats==null && len>=WIDE_LIST_THRESHOLD ) {
			oversize = true; // fail-safe if we have never seen an oversize list for this pair in corpus
		}
		else {
			if ( stats!=null&&splitStats!=null ) {
				// compare distance in units of standard deviations to regular or split means
				// like a one-dimensional Mahalanobis distance
				double d1 = Math.abs(splitStats.median-len) / Math.sqrt(splitStats.variance);
				double d2 = Math.abs(stats.median-len) / Math.sqrt(stats.variance);
				if ( d1<d2 ) {
					oversize = true;
				}
			}
		}
		return oversize;
	}
}
