#
# AUTO-GENERATED FILE. DO NOT EDIT
# CodeBuff 1.4.7 'Mon May 09 17:56:36 PDT 2016'
#
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = plt.subplot(111)
labels = ["AttributeRenderer.java", "AutoIndentWriter.java", "Bytecode.java", "BytecodeDisassembler.java", "CompilationState.java", "CompiledST.java", "Compiler.java", "FormalArgument.java", "STException.java", "STLexer.java", "StringTable.java", "DateRenderer.java", "AddAttributeEvent.java", "ConstructionEvent.java", "EvalExprEvent.java", "EvalTemplateEvent.java", "IndentEvent.java", "InterpEvent.java", "JTreeASTModel.java", "JTreeScopeStackModel.java", "JTreeSTModel.java", "STViewFrame.java", "STViz.java", "InstanceScope.java", "Interpreter.java", "Aggregate.java", "AggregateModelAdaptor.java", "AmbiguousMatchException.java", "ArrayIterator.java", "Coordinate.java", "ErrorBuffer.java", "ErrorManager.java", "ErrorType.java", "Interval.java", "MapModelAdaptor.java", "Misc.java", "MultiMap.java", "ObjectModelAdaptor.java", "STCompiletimeMessage.java", "STGroupCompiletimeMessage.java", "STLexerMessage.java", "STMessage.java", "STModelAdaptor.java", "STNoSuchAttributeException.java", "STNoSuchPropertyException.java", "STRuntimeMessage.java", "TypeRegistry.java", "ModelAdaptor.java", "NoIndentWriter.java", "NumberRenderer.java", "ST.java", "STErrorListener.java", "STGroup.java", "STGroupDir.java", "STGroupFile.java", "STGroupString.java", "STRawGroupDir.java", "StringRenderer.java", "STWriter.java"]
N = len(labels)

featureIndexes = range(0,N)
java_self = [4.7169812E-4, 0.010682991, 0.030774673, 0.015425444, 0.018212788, 0.011839364, 0.22315025, 0.004068716, 5.7670125E-4, 0.07913579, 4.570384E-4, 0.17826189, 0.026627218, 0.008148484, 0.044106744, 5.5648305E-4, 5.488474E-4, 0.014310494, 0.005312085, 0.0069905003, 0.0140280565, 0.009339975, 0.14726113, 0.003618421, 0.045454547, 5.044136E-4, 9.4562647E-4, 4.9407117E-4, 0.0015841584, 0.004680187, 0.0012, 0.20679672, 2.6867277E-4, 0.0026925148, 0.0011406844, 0.015161332, 5.058169E-4, 0.010821779, 0.019834295, 0.026479242, 0.002074689, 0.0020057308, 5.0813006E-4, 9.3984965E-4, 4.6040516E-4, 0.0033589252, 2.962524E-4, 3.529827E-4, 5.3106743E-4, 4.0966817E-4, 0.033244736, 5.521811E-4, 0.04796313, 0.27274528, 0.27980536, 0.010881393, 0.02102564, 0.0028936581, 0.0013273162]
java_corpus = [4.7169812E-4, 0.0074420837, 0.05730555, 0.014927849, 0.01860587, 0.019037697, 0.1630972, 0.009515179, 0.011428571, 0.07614255, 0.0018281536, 0.18113913, 0.04978038, 0.016956715, 0.051149, 0.0011129661, 0.0010976949, 0.023850825, 0.0056459648, 0.0069905003, 0.016758544, 0.04622459, 0.16367762, 0.0042833607, 0.03665368, 0.0010088272, 0.0066193854, 0.0014822134, 0.001980198, 0.020502307, 0.0020008003, 0.22171724, 0.0037513399, 0.019587083, 0.008365019, 0.01314433, 5.058169E-4, 0.12530303, 0.024855636, 0.028113762, 0.0024204704, 0.006274957, 0.0071138213, 9.3984965E-4, 4.6040516E-4, 0.042396314, 0.01170197, 0.0038828098, 5.3106743E-4, 4.0966817E-4, 0.044466946, 5.521811E-4, 0.04315544, 0.045442607, 0.040566742, 0.010887316, 0.025128204, 0.007947977, 0.0029200956]
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
