package org.antlr.codebuff.gui;

import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.codebuff.Trainer;
import org.antlr.codebuff.validation.TokenPositionAnalysis;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.util.List;

import static javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class GUIController {
	public BuffScope scope;
	public List<TokenPositionAnalysis> analysisPerToken;
	public InputDocument testDoc;
	public String formattedText;
	public Class<? extends Lexer> lexerClass;
	public CommonTokenStream original_tokens;
	public CommonTokenStream formatted_tokens;

	public List<Token> realFormattedTokens;

	public GUIController(List<TokenPositionAnalysis> analysisPerToken,
	                     InputDocument testDoc,
	                     String formattedText,
	                     Class<? extends Lexer> lexerClass)
	{
		this.analysisPerToken = analysisPerToken;
		this.formattedText = formattedText;
		this.lexerClass = lexerClass;
		this.testDoc = testDoc;
		this.scope = new BuffScope();
	}

	public void show() throws Exception {
		// tokenize so we can highlight tokens
		String originalText = testDoc.content;
		original_tokens = Tool.tokenize(originalText, lexerClass);
		formatted_tokens = Tool.tokenize(formattedText, lexerClass);

		realFormattedTokens = Trainer.getRealTokens(formatted_tokens);

		// show spaces as dots
//		originalText = originalText.replace(' ','\u00B7');
//		formattedText = formattedText.replace(' ','\u00B7');

		// set text and font
		Font docFont = new Font("Monaco", Font.PLAIN, 14);
		scope.getOrigTextPane().putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.getOrigTextPane().setFont(docFont);
		scope.getFormattedTextPane().putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.getFormattedTextPane().setFont(docFont);
		scope.getOrigTextPane().setText(originalText);
		scope.getFormattedTextPane().setText(formattedText);

		scope.injectNLConsole.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.injectNLConsole.setFont(docFont);
		scope.alignConsole.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.alignConsole.setFont(docFont);

		JFrame frame = new JFrame("CodeBuff Scope");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(scope.$$$getRootComponent$$$(), BorderLayout.CENTER);

		scope.getFormattedTextPane().addCaretListener(new HighlightTokenListener());

		frame.pack();
		frame.setVisible(true);
	}

//	public static Token getTokenAtCharIndex(List<Token> tokens, int index) {
//		for (int i=0; i<tokens.size(); i++) {
//			Token t = tokens.get(i);
//			if ( index>=t.getStartIndex() && index<=t.getStopIndex() ) {
//				return t;
//			}
//		}
//		return null;
//	}

	public TokenPositionAnalysis getAnalysisForCharIndex(int charIndex) {
		for (TokenPositionAnalysis ta : analysisPerToken) {
			if ( ta!=null && charIndex>=ta.charIndexStart && charIndex<=ta.charIndexStop ) {
				return ta;
			}
		}
		return null;
	}

	class HighlightTokenListener implements CaretListener {
		@Override
		public void caretUpdate(CaretEvent e) {
			int cursor = e.getDot();
			JTextPane textPane = (JTextPane)e.getSource();
			TokenPositionAnalysis analysis = getAnalysisForCharIndex(cursor);
			Highlighter highlighter = textPane.getHighlighter();
			HighlightPainter painter = new DefaultHighlightPainter(Color.orange);
			try {
				highlighter.removeAllHighlights();
				if ( analysis!=null ) {
					highlighter.addHighlight(analysis.charIndexStart, analysis.charIndexStop+1, painter);
				}
				scope.injectNLConsole.setText(analysis!=null ? analysis.wsAnalysis : "");
				scope.injectNLConsole.setCaretPosition(0);
				scope.alignConsole.setText(analysis!=null ? analysis.alignAnalysis : "");
				scope.alignConsole.setCaretPosition(0);
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
}
