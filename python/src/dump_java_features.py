import sys
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from antlr4.tree.Trees import Trees
from CollectTokenFeatures import CollectTokenFeatures
from sklearn.feature_extraction import DictVectorizer

sample = \
"""
public class T {
    int i;
    int j;
    int[] a = {
        1,2,3,
        4,5,6
    };
    void foo() { int j; j=i+10; }
}
"""

def cvt_dummy_variables(token_features):
    for record in token_features.data:
        d = dict((CollectTokenFeatures.feature_names[i], record[i]) for i in range(0, len(CollectTokenFeatures.feature_names)))
        dv = DictVectorizer(sparse=False)
        print dv.fit_transform(d)
        print dv.get_feature_names()


def main(argv):
    if len(argv)>1:
        input = FileStream(argv[1])
    else:
        input = InputStream(sample)
    lexer = JavaLexer(input)
    stream = CommonTokenStream(lexer)
    parser = JavaParser(stream)
    tree = parser.compilationUnit()
    # print(Trees.toStringTree(tree, None, parser))
    print "Grammar %s has %d rules, %d tokens" % (parser.grammarFileName, len(parser.ruleNames), len(lexer.ruleNames))
    print ', '.join(CollectTokenFeatures.feature_names)
    token_features = CollectTokenFeatures(stream)
    walker = ParseTreeWalker()
    walker.walk(token_features, tree)
    print token_features.feature_names

if __name__ == '__main__':
    main(sys.argv)