from JavaListener import JavaListener
from CollectTokenFeatures import CollectTokenFeatures
import groomlib
from antlr4 import ParseTreeListener

"""
Similar to CollectTokenFeatures but we do prediction of newline on the fly
for each token.  Must adjust token line/column info on the fly as current token
position depends on previous tokens and prediction of newline.
"""
class ProcessTokens(ParseTreeListener):
    def __init__(self, forest, vec, stream):
        self.forest = forest
        self.vec = vec          # the DictVectorizer used to transform categorical features
        self.stream = stream    # track stream so we can examine previous tokens

    def visitTerminal(self, node):
        curToken = node.symbol
        if curToken.type==-1:
            return
        if curToken.tokenIndex>=1:
            # predict newline based upon curToken appearing after prevToken on same line
            prevToken = self.stream.tokens[curToken.tokenIndex-1]
            curLine = prevToken.line
            curToken.column = prevToken.column + len(prevToken.text)
        else:
            curLine = 1
            curToken.column = 0
        curToken.line = curLine

        vars = groomlib.node_features(self.stream.tokens, node)

        feature_names = groomlib.FEATURE_NAMES
        n = len(feature_names)
        d = dict((feature_names[i], vars[i]) for i in range(0, n))
        # print d
        transformed_data_testing = self.vec.transform(d).toarray()
        inject_newline = self.forest.predict(transformed_data_testing)
        # newline_predictions_proba = self.forest.predict_proba(transformed_data_testing)
        # print curToken, "->", newline_predictions_proba
        # print "inject_newline", inject_newline
        if inject_newline:
            curToken.line += 1
            curToken.column = 0
            print
        print curToken.text,
