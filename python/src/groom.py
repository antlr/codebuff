import sys
from sklearn.ensemble import RandomForestClassifier
from groomlib import analyze_corpus
from groomlib import convert_categorical_data
from groomlib import format_code
from groomlib import graph_importance
from groomlib import print_importances
import numpy as np
import groomlib

sample_java = \
    """
    package org.antlr.groom;

    import java.util.List;

    public class InputDocument {
        public String fileName;
        public char[] content;
        public int index;
        public List<int[]> data;

        public InputDocument(InputDocument d, int index) {
            this.fileName = d.fileName;
            this.content = d.content;
            this.index = index;
        }

        public InputDocument(String fileName, char[] content) {
            this.content = content;
            this.fileName = fileName;
        }

        @Override
        public String toString(String fileName, char[] content) {
            i = this.content + content;
            return fileName+"["+content.length+"]"+"@"+index;
        }
    }
    """

# def newlines(csvfile):
#     """
#     Return a random forest trained on a style.csv file
#     """
#     data = np.loadtxt(csvfile, delimiter=",", skiprows=1)
#
#     X = data[0::,1::]	# features
#     Y = data[0::,0]	    # prediction class
#
#     # get feature names
#     with open(csvfile, 'r') as f:
#         features = f.readline().strip().split(', ')
#         features = features[1:] # first col is predictor var
#
#     print "there are %d records" % len(data)
#     print "a priori   'inject newline' rate is %3d/%4d = %f" % (sum(Y), len(Y), sum(Y)/float(len(Y)))
#
#     # train model on entire data set from style.csv
#     forest = RandomForestClassifier(n_estimators = 600)
#     forest = forest.fit(X, Y)
#     return forest

# forest = newlines(csvfile) # train model

# TRAIN ON CORPUS
# import pstats
# cProfile.run("inject_newlines, features = analyze_corpus(sys.argv[1])", "stats")
# p = pstats.Stats('stats')
# p.strip_dirs().sort_stats("time").print_stats()

inject_newlines, features = analyze_corpus(sys.argv[1])
vec, transformed_data = convert_categorical_data(features)

X = transformed_data
Y = inject_newlines  # prediction class

forest = RandomForestClassifier(n_estimators=100)
forest = forest.fit(X, Y)

print_importances(forest, vec.get_feature_names(), n=15)

# PREDICT

# sample_java = open("samples/stringtemplate4/org/stringtemplate/v4/STGroup.java", "r").read()
sample_java = sample_java.expandtabs(groomlib.TABSIZE)
format_code(forest, vec, sample_java)


# graph_importance(forest, vec.get_feature_names())

# tokens_testing, inject_newlines_testing, features_testing = extract_data(sample_java)
# transformed_data_testing = vec.transform(todict(features_testing)).toarray()
# X = transformed_data_testing
# Y_truth = inject_newlines_testing	    # truth about newlines in sample input
#
# newline_predictions = forest.predict(X)
# newline_predictions_proba = forest.predict_proba(X)
# i = 0
# for probs in newline_predictions_proba:
#     print "%-25s %s" % (probs, tokens_testing[i])
#     i += 1
#
# format_code(sample_java, None)
# format_code(sample_java, newline_predictions)

# graph_importance(forest, vec.get_feature_names(), X)
