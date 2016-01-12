import groomlib
from antlr4 import ParseTreeListener

class CollectTokenFeatures(ParseTreeListener):
    def __init__(self, stream):
        self.stream = stream # track stream so we can examine previous tokens
        self.inject_newlines = []
        self.features = []

    def visitTerminal(self, node):
        i = node.symbol.tokenIndex
        curToken = node.symbol
        if curToken.type==-1:
            return
        precedingNL = False
        if i>=1:
            prevToken = self.stream.tokens[i-1]
            precedingNL = curToken.line > prevToken.line

        #print dir(node)
        vars = groomlib.node_features(self.stream.tokens, node)
        self.inject_newlines.append(1 if precedingNL else 0)
        self.features.append(vars)

        # print "%s, %s" % (1 if precedingNL else 0, ', '.join(str(x) for x in features))