import sys
from sklearn.ensemble import RandomForestClassifier
import numpy as np
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from CollectTokenFeatures import CollectTokenFeatures
from sklearn.feature_extraction import DictVectorizer
import matplotlib.pyplot as plt

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

def extract_data(code):
    input = InputStream(code)
    lexer = JavaLexer(input)
    tokens = CommonTokenStream(lexer)
    parser = JavaParser(tokens)
    tree = parser.compilationUnit()
    collector = CollectTokenFeatures(tokens)
    walker = ParseTreeWalker()
    walker.walk(collector, tree)
    return (tokens.tokens, collector.inject_newlines, collector.features)

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


def graph_importance(forest, feature_names, X):
    importances = forest.feature_importances_
    std = np.std([tree.feature_importances_ for tree in forest.estimators_], axis=0)
    indices = np.argsort(importances)[::-1]

    fig, ax = plt.subplots(1,1)
    plt.title("Feature importances")
    xlabels = [feature_names[int(i)] for i in indices]
    plt.bar(range(X.shape[1]), importances[indices],
           color="r", yerr=std[indices], align="center")
    plt.xticks(range(X.shape[1]), xlabels, rotation=15)
    plt.xlim([-1, X.shape[1]])
    plt.ylim([0, 1])

    for tick in ax.xaxis.get_major_ticks():
        tick.tick1line.set_markersize(0)
        tick.tick2line.set_markersize(0)
        tick.label1.set_horizontalalignment('right')

    plt.show()


def todict(features):
    records = []
    for record in features:
        d = dict((CollectTokenFeatures.features[i], record[i]) for i in range(0, len(CollectTokenFeatures.features)))
        records += [d]
    return records


# forest = newlines(csvfile) # train model

# file_to_groom = sys.argv[1]
with open("samples/stringtemplate4/org/stringtemplate/v4/ST.java", 'r') as f:
    corpus = f.read()

tokens, inject_newlines, features = extract_data(corpus)

records = todict(features)

vec = DictVectorizer(sort=False)
transformed_data = vec.fit_transform(records).toarray()

print len(vec.get_feature_names())
print vec.get_feature_names()
print vec.get_vocabulary()
print len(transformed_data[0])

X = transformed_data
Y = inject_newlines	    # prediction class

forest = RandomForestClassifier(n_estimators = 600)
forest = forest.fit(X, Y)

# PREDICT

tokens_testing, inject_newlines_testing, features_testing = extract_data(sample_java)
records_testing = todict(features_testing)
transformed_data_testing = vec.fit_transform(records_testing).toarray()
X = transformed_data_testing
print "197==",len(transformed_data_testing[0])
Y = inject_newlines_testing	    # prediction class

newline_predictions = forest.predict(X)
newline_predictions_proba = forest.predict_proba(X)
print newline_predictions_proba

i = 0
for t in tokens:
    if t.type==-1: break
    print t.text,
    i += 1

i = 0
for t in tokens:
    if t.type==-1: break
    if newline_predictions[i]:
        print
    print t.text,
    i += 1

graph_importance(forest, vec.get_feature_names(), X)