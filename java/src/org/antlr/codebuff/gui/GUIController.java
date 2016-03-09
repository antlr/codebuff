package org.antlr.codebuff.gui;

import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.Tool;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class GUIController {
	public BuffScope scope;
	public InputDocument testDoc;
	public String formattedText;
	public Class<? extends Lexer> lexerClass;
	public CommonTokenStream original_tokens;
	public CommonTokenStream formatted_tokens;

	public GUIController(InputDocument testDoc, String formattedText, Class<? extends Lexer> lexerClass) {
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

		// show spaces as dots
		originalText = originalText.replace(' ','\u00B7');
		formattedText = formattedText.replace(' ','\u00B7');

		// set text
		Font docFont = new Font("Monaco", Font.PLAIN, 14);
		scope.getOrigTextPane().putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.getOrigTextPane().setFont(docFont);
		scope.getFormattedTextPane().putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		scope.getFormattedTextPane().setFont(docFont);
		scope.getOrigTextPane().setText(originalText);
		scope.getFormattedTextPane().setText(formattedText);

		JFrame frame = new JFrame("CodeBuff Scope");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.getContentPane().add(scope.$$$getRootComponent$$$(), BorderLayout.CENTER);

		scope.getFormattedTextPane().addCaretListener(new HighlightTokenListener());

		frame.pack();
		frame.setVisible(true);
	}

	public static Token getTokenAtCharIndex(CommonTokenStream tokens, int index) {
		for (int i=0; i<tokens.size(); i++) {
			Token t = tokens.get(i);
			if ( index>=t.getStartIndex() && index<=t.getStopIndex() ) {
				return t;
			}
		}
		return null;
	}

	class HighlightTokenListener implements CaretListener {
		@Override
		public void caretUpdate(CaretEvent e) {
			int cursor = e.getDot();
			JTextPane textPane = (JTextPane)e.getSource();
			Token t = getTokenAtCharIndex(formatted_tokens, cursor);
			Highlighter highlighter = textPane.getHighlighter();
			HighlightPainter painter = new DefaultHighlightPainter(Color.orange);
			try {
				highlighter.removeAllHighlights();
				if ( t!=null ) {
					highlighter.addHighlight(t.getStartIndex(), t.getStopIndex()+1, painter);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
}
