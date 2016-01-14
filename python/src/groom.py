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
        return fileName+"["+content.length+"]"+
               "@"+index;
    }
}
"""

# TRAIN ON CORPUS
# import pstats
# cProfile.run("inject_newlines, features = analyze_corpus(sys.argv[1])", "stats")
# p = pstats.Stats('stats')
# p.strip_dirs().sort_stats("time").print_stats()

inject_newlines, indents, whitespace, features = analyze_corpus(sys.argv[1])
# for i in range(len(indents)):
#     print whitespace[i], features[i]
vec, transformed_features = convert_categorical_data(features)

newline_predictor_RF = RandomForestClassifier(n_estimators=300)
newline_forest = newline_predictor_RF.fit(transformed_features, inject_newlines)
print_importances(newline_forest, vec.get_feature_names(), n=15)

indent_predictor_RF = RandomForestClassifier(n_estimators=300)
indent_forest = indent_predictor_RF.fit(transformed_features, indents)
print_importances(indent_forest, vec.get_feature_names(), n=15)

whitespace_predictor_RF = RandomForestClassifier(n_estimators=300)
whitespace_forest = whitespace_predictor_RF.fit(transformed_features, whitespace)
print_importances(whitespace_forest, vec.get_feature_names(), n=15)

# PREDICT

# sample_java = open("samples/stringtemplate4/org/stringtemplate/v4/STGroup.java", "r").read()
sample_java = sample_java.expandtabs(groomlib.TABSIZE)
format_code(newline_forest, indent_forest, whitespace_forest, vec, sample_java)


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
