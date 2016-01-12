import sys
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from JavaListener import JavaListener
from antlr4.tree.Trees import Trees
from sklearn.feature_extraction import DictVectorizer
from CollectTokenFeatures import CollectTokenFeatures

"""
Similar to CollectTokenFeatures but we do prediction of newline on the fly
for each token.  Must adjust token line/column info on the fly as current token
position depends on previous tokens and prediction of newline.
"""
class ProcessTokens(JavaListener):
    def __init__(self, forest, vec, stream):
        self.forest = forest
        self.vec = vec          # the DictVectorizer used to transform categorical features
        self.stream = stream    # track stream so we can examine previous tokens

    def visitTerminal(self, node):
        curToken = node.symbol
        if curToken.type==-1:
            return
        prevToken = None
        if curToken.tokenIndex>=1:
            prevToken = self.stream.tokens[curToken.tokenIndex-1]

        #print dir(node)
        ruleIndex = node.getParent().getRuleIndex()
        ruleName = JavaParser.ruleNames[ruleIndex]
        earliestAncestor = self.earliestAncestorStartingAtToken(node.getParent(),curToken)
        earliestAncestorName = 'none'
        earliestAncestorWidth = 0
        if earliestAncestor is not None:
            earliestAncestorRuleIndex = earliestAncestor.getRuleIndex()
            earliestAncestorName = JavaParser.ruleNames[earliestAncestorRuleIndex]
            earliestAncestorWidth = earliestAncestor.stop.stop - earliestAncestor.start.start + 1

        # predict newline based upon curToken appearing after prevToken on same line
        if  prevToken is not None:
            curToken.column = prevToken.column + len(prevToken.text)
        else:
            curToken.column = 0

        vars = [JavaLexer.symbolicNames[curToken.type], curToken.column, len(curToken.text),
                ruleName, earliestAncestorName, earliestAncestorWidth]
        if prevToken is not None:
            endofprevtoken = prevToken.column + len(prevToken.text) - 1
            vars += [JavaLexer.symbolicNames[prevToken.type], prevToken.column, endofprevtoken]
        else:
            vars += ['None', -1, 0]

        feature_names = CollectTokenFeatures.feature_names
        n = len(feature_names)
        d = dict((feature_names[i], vars[i]) for i in range(0, n))
        # print d
        transformed_data_testing = self.vec.transform(d).toarray()
        inject_newline = self.forest.predict(transformed_data_testing)
        # print "inject_newline", inject_newline
        if inject_newline:
            curToken.column = 0
            print
        print curToken.text,

    def earliestAncestorStartingAtToken(self, node, token):
        """
        Walk upwards from node while p.start == token
        """
        p = node
        prev = None
        while p is not None and p.start == token:
            prev = p
            p = p.parentCtx

        return prev