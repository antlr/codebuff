#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.14 'Sat May 14 15:57:56 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AttributeRenderer.java", "AutoIndentWriter.java", "Bytecode.java", "BytecodeDisassembler.java", "CompilationState.java", "CompiledST.java", "Compiler.java", "FormalArgument.java", "STException.java", "STLexer.java", "StringTable.java", "DateRenderer.java", "AddAttributeEvent.java", "ConstructionEvent.java", "EvalExprEvent.java", "EvalTemplateEvent.java", "IndentEvent.java", "InterpEvent.java", "JTreeASTModel.java", "JTreeScopeStackModel.java", "JTreeSTModel.java", "STViewFrame.java", "STViz.java", "InstanceScope.java", "Interpreter.java", "Aggregate.java", "AggregateModelAdaptor.java", "AmbiguousMatchException.java", "ArrayIterator.java", "Coordinate.java", "ErrorBuffer.java", "ErrorManager.java", "ErrorType.java", "Interval.java", "MapModelAdaptor.java", "Misc.java", "MultiMap.java", "ObjectModelAdaptor.java", "STCompiletimeMessage.java", "STGroupCompiletimeMessage.java", "STLexerMessage.java", "STMessage.java", "STModelAdaptor.java", "STNoSuchAttributeException.java", "STNoSuchPropertyException.java", "STRuntimeMessage.java", "TypeRegistry.java", "ModelAdaptor.java", "NoIndentWriter.java", "NumberRenderer.java", "ST.java", "STErrorListener.java", "STGroup.java", "STGroupDir.java", "STGroupFile.java", "STGroupString.java", "STRawGroupDir.java", "StringRenderer.java", "STWriter.java"]
N = len(labels)

featureIndexes = range(0,N)
java8_self = [0.0014144272, 0.00874461, 0.028827976, 0.009786034, 0.01831502, 0.011555219, 0.15715104, 0.0051907017, 5.7670125E-4, 0.07608514, 0.0013704888, 0.17826189, 0.026627218, 0.0036215482, 0.02186805, 5.5648305E-4, 5.488474E-4, 0.008605852, 0.00597213, 0.0030520647, 0.014046823, 0.013075965, 0.16972637, 0.0052476223, 0.042379897, 0.0012607161, 0.0018912529, 0.0014814815, 0.0015829046, 0.005720229, 0.0019984012, 0.2062492, 0.0010741139, 0.0037695207, 0.0011406844, 0.013592233, 0.0015166835, 0.01166723, 0.019834295, 0.026806146, 0.002073255, 0.0025787966, 5.0813006E-4, 0.0014091122, 0.0013805799, 0.0035945363, 0.004278548, 0.0014109347, 0.0015923567, 0.0016373311, 0.049746487, 0.0016556291, 0.05529043, 0.27790973, 0.2869508, 0.0046537095, 0.022051282, 0.0026506025, 0.0037046839]
java8_corpus = [0.0014144272, 0.01206979, 0.3065803, 0.018013552, 0.019228254, 0.038102284, 0.17545524, 0.009515179, 0.009153318, 0.09051864, 0.002739726, 0.18152124, 0.026134122, 0.012992832, 0.05263158, 0.0011129661, 0.0010976949, 0.02558543, 0.0066313, 0.06420464, 0.015834076, 0.014072229, 0.17210801, 0.00461285, 0.04397421, 0.001512478, 0.0075650117, 0.0024703557, 0.0027711797, 0.022006141, 0.0024009603, 0.23498191, 0.0010741139, 0.021141648, 0.008365019, 0.015229736, 0.0015166835, 0.11260426, 0.036899455, 0.032036614, 0.0031120332, 0.006554574, 0.0071138213, 0.0014091122, 0.0018399263, 0.060763497, 0.012585135, 0.0042357924, 0.0015923567, 0.0012285012, 0.042357508, 0.0033057851, 0.050637234, 0.04478592, 0.02699478, 0.011150395, 0.026153846, 0.006971154, 0.003970355]
java8_diff = np.abs(np.subtract(java8_self, java8_corpus))

all = zip(java8_self, java8_corpus, java8_diff, labels)
all = sorted(all, key=lambda x : x[2], reverse=True)
java8_self, java8_corpus, java8_diff, labels = zip(*all)

ax.plot(featureIndexes, java8_self, label="java8_self")
#ax.plot(featureIndexes, java8_corpus, label="java8_corpus")
ax.plot(featureIndexes, java8_diff, label="java8_diff")
ax.set_xticklabels(labels, rotation=60, fontsize=8)
plt.xticks(featureIndexes, labels, rotation=60)
ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=0.5)

ax.text(1, .25, 'median $f$ self distance = %5.3f, corpus+$f$ distance = %5.3f' %    (np.median(java8_self),np.median(java8_corpus)))
ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File java8 $f$\nwith Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/java8_one_file_capture.pdf", format='pdf')
plt.show()
