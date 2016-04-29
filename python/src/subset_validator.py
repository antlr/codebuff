#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff %s '%s'
#
import matplotlib.pyplot as plt

fig = plt.figure()
ax = plt.subplot(111)
N = 14
sizes = range(1,N+1)
java = [0.19724771,0.09901961,0.11211664,0.11627907,0.083333336,0.084632516,0.13913915,0.122824974,0.08401305,0.14666666,0.08943089,0.08130081,0.10754717,0.08896211]
ax.plot(sizes, java, label="java", marker='o')
sqlite = [0.28715083,0.20187794,0.18737672,0.16666667,0.18681319,0.18055555,0.15234375,0.16927083,0.19537275,0.16402116,0.14720812,0.1554054,0.14285715,0.15692307]
ax.plot(sizes, sqlite, label="sqlite", marker='o')
java8 = [0.20166898,0.12376238,0.09090909,0.083333336,0.09615385,0.12111801,0.110044315,0.08943089,0.07692308,0.10144927,0.10490856,0.08543347,0.0875,0.11627907]
ax.plot(sizes, java8, label="java8", marker='o')
antlr = [0.44604316,0.27181605,0.28723404,0.16751668,0.32150313,0.20704846,0.28317234,0.217119,0.21582733,0.18742922,0.19148937,0.14537445,0.0,0.0]
ax.plot(sizes, antlr, label="antlr", marker='o')
tsql = [0.28169015,0.21369295,0.20971867,0.15666667,0.17124394,0.1779661,0.16913946,0.15447155,0.14197531,0.16759777,0.1569966,0.14814815,0.1722365,0.15196079]
ax.plot(sizes, tsql, label="tsql", marker='o')

ax.set_xlabel("Number n of training files in corpus")
ax.set_ylabel("Median Error rate for 30 trials")
ax.set_title("Corpus size vs Median Leave-one-out Validation Error Rate\nN=30 trials")
plt.legend()
plt.show()
