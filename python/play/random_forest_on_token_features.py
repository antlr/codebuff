from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import OneHotEncoder
import numpy as np
import matplotlib.pyplot as plt
import sys

def print_importance(forest):
    importances = forest.feature_importances_
    indices = np.argsort(importances)[::-1]
    # Compute stddev of forest's feature importances over all trees
    std = np.std([tree.feature_importances_ for tree in forest.estimators_], axis=0)
    for f in range(X_training.shape[1]):
        print "%d. feature %s col %d (%f, inter-tree variability=%f)" % \
              (f + 1,
               token_features[indices[f]],
               indices[f],
               importances[indices[f]],
               std[indices[f]])

def graph_importance(forest):
    importances = forest.feature_importances_
    std = np.std([tree.feature_importances_ for tree in forest.estimators_], axis=0)
    indices = np.argsort(importances)[::-1]

    fig, ax = plt.subplots(1,1)
    plt.title("Feature importances")
    xlabels = [token_features[int(i)] for i in indices]
    plt.bar(range(X_training.shape[1]), importances[indices],
           color="r", yerr=std[indices], align="center")
    plt.xticks(range(X_training.shape[1]), xlabels, rotation=15)
    plt.xlim([-1, X_training.shape[1]])
    plt.ylim([0, 1])

    for tick in ax.xaxis.get_major_ticks():
        tick.tick1line.set_markersize(0)
        tick.tick2line.set_markersize(0)
        tick.label1.set_horizontalalignment('right')

    plt.show()

data = np.loadtxt("samples/stringtemplate4/style.csv", delimiter=",", skiprows=1)

token_features = []
with open("samples/stringtemplate4/style.csv", 'r') as f:
    token_features = f.readline().strip().split(', ')
    token_features = token_features[1:] # first col is predictor var

X = data[0::,1::]	# features
Y = data[0::,0]	    # prediction class

# get first 80% as training data, 20% as testing data
n = len(data)
last_training_index = n * 0.80
X_training = X[0:last_training_index]
X_testing = X[last_training_index:]
Y_training = Y[0:last_training_index]
Y_testing = Y[last_training_index:]

print "there are %d records, %d training and %d testing" % (len(data), len(X_training), len(X_testing))
print "a priori   'inject newline' rate is %3d/%4d = %f" % (sum(Y), len(Y), sum(Y)/float(len(Y)))

# transform categorical values
index_of_cat_features = [0, 3, 4, 5]
# todo

forest = RandomForestClassifier(n_estimators = 100)
forest = forest.fit(X_training, Y_training)

Y_predictions = forest.predict(X_testing)

print "expected   'inject newline' rate is %3d/%4d = %f" % \
      (sum(Y_testing), len(Y_testing), sum(Y_testing)/float(len(Y_testing)))
print "prediction 'inject newline' rate is %3d/%4d = %f" % \
      (sum(Y_predictions), len(Y_predictions), sum(Y_predictions)/float(len(Y_predictions)))

# print "predictions:"
# print Y_predictions
#
# print "actual:"
# print Y_testing

# print "diff"
# print Y_testing-Y_predictions

misclassified = int(abs(sum(Y_testing - Y_predictions)))
print "number misclassified: %d/%d=%f%%" %\
      (misclassified, len(Y_testing), misclassified/float(len(Y_testing))*100)

print_importance(forest)

# now graph and show important features

graph_importance(forest)
