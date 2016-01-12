import sys
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from JavaListener import JavaListener
from antlr4.tree.Trees import Trees

class CollectTokenFeatures(JavaListener):
    INVALID_RULE_INDEX = 9999
    PREDICTOR_VAR = "inject newline"
    features = ["token type", "column", "length", "enclosing rule", "earliest ancestor rule",
                 "earliest ancestor length", "prev token type", "prev token column",
                 "prev token last char index"]

    def __init__(self, stream):
        self.stream = stream # track stream so we can examine previous tokens
        self.inject_newlines = []
        self.features = []
        pass

    def visitTerminal(self, node):
        i = node.symbol.tokenIndex
        curToken = node.symbol
        if curToken.type==-1:
            return
        prevToken = None
        precedingNL = False
        if i>=1:
            prevToken = self.stream.tokens[i-1]
            precedingNL = curToken.line > prevToken.line

        #print dir(node)
        ruleIndex = node.getParent().getRuleIndex()
        ruleName = JavaParser.ruleNames[ruleIndex]
        earliestAncestor = self.earliestAncestorStartingAtToken(node.getParent(),curToken)
        earliestAncestorName = 'none'
        earliestAncestorRuleIndex = -1
        earliestAncestorWidth = 0
        if earliestAncestor is not None:
            earliestAncestorRuleIndex = earliestAncestor.getRuleIndex()
            earliestAncestorName = JavaParser.ruleNames[earliestAncestorRuleIndex]
            earliestAncestorWidth = earliestAncestor.stop.stop - earliestAncestor.start.start + 1

        features = [JavaLexer.symbolicNames[curToken.type], curToken.column, len(curToken.text),
                    ruleName, earliestAncestorName, earliestAncestorWidth]
        if prevToken is not None:
            endofprevtoken = prevToken.column + len(prevToken.text) - 1
            features += [JavaLexer.symbolicNames[prevToken.type], prevToken.column, endofprevtoken]
        else:
            features += ['0', -1, 0]
        self.inject_newlines.append(1 if precedingNL else 0)
        self.features.append(features)

        # print "%s, %s" % (1 if precedingNL else 0, ', '.join(str(x) for x in features))

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