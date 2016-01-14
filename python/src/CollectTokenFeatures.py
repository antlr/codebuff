import groomlib
from antlr4 import ParseTreeListener

class CollectTokenFeatures(ParseTreeListener):
    def __init__(self, stream):
        self.stream = stream        # track stream so we can examine previous tokens
        self.inject_newlines = []   # prediction
        self.indent = []            # prediction of indent/dedent == column delta
        self.whitespace = []        # prediction of whitespace before token
        self.features = []          # independent vars
        self.first_token_on_line = None # track to compute indent

    def visitTerminal(self, node):
        i = node.symbol.tokenIndex
        curToken = node.symbol
        if curToken.type==-1:
            return
        precedingNL = False
        column_delta = 0
        ws = 0
        if i>=1:
            prevToken = self.stream.tokens[i-1]
            precedingNL = curToken.line > prevToken.line
            if precedingNL:
                if self.first_token_on_line is not None:
                    column_delta = curToken.column - self.first_token_on_line.column
                self.first_token_on_line = curToken
            else:
                ws = curToken.column - (prevToken.column+len(prevToken.text))

        #print dir(node)
        vars = groomlib.node_features(self.stream.tokens, node)
        self.inject_newlines.append(1 if precedingNL else 0)
        self.indent.append(column_delta)
        self.whitespace.append(ws)
        self.features.append(vars)
