import matplotlib.pyplot as plt

no = [41, 62, 72, 121, 87, 109, 72, 64, 41, 38, 53]
yes = [76, 81, 44, 293, 215, 82, 92, 70, 44, 38, 69, 49, 54, 69, 85, 43, 82, 63]

# the histogram of the data
n, bins, patches = plt.hist(no, 10, normed=1, facecolor='red', alpha=0.75)
n, bins, patches = plt.hist(yes, 10, normed=1, facecolor='green', alpha=0.75)

plt.grid(True)

plt.show()