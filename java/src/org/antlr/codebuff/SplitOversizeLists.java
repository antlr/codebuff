package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.CodeBuffTokenStream;
import org.antlr.codebuff.misc.ParentSiblingListKey;
import org.antlr.codebuff.misc.SiblingListStats;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/** Walk tree and inject newlines where needed, before or after lists
 *  with separators that are oversized.
 */
public class SplitOversizeLists extends VisitSiblingLists {
	Corpus corpus;
	String[] injection;
	CodeBuffTokenStream tokens;

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
		List<Tree> children = Trees.getChildren(ctx);
		ParentSiblingListKey pair = new ParentSiblingListKey(ctx, first, separator.getType());
		SiblingListStats stats = corpus.rootAndChildListStats.get(pair);
		SiblingListStats splitStats = corpus.rootAndSplitChildListStats.get(pair);
		// Either:
		// a) we've only seen as a split list
		// b) we've seen as both list and split list: if len closer to split median, decide it's oversize
		int len = CollectFeatures.getSiblingsLength(siblings);
		boolean oversize = CollectFeatures.isOversizeList(corpus, ctx, siblings, separator);
		System.out.println((oversize?"   ":"")+
			                   "SPLIT "+JavaParser.ruleNames[ctx.getRuleIndex()]+
			                   "->"+JavaParser.ruleNames[first.getRuleIndex()]+" sep "+
			                   JavaParser.tokenNames[separator.getType()]+
			                   " "+separator+" '"+StringUtils.abbreviate(CollectFeatures.getSiblingsText(siblings), 30)+
			                   "' len="+len+" stats="+stats+" splitstats="+splitStats+" oversize="+oversize);
		// find all separators
		int start = BuffUtils.indexOf(ctx, first);
		int end = BuffUtils.indexOf(ctx, last);

		List<Tree> elements = children.subList(start, end+1);
		List<Tree> separators = BuffUtils.filter(elements, c -> c instanceof TerminalNode);
		if ( oversize ) {
			// inject newline before or after separator and before/after list depending on most common pattern
			Integer formI = corpus.splitListForms.get(pair);
			int[] form = CollectFeatures.unlistform(formI);
			if ( form[0]!=0 ) { // before first element in list
				injection[first.getStart().getTokenIndex()] = "\n";
			}
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
	}

}
