package org.antlr.codebuff.walkers;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.JavaParser;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.VisitSiblingLists;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.Tree;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [USED IN FORMATTING ONLY]
 *  Walk tree and inject newlines where needed, before or after lists
 *  with separators that are oversized.
 */
public class SplitOversizeLists extends VisitSiblingLists {
	Corpus corpus;
	String[] injection;
	CodeBuffTokenStream tokens;

	/** Map token to ("is oversize", element type). Used to compute feature vector. */
	public Map<Token,Pair<Boolean,Integer>> tokenToListInfo = new HashMap<>();

	public SplitOversizeLists(Corpus corpus, CodeBuffTokenStream tokens, String[] injection) {
		this.corpus = corpus;
		this.tokens = tokens;
		this.injection = injection;
	}

	public void visitNonSingletonWithSeparator(ParserRuleContext ctx,
	                                           List<? extends ParserRuleContext> siblings,
	                                           Token separator)
	{
		ParserRuleContext first = siblings.get(0);
		ParserRuleContext last = siblings.get(siblings.size()-1);
		ParentSiblingListKey pair = new ParentSiblingListKey(ctx, first, separator.getType());
		SiblingListStats stats = corpus.rootAndChildListStats.get(pair);
		SiblingListStats splitStats = corpus.rootAndSplitChildListStats.get(pair);
		int len = Trainer.getSiblingsLength(siblings);
		boolean oversize = isOversizeList(ctx, siblings, separator);
		System.out.println((oversize?"   ":"")+
			                   "SPLIT "+JavaParser.ruleNames[ctx.getRuleIndex()]+
			                   "->"+JavaParser.ruleNames[first.getRuleIndex()]+" sep "+
			                   JavaParser.tokenNames[separator.getType()]+
			                   " "+separator+" '"+StringUtils.abbreviate(Trainer.getSiblingsText(siblings), 30)+
			                   "' len="+len+" stats="+stats+" splitstats="+splitStats+" oversize="+oversize);

		if ( oversize ) {
			// inject newline before or after separator and before/after list depending on most common pattern
			Integer formI = corpus.splitListForms.get(pair);
			int[] form = Trainer.unlistform(formI);
			if ( form[0]!=0 ) { // before first element in list
				injection[first.getStart().getTokenIndex()] = "\n";
			}
			List<Tree> separators = getSeparators(ctx, siblings);
			if ( form[1]!=0 ) { // before separator
				separators.forEach(t -> {
					int ti = ((Token) t.getPayload()).getTokenIndex();
					injection[ti] = "\n";
				});
			}
			if ( form[2]!=0 ) { // after separator, meaning right before sibling elements (except first element)
				siblings.subList(1,siblings.size()).forEach(r -> {
					int ti = r.getStart().getTokenIndex();
					injection[ti] = "\n";
				});
			}
			if ( form[3]!=0 ) { // after last element in list
				int indexOfTokenAfterList = last.getStop().getTokenIndex()+1;
				List<Token> nextRealToken = tokens.getRealTokens(indexOfTokenAfterList, indexOfTokenAfterList);
				if ( nextRealToken!=null ) {
					injection[nextRealToken.get(0).getTokenIndex()] = "\n";
				}
			}
		}

		Map<Token, Pair<Boolean, Integer>> tokenInfo = getInfoAboutListTokens(ctx, tokens, siblings, oversize);
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
		if ( stats!=null&&splitStats!=null &&
			Math.abs(splitStats.median-len) < Math.abs(stats.median-len) )
		{
			oversize = true;
		}
		return oversize;
	}
}
