#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.12 'Wed May 11 16:13:12 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AttributeRenderer.java", "AutoIndentWriter.java", "Bytecode.java", "BytecodeDisassembler.java", "CompilationState.java", "CompiledST.java", "Compiler.java", "FormalArgument.java", "STException.java", "STLexer.java", "StringTable.java", "DateRenderer.java", "AddAttributeEvent.java", "ConstructionEvent.java", "EvalExprEvent.java", "EvalTemplateEvent.java", "IndentEvent.java", "InterpEvent.java", "JTreeASTModel.java", "JTreeScopeStackModel.java", "JTreeSTModel.java", "STViewFrame.java", "STViz.java", "InstanceScope.java", "Interpreter.java", "Aggregate.java", "AggregateModelAdaptor.java", "AmbiguousMatchException.java", "ArrayIterator.java", "Coordinate.java", "ErrorBuffer.java", "ErrorManager.java", "ErrorType.java", "Interval.java", "MapModelAdaptor.java", "Misc.java", "MultiMap.java", "ObjectModelAdaptor.java", "STCompiletimeMessage.java", "STGroupCompiletimeMessage.java", "STLexerMessage.java", "STMessage.java", "STModelAdaptor.java", "STNoSuchAttributeException.java", "STNoSuchPropertyException.java", "STRuntimeMessage.java", "TypeRegistry.java", "ModelAdaptor.java", "NoIndentWriter.java", "NumberRenderer.java", "ST.java", "STErrorListener.java", "STGroup.java", "STGroupDir.java", "STGroupFile.java", "STGroupString.java", "STRawGroupDir.java", "StringRenderer.java", "STWriter.java"]
N = len(labels)

featureIndexes = range(0,N)
java_self = [0.0014144272, 0.012811303, 0.030774673, 0.013103334, 0.01860587, 0.012028793, 0.218416, 0.00518836, 5.7670125E-4, 0.078947365, 0.0013704888, 0.17864668, 0.027120316, 0.00905387, 0.044477392, 5.5648305E-4, 5.488474E-4, 0.015611448, 0.00597213, 0.0073463535, 0.014466949, 0.013449565, 0.14726113, 0.0052476223, 0.04515959, 0.0012607161, 0.0018912529, 0.0014814815, 0.0031633056, 0.005720229, 0.0019984012, 0.20679672, 0.0010741139, 0.0037695207, 0.0011406844, 0.015416505, 0.0015166835, 0.09913393, 0.020838564, 0.027459955, 0.0031109576, 0.0028653296, 5.0813006E-4, 0.0018779343, 0.0013805799, 0.004553079, 5.923293E-4, 0.0014109347, 0.0015923567, 0.0016373311, 0.034215078, 0.0016556291, 0.05029586, 0.2618569, 0.28038004, 0.01141925, 0.022051282, 0.0038582108, 0.0045045046]
java_corpus = [0.0014144272, 0.007682151, 0.05730555, 0.014927849, 0.019392034, 0.018564122, 0.16103922, 0.009968283, 0.011415525, 0.07721785, 0.002739726, 0.18152124, 0.041420117, 0.017833259, 0.057820607, 0.0011129661, 0.0010976949, 0.017779706, 0.0069651743, 0.0073463535, 0.016751157, 0.049215924, 0.16366583, 0.0049423394, 0.036369413, 0.001512478, 0.0075650117, 0.0024703557, 0.0027722772, 0.021505376, 0.0028011205, 0.22166277, 0.004549104, 0.020624008, 0.008365019, 0.01364924, 0.0015166835, 0.125, 0.02535777, 0.02876757, 0.0031120332, 0.006841505, 0.0071138213, 0.0018796993, 0.0013805799, 0.042837404, 0.011998222, 0.0045887753, 0.0015923567, 0.0012285012, 0.044762265, 0.0033057851, 0.043212336, 0.04084581, 0.034601044, 0.011425463, 0.026153846, 0.008425614, 0.0047707395]
java_diff = np.abs(np.subtract(java_self, java_corpus))

all = zip(java_self, java_corpus, java_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
java_self, java_corpus, java_diff, labels = zip(*all)

ax.plot(featureIndexes, java_self, label="java_self")
#ax.plot(featureIndexes, java_corpus, label="java_corpus")
ax.plot(featureIndexes, java_diff, label="java_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(java_self),np.median(java_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File java $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/java_one_file_capture.pdf", format='pdf')
plt.show()
