#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Fri May 06 17:40:28 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AttributeRenderer.java", "AutoIndentWriter.java", "Bytecode.java", "BytecodeDisassembler.java", "CompilationState.java", "CompiledST.java", "Compiler.java", "FormalArgument.java", "STException.java", "STLexer.java", "StringTable.java", "DateRenderer.java", "AddAttributeEvent.java", "ConstructionEvent.java", "EvalExprEvent.java", "EvalTemplateEvent.java", "IndentEvent.java", "InterpEvent.java", "JTreeASTModel.java", "JTreeScopeStackModel.java", "JTreeSTModel.java", "STViewFrame.java", "STViz.java", "InstanceScope.java", "Interpreter.java", "Aggregate.java", "AggregateModelAdaptor.java", "AmbiguousMatchException.java", "ArrayIterator.java", "Coordinate.java", "ErrorBuffer.java", "ErrorManager.java", "ErrorType.java", "Interval.java", "MapModelAdaptor.java", "Misc.java", "MultiMap.java", "ObjectModelAdaptor.java", "STCompiletimeMessage.java", "STGroupCompiletimeMessage.java", "STLexerMessage.java", "STMessage.java", "STModelAdaptor.java", "STNoSuchAttributeException.java", "STNoSuchPropertyException.java", "STRuntimeMessage.java", "TypeRegistry.java", "ModelAdaptor.java", "NoIndentWriter.java", "NumberRenderer.java", "ST.java", "STErrorListener.java", "STGroup.java", "STGroupDir.java", "STGroupFile.java", "STGroupString.java", "STRawGroupDir.java", "StringRenderer.java", "STWriter.java"]
N = len(labels)

featureIndexes = range(0,N)
java8_self = [4.7169812E-4, 0.01860521, 0.028253932, 0.009786034, 0.017688679, 0.010513355, 0.15910824, 0.004518753, 5.7670125E-4, 0.09630601, 4.570384E-4, 0.17787673, 0.026134122, 0.004518753, 0.021497406, 5.5648305E-4, 5.488474E-4, 0.020416845, 0.005312085, 0.002513465, 0.014046823, 0.009215442, 0.17277688, 0.003618421, 0.037556645, 5.044136E-4, 9.4562647E-4, 4.9407117E-4, 3.960396E-4, 0.004680187, 0.0012, 0.2062492, 2.6867277E-4, 0.0026925148, 0.0011406844, 0.01373057, 5.058169E-4, 0.01166723, 0.019081095, 0.029094474, 0.0013831259, 0.0017191977, 5.0813006E-4, 4.6992482E-4, 4.6040516E-4, 0.002159309, 0.0039846515, 3.529827E-4, 5.3106743E-4, 4.0966817E-4, 0.04362317, 5.521811E-4, 0.045118343, 0.2773925, 0.28646633, 0.0041084634, 0.02102564, 0.0016879672, 5.3092645E-4]
java8_corpus = [4.7169812E-4, 0.01536446, 0.30663788, 0.018022487, 0.018474843, 0.023555009, 0.167685, 0.009062075, 0.009163803, 0.06687286, 0.0018281536, 0.1814149, 0.026134122, 0.013876455, 0.054484803, 0.0011129661, 0.0010976949, 0.026452731, 0.005312085, 0.06388748, 0.015834076, 0.047238372, 0.16962567, 0.0039538713, 0.042155076, 0.0010088272, 0.0066193854, 0.0014822134, 0.001980198, 0.021004098, 0.0020008003, 0.20730183, 0.0013422819, 0.02010582, 0.008365019, 0.015233669, 5.058169E-4, 0.026547177, 0.036183394, 0.031055901, 0.002074689, 0.005988024, 0.0071138213, 4.6992482E-4, 9.2081033E-4, 0.029638274, 0.03915359, 0.003529827, 5.3106743E-4, 4.0966817E-4, 0.049824916, 5.521811E-4, 0.044208013, 0.035460994, 0.02699478, 0.010612245, 0.025128204, 0.0064935065, 0.0015927794]
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

ax.set_xlabel("File Name")
ax.set_ylabel("Edit Distance")
ax.set_title("Difference between Formatting File $f$ with Training=$f$ and Training=$f$+Corpus")
plt.legend()
plt.tight_layout()
fig.savefig("images/java8_one_file_capture.pdf", format='pdf')
plt.show()
