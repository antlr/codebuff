#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.19 'Sat Jun 18 16:47:47 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AccessibilityEvent.quorum", "AccessibilityManager.quorum", "AccessibleChild.quorum", "EverythingObserver.quorum", "ExtensibleMarkupLanguageParser.quorum", "FocusEvent.quorum", "FocusObserver.quorum", "KeyboardEvent.quorum", "KeyboardObserver.quorum", "MenuEvent.quorum", "MenuObserver.quorum", "MouseEvent.quorum", "MouseObserver.quorum", "PropertyEvent.quorum", "PropertyObserver.quorum", "SystemEvent.quorum", "WindowEvent.quorum", "WindowObserver.quorum", "Array.quorum", "Addable.quorum", "ArrayBlueprint.quorum", "Container.quorum", "Copyable.quorum", "HashTableBlueprint.quorum", "Indexed.quorum", "Iterative.quorum", "Iterator.quorum", "KeyedAddable.quorum", "KeyedIterative.quorum", "ListBlueprint.quorum", "QueueBlueprint.quorum", "Sortable.quorum", "StackBlueprint.quorum", "TableBlueprint.quorum", "HashTable.quorum", "List.quorum", "Queue.quorum", "Stack.quorum", "ArrayIterator.quorum", "HashNode.quorum", "HashTableIterator.quorum", "HashTableKeyIterator.quorum", "HashTableValueIterator.quorum", "KeyedNode.quorum", "ListIterator.quorum", "ListNode.quorum", "Table.quorum", "Button.quorum", "CollisionEvent.quorum", "CollisionEvent2D.quorum", "CollisionEvent3D.quorum", "KeyboardEvent.quorum", "KeyboardListener.quorum", "KeyboardProcessor.quorum", "MouseEvent.quorum", "MouseListener.quorum", "MouseMovementListener.quorum", "MouseProcessor.quorum", "MouseWheelListener.quorum", "TouchEvent.quorum", "TouchListener.quorum", "Item.quorum", "Item2D.quorum", "Item3D.quorum", "Panel.quorum", "TextBox.quorum", "CastError.quorum", "DivideByZeroError.quorum", "EndOfFileError.quorum", "Error.quorum", "FileNotFoundError.quorum", "InputOutputError.quorum", "InvalidArgumentError.quorum", "InvalidLocationError.quorum", "InvalidPathError.quorum", "MatrixError.quorum", "OutOfBoundsError.quorum", "ParseError.quorum", "UndefinedObjectError.quorum", "Object.quorum", "CompareResult.quorum", "Boolean.quorum", "Integer.quorum", "Number.quorum", "Text.quorum", "Audio.quorum", "Chord.quorum", "Instrument.quorum", "Music.quorum", "MusicEvent.quorum", "Note.quorum", "Playable.quorum", "Speech.quorum", "Track.quorum", "FileRandomAccessBlueprint.quorum", "FileReaderBlueprint.quorum", "FileWriterBlueprint.quorum", "Console.quorum", "DateTime.quorum", "File.quorum", "FileRandomAccess.quorum", "FileReader.quorum", "FileWriter.quorum", "Path.quorum", "Properties.quorum", "StackTraceItem.quorum", "SystemHelper.quorum"]
N = len(labels)

featureIndexes = range(0,N)
quorum_self = [0.0073099416, 0.0057061343, 0.004962779, 0.008215962, 0.003688604, 0.0038986355, 0.0055555557, 0.016325798, 0.005610098, 0.003968254, 0.0054274085, 0.015222161, 0.0057471264, 0.0028248588, 0.005, 0.0047959182, 0.0032733225, 0.0052083335, 0.0074608787, 0.0062421975, 0.0056917686, 0.008695652, 0.0056306305, 0.0041695624, 0.0064020487, 0.01010101, 0.009377664, 0.006305638, 0.0062176166, 0.011499337, 0.0055599683, 0.0034642033, 0.006535948, 0.003220612, 0.020187957, 0.004227557, 0.003965228, 0.0044288333, 0.0033964096, 0.03587444, 0.16777308, 0.039647575, 0.039187226, 0.0076465593, 0.0034078807, 0.0057077627, 0.0038704684, 0.0041935, 0.009199632, 0.010670732, 0.010670732, 0.003618176, 0.0073891627, 0.010332434, 0.013578501, 0.009259259, 0.008708273, 0.008828073, 0.00814664, 0.011797578, 0.0074487897, 0.005622688, 0.007048723, 0.005635333, 0.006359895, 0.0032948928, 0.0018214936, 0.0033557047, 0.0023474179, 0.0037556335, 0.0029895366, 0.0021482278, 0.0028612304, 0.0023584906, 0.007226739, 0.0028985508, 0.002444988, 0.0016025641, 0.0031152647, 0.0031069685, 0.002762431, 0.0051223678, 0.0057242992, 0.0060038073, 0.0070229797, 0.0033198218, 0.0076595745, 0.00385208, 0.0065352237, 0.00885878, 0.0068649887, 0.005846774, 0.0048687183, 0.00907544, 0.020926757, 0.027496383, 0.027777778, 0.005990783, 0.0046813106, 0.0061018225, 0.0048613413, 0.0058425297, 0.004263693, 0.009960718, 0.0050929463, 0.00414823, 0.005701254]
quorum_corpus = [0.009502924, 0.007890603, 0.0049663, 0.008215962, 0.0038842494, 0.001953125, 0.0041724616, 0.025296018, 0.004213483, 0.003968254, 0.004076087, 0.020844761, 0.004316547, 0.0028248588, 0.0037546933, 0.013663968, 0.0032733225, 0.003911343, 0.010111761, 0.0068621333, 0.0061269146, 0.014534884, 0.0056306305, 0.007670851, 0.006828852, 0.0115440115, 0.010238908, 0.0066790353, 0.008324662, 0.011057055, 0.007148531, 0.00461361, 0.0073589534, 0.004022526, 0.027298493, 0.17020446, 0.08595828, 0.0047357166, 0.10442555, 0.12340426, 0.05612553, 0.041116007, 0.040638607, 0.031936955, 0.16104734, 0.076079264, 0.006474573, 0.0052473764, 0.05292479, 0.010670732, 0.010670732, 0.003724593, 0.00863132, 0.01212938, 0.006843456, 0.010819165, 0.010174419, 0.009710881, 0.010204081, 0.005941213, 0.008387698, 0.0068655536, 0.00789669, 0.07775894, 0.008230452, 0.0029506206, 0.0036363637, 0.0050251256, 0.0023474179, 0.0037556335, 0.0029895366, 0.0021482278, 0.0028612304, 0.0023584906, 0.007226739, 0.0028985508, 0.002444988, 0.0032, 0.0031152647, 0.004436557, 0.009602195, 0.005701254, 0.008393565, 0.006447831, 0.007333196, 0.0037424327, 0.0072340425, 0.003468208, 0.014850937, 0.0093847755, 0.0038255546, 0.005846774, 0.0053875563, 0.00907544, 0.021674141, 0.027496383, 0.027777778, 0.0064545874, 0.004860486, 0.021181656, 0.0048613413, 0.0059352685, 0.0041543674, 0.010241302, 0.0053435112, 0.004426003, 0.005323194]
quorum_diff = np.abs(np.subtract(quorum_self, quorum_corpus))

all = zip(quorum_self, quorum_corpus, quorum_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
quorum_self, quorum_corpus, quorum_diff, labels = zip(*all)

ax.plot(featureIndexes, quorum_self, label="quorum_self")
#ax.plot(featureIndexes, quorum_corpus, label="quorum_corpus")
ax.plot(featureIndexes, quorum_diff, label="quorum_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(quorum_self),np.median(quorum_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File quorum $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/quorum_one_file_capture.pdf", format='pdf')
plt.show()
