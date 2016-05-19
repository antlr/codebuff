#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.15 'Thu May 19 10:18:31 PDT 2016'
#
import matplotlib.pyplot as plt

quorum=[0.053846154, 0.044843048, 0.04464286, 0.038720537, 0.03960396, 0.041800644, 0.04256428, 0.04040404, 0.04263566, 0.04263566, 0.043624163]
java=[0.07102041, 0.060402684, 0.053435113, 0.05352798, 0.05352798, 0.05303761, 0.05303761, 0.05352798, 0.053435113, 0.05303761, 0.053097345]
java8=[0.074074075, 0.057747833, 0.05319149, 0.05233853, 0.048482955, 0.047131147, 0.050808314, 0.047115386, 0.047115386, 0.048245613, 0.049844235]
antlr=[0.19766825, 0.1815235, 0.20090996, 0.16949153, 0.16718563, 0.16694611, 0.16718563, 0.16718563, 0.16718563, 0.16718563, 0.16949153]
sqlite=[0.118644066, 0.11470588, 0.11038961, 0.10973085, 0.11026034, 0.10973085, 0.10696518, 0.1089838, 0.108895704, 0.105417274, 0.103896104]
tsql=[0.11570248, 0.100271, 0.101604275, 0.100271, 0.1020548, 0.10074074, 0.100271, 0.09150327, 0.09677419, 0.09536785, 0.09090909]

ks = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 131]
fig = plt.figure()
ax = plt.subplot(111)
ax.plot(ks, quorum, label="quorum", marker='o')
ax.plot(ks, java, label="java", marker='o')
ax.plot(ks, java8, label="java8", marker='o')
ax.plot(ks, antlr, label="antlr", marker='o')
ax.plot(ks, sqlite, label="sqlite", marker='o')
ax.plot(ks, tsql, label="tsql", marker='o')
ax.set_xlabel("k nearest neighbors")
ax.set_ylabel("Error rate")
ax.set_title("k Nearest Neighbors vs\nLeave-one-out Validation Error Rate")
plt.legend()
fig.savefig('images/vary_k.pdf', format='pdf')
plt.show()
