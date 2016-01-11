import sys
from sklearn.ensemble import RandomForestClassifier
import numpy as np
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from DumpFeatures import DumpFeatures

csvfile = "samples/stringtemplate4/style.csv"

sample_java = \
"""
public class T {
    int i;
    int j;
    int[] a = {
        1,2,3,
        4,5,6
    };
    void foo() { int j; j=i+10; }
}
"""

def newlines(csvfile):
    """
    Return a random forest trained on a style.csv file
    """
    data = np.loadtxt(csvfile, delimiter=",", skiprows=1)

    X = data[0::,1::]	# features
    Y = data[0::,0]	    # prediction class

    # get feature names
    with open(csvfile, 'r') as f:
        features = f.readline().strip().split(', ')
        features = features[1:] # first col is predictor var

    print "there are %d records" % len(data)
    print "a priori   'inject newline' rate is %3d/%4d = %f" % (sum(Y), len(Y), sum(Y)/float(len(Y)))

    # train model on entire data set from style.csv
    forest = RandomForestClassifier(n_estimators = 600)
    forest = forest.fit(X, Y)
    return forest

forest = newlines(csvfile) # train model

# file_to_groom = sys.argv[1]
input = InputStream(sample_java)
lexer = JavaLexer(input)
tokens = CommonTokenStream(lexer)
parser = JavaParser(tokens)
tree = parser.compilationUnit()

for t in tokens.tokens:
    print t.text,

collector = DumpFeatures(tokens)
walker = ParseTreeWalker()
walker.walk(collector, tree)

# print collector.data

newline_predictions = forest.predict(collector.data)
print newline_predictions
i = 0
for t in tokens.tokens:
    if t.type==-1: break
    if newline_predictions[i]:
        print
    print t.text,
    i += 1
