#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 15:56:29 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AttributeRenderer.java", "AutoIndentWriter.java", "Bytecode.java", "BytecodeDisassembler.java", "CompilationState.java", "CompiledST.java", "Compiler.java", "FormalArgument.java", "STException.java", "STLexer.java", "StringTable.java", "DateRenderer.java", "AddAttributeEvent.java", "ConstructionEvent.java", "EvalExprEvent.java", "EvalTemplateEvent.java", "IndentEvent.java", "InterpEvent.java", "JTreeASTModel.java", "JTreeScopeStackModel.java", "JTreeSTModel.java", "STViewFrame.java", "STViz.java", "InstanceScope.java", "Interpreter.java", "Aggregate.java", "AggregateModelAdaptor.java", "AmbiguousMatchException.java", "ArrayIterator.java", "Coordinate.java", "ErrorBuffer.java", "ErrorManager.java", "ErrorType.java", "Interval.java", "MapModelAdaptor.java", "Misc.java", "MultiMap.java", "ObjectModelAdaptor.java", "STCompiletimeMessage.java", "STGroupCompiletimeMessage.java", "STLexerMessage.java", "STMessage.java", "STModelAdaptor.java", "STNoSuchAttributeException.java", "STNoSuchPropertyException.java", "STRuntimeMessage.java", "TypeRegistry.java", "ModelAdaptor.java", "NoIndentWriter.java", "NumberRenderer.java", "ST.java", "STErrorListener.java", "STGroup.java", "STGroupDir.java", "STGroupFile.java", "STGroupString.java", "STRawGroupDir.java", "StringRenderer.java", "STWriter.java"]
N = len(labels)

featureIndexes = range(0,N)
java_self = [0.0014144272, 0.00921273, 0.030774673, 0.010449494, 0.013233753, 0.009566206, 0.2061341, 0.00518836, 5.7670125E-4, 0.042569626, 0.0013704888, 0.17826189, 0.027120316, 0.0036215482, 0.044477392, 5.5648305E-4, 5.488474E-4, 0.0160451, 0.00597213, 0.0069804904, 0.008637873, 0.013574097, 0.114877656, 0.0052476223, 0.045636185, 0.0012607161, 0.0018912529, 0.0014814815, 0.0027667985, 0.005720229, 0.0019984012, 0.20618945, 0.0010741139, 0.0037695207, 0.0011406844, 0.0117525505, 0.0015166835, 0.008285425, 0.02033643, 0.027459955, 0.0024196336, 0.0028653296, 5.0813006E-4, 0.0018779343, 0.0013805799, 0.0043144776, 5.923293E-4, 0.0014109347, 0.0015923567, 0.0016373311, 0.023233462, 0.0016556291, 0.06180504, 0.2565026, 0.29939926, 0.01141925, 0.022051282, 0.0038582108, 0.003968254]
java_corpus = [0.0014144272, 0.007682151, 0.30651522, 0.013600929, 0.020571278, 0.024351558, 0.17236817, 0.009968283, 0.009153318, 0.085019246, 0.002739726, 0.18152124, 0.043670263, 0.017833259, 0.05263158, 0.0011129661, 0.0010976949, 0.017779706, 0.0069651743, 0.0073463535, 0.016097024, 0.012577833, 0.16652292, 0.0049423394, 0.04210875, 0.001512478, 0.0075650117, 0.0024703557, 0.0023762377, 0.021505376, 0.0028011205, 0.22166277, 0.004549104, 0.020624008, 0.008365019, 0.013274906, 0.0015166835, 0.124659196, 0.076005615, 0.02876757, 0.0034578147, 0.006841505, 0.0071138213, 0.0018796993, 0.0013805799, 0.07252845, 0.01170197, 0.0042357924, 0.0015923567, 0.0012285012, 0.035987005, 0.0033057851, 0.042728722, 0.044760536, 0.03788218, 0.011425463, 0.026153846, 0.008182912, 0.0037066455]
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
