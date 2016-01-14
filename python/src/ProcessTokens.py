from JavaListener import JavaListener
from CollectTokenFeatures import CollectTokenFeatures
import groomlib
from antlr4 import ParseTreeListener
import sys

"""
Similar to CollectTokenFeatures but we do prediction of newline on the fly
for each token.  Must adjust token line/column info on the fly as current token
position depends on previous tokens and prediction of newline.
"""
class ProcessTokens(ParseTreeListener):
    def __init__(self, newline_forest, indent_forest, whitespace_forest, vec, stream):
        self.newline_forest = newline_forest
        self.indent_forest = indent_forest
        self.whitespace_forest = whitespace_forest
        self.vec = vec          # the DictVectorizer used to transform categorical features
        self.stream = stream    # track stream so we can examine previous tokens
        self.current_indent = 0

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
        transformed_features_testing = self.vec.transform(d).toarray()
        inject_newline = self.newline_forest.predict(transformed_features_testing)
        # newline_predictions_proba = self.forest.predict_proba(transformed_data_testing)
        # print curToken, "->", newline_predictions_proba
        # print "inject_newline", inject_newline
        if inject_newline:
            curToken.line += 1
            curToken.column = 0
            print # inject a newline
            # now figure out indent
            indent = self.indent_forest.predict(transformed_features_testing)
            # if indent!=0:
            #     print "indent %d at %s" % (indent,curToken)
            self.current_indent += indent
            sys.stdout.write(" " * self.current_indent) # inject indent
        else:
            ws = self.whitespace_forest.predict(transformed_features_testing)
            sys.stdout.write(" " * ws) # inject whitespace before token

        sys.stdout.write(curToken.text)
