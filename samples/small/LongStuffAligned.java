public class T {
    public TokenPositionAnalysis getTokenAnalysis(int[] features,
												  int indexIntoRealTokens, 
												  int tokenIndexInStream,
												  int injectNewline,
												  int alignWithPrevious,
												  int indent,
												  int ws) {
        CommonToken curToken = foo;
        CommonToken prevToken = 1;
        if ( ws==0 && cannotJoin(realTokens.get(indexIntoRealTokens-1), curToken) ) {
            ws = 1;
            failsafeTriggered = true;
        }
    }
    public static boolean cannotJoin(Token prevToken, Token curToken) {
        String prevTokenText = prevToken.getText();
        char prevLastChar = prevTokenText.charAt(prevTokenText.length()-1);
        String curTokenText = curToken.getText();
        char curFirstChar = curTokenText.charAt(0);
        return Character.isLetterOrDigit(prevLastChar) &&
			   Character.isLetterOrDigit(curFirstChar);
    }
}
