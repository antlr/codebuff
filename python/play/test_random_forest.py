# looking at https://www.kaggle.com/c/titanic/details/getting-started-with-random-forests

from sklearn.ensemble import RandomForestClassifier
import numpy as np

forest = RandomForestClassifier(n_estimators = 100)

# let's get some data. So called "iris" data
# http://www.math.uah.edu/stat/data/Fisher.csv
data = np.loadtxt("iris.csv", delimiter=",", skiprows=1)

X = data[0::,1::]	# features
Y = data[0::,0]	    # prediction class

# get first 70% as training data, 30% as testing data
# it's 150 rows with 4 features, 1 class column (first col)
n = len(data)
last_training_index = n * 0.7
X_training = X[0:last_training_index]
X_testing = X[last_training_index:]
Y_training = Y[0:last_training_index]
Y_testing = Y[last_training_index:]

forest = forest.fit(X_training, Y_training)

Y_predictions = forest.predict(X_testing)

print "predictions:"
print Y_predictions

print "actual:"
print Y_testing

# print "diff"
# print Y_testing-Y_predictions

print "number misclassified:", int(abs(sum(Y_testing-Y_predictions)))