import sys
from antlr4 import *
from JavaLexer import JavaLexer
from JavaParser import JavaParser
from antlr4.tree.Trees import Trees
from DumpFeatures import DumpFeatures

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
    print "inject newline, token type, column, length, enclosing rule, earliest ancestor rule, " \
          "earliest ancestor length, prev token type, prev token column, prev token last char index"
    features = DumpFeatures(stream)
    walker = ParseTreeWalker()
    walker.walk(features, tree)

if __name__ == '__main__':
    main(sys.argv)