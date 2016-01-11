import sys
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from JavaListener import JavaListener
from antlr4.tree.Trees import Trees

class DumpFeatures(JavaListener):
    def __init__(self, stream):
        self.stream = stream # track stream so we can examine previous tokens
        pass

    def visitTerminal(self, node):
        i = node.symbol.tokenIndex
        curToken = node.symbol
        prevToken = None
        precedingNL = False
        if i>=1:
            prevToken = self.stream.tokens[i-1]
            precedingNL = curToken.line > prevToken.line

        #print dir(node)
        ruleIndex = node.getParent().getRuleIndex()
        ruleName = JavaParser.ruleNames[ruleIndex]
        earliestAncestor = self.earliestAncestorStartingAtToken(node.getParent(),curToken)
        earliestAncestorName = None
        earliestAncestorRuleIndex = -1
        earliestAncestorWidth = 0
        if earliestAncestor is not None:
            earliestAncestorRuleIndex = earliestAncestor.getRuleIndex()
            earliestAncestorName = JavaParser.ruleNames[earliestAncestorRuleIndex]
            earliestAncestorWidth = earliestAncestor.stop.stop - earliestAncestor.start.start + 1

        features = [curToken.type, curToken.column, len(curToken.text),
                    ruleIndex, earliestAncestorRuleIndex, earliestAncestorWidth
                    ]
        if prevToken is not None:
            endofprevtoken = prevToken.column + len(prevToken.text) - 1
            features += [prevToken.type, prevToken.column, endofprevtoken]
        else:
            features += [0, -1, 0]
        print "%s, %s" % (1 if precedingNL else 0, ', '.join(str(x) for x in features))

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