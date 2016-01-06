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
        ruleName = JavaParser.ruleNames[node.getParent().getRuleIndex()]
        earliestAncestor = self.earliestAncestorStartingAtToken(node.getParent(),curToken)
        earliestAncestorName = "None"
        earliestAncestorWidth = 0
        if earliestAncestor is not None:
            earliestAncestorName = JavaParser.ruleNames[earliestAncestor.getRuleIndex()]
            earliestAncestorWidth = earliestAncestor.stop.stop - earliestAncestor.start.start + 1

        features = [curToken.text, curToken.type, curToken.column, len(curToken.text),
                    ruleName, earliestAncestorName, earliestAncestorWidth
                    ]
        if prevToken is not None:
            endofprevtoken = prevToken.column + len(prevToken.text) - 1
            features += [prevToken.text, prevToken.type, prevToken.column, endofprevtoken]
        else:
            features += ['None', 0, -1, 0]
        print precedingNL, features

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