public class T {
    public TokenPositionAnalysis getTokenAnalysis(int[] features, int indexIntoRealTokens, int tokenIndexInStream, int injectNewline, int alignWithPrevious, int indent, int ws) {
        CommonToken curToken = (CommonToken)tokens.get(tokenIndexInStream);
        CommonToken prevToken = originalTokens.get(curToken.getTokenIndex()-1);
        CommonToken originalCurToken = originalTokens.get(curToken.getTokenIndex());
        boolean failsafeTriggered = false;
        if ( ws==0 && cannotJoin(realTokens.get(indexIntoRealTokens-1), curToken) ) {
            ws = 1;
            failsafeTriggered = true;
        }

        boolean prevIsWS = prevToken.getType()== JavaLexer.WS;
        int actualNL = Tool.count(prevToken.getText(), '\n');
        int actualWS = Tool.count(prevToken.getText(), ' ');
        int actualIndent = originalCurToken.getCharPositionInLine()- 0;
        boolean actualAlign = isAlignedWithFirstSiblingOfList(tokenToNodeMap, tokens, curToken);
        String newlinePredictionString = String.format("### line %d: predicted %d \\n actual %s", originalCurToken.getLine(), injectNewline, prevIsWS? actualNL :"none");
        String alignPredictionString = String.format("### line %d: predicted %s actual %s", originalCurToken.getLine(), alignWithPrevious==1 ?"align" :
                                                                                                                        "unaligned", actualAlign?"align" :"unaligned");
        String indentPredictionString = String.format("### line %d: predicted indent %d actual %s", originalCurToken.getLine(), indent, actualIndent);
        String wsPredictionString = String.format("### line %d: predicted %d ' ' actual %s", originalCurToken.getLine(), ws, prevIsWS? actualWS :"none");
        if ( failsafeTriggered ) {
            wsPredictionString += " (failsafe triggered)";
        }
        String newlineAnalysis = newlinePredictionString+"\n"+newlineClassifier.getPredictionAnalysis(doc, k, features, corpus.injectNewlines, MAX_CONTEXT_DIFF_THRESHOLD);
        String alignAnalysis = alignPredictionString+"\n"+alignClassifier.getPredictionAnalysis(doc, k, features, corpus.align, MAX_CONTEXT_DIFF_THRESHOLD);
        String wsAnalysis = wsPredictionString+"\n"+wsClassifier.getPredictionAnalysis(doc, k, features, corpus.injectWS, MAX_CONTEXT_DIFF_THRESHOLD);
        return new TokenPositionAnalysis(newlineAnalysis, alignAnalysis, wsAnalysis);
    }
    public static boolean cannotJoin(Token prevToken, Token curToken) {
        String prevTokenText = prevToken.getText();
        char prevLastChar = prevTokenText.charAt(prevTokenText.length()-1);
        String curTokenText = curToken.getText();
        char curFirstChar = curTokenText.charAt(0);
        return Character.isLetterOrDigit(prevLastChar) && Character.isLetterOrDigit(curFirstChar);
    }
}
