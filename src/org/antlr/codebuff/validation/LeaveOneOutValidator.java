package org.antlr.codebuff.validation;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Formatter;
import org.antlr.codebuff.InputDocument;
import org.antlr.codebuff.misc.LangDescriptor;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.antlr.codebuff.Tool.getFilenames;
import static org.antlr.codebuff.Tool.load;
import static org.antlr.codebuff.misc.BuffUtils.filter;

public class LeaveOneOutValidator {
	public static final int DOCLIST_RANDOM_SEED = 951413; // need randomness but use same seed to get reproducibility

	public static final String outputDir = "/tmp";

	final Random random = new Random();

	public String rootDir;
	public LangDescriptor language;

	public List<InputDocument> documents;

	public LeaveOneOutValidator(String rootDir, LangDescriptor language) {
		this.rootDir = rootDir;
		this.language = language;
		random.setSeed(DOCLIST_RANDOM_SEED);
	}

	public List<Float> validate(boolean saveOutput) throws Exception {
		File dir = new File(outputDir+"/"+language.name);
		if ( saveOutput ) {
			dir = new File(outputDir+"/"+language.name);
			dir.mkdir();
		}
		List<String> allFiles = getFilenames(new File(rootDir), language.fileRegex);
		documents = load(allFiles, language);
		List<Float> distances = new ArrayList<>();
		for (int i = 0; i<documents.size(); i++) {
			final int leaveOutIndex = i;
			InputDocument testDoc = documents.get(leaveOutIndex);
			List<InputDocument> others = filter(documents, f -> f.index!=leaveOutIndex);
			Corpus corpus = new Corpus(others, language);
			corpus.train();
			Formatter formatter = new Formatter(corpus);
			String output = formatter.format(testDoc, false);
			if ( saveOutput ) {
				Utils.writeFile(dir.getPath()+"/"+new File(testDoc.fileName).getName(), output);
			}
			float editDistance = formatter.getEditDistance();
			System.out.println(testDoc.fileName+": "+editDistance);
			distances.add(editDistance);
		}
		return distances;
	}

	/** From input documents, grab n in random order w/o replacement */
	public List<InputDocument> getRandomDocuments(List<InputDocument> documents, int n) {
		List<InputDocument> documents_ = new ArrayList<>(documents);
		Collections.shuffle(documents_, random);
		List<InputDocument> contentList = new ArrayList<>(n);
		for (int i=0; i<n; i++) { // get first n files from shuffle and set file index for it
			contentList.add(documents.get(i));
		}
		return contentList;
	}

	/** From input documents, grab n in random order w replacement */
	public List<InputDocument> getRandomDocumentsWithRepl(List<InputDocument> documents, int n) {
		List<InputDocument> contentList = new ArrayList<>(n);
		for (int i=1; i<=n; i++) {
			int r = random.nextInt(documents.size()); // get random index from 0..|inputfiles|-1
			contentList.add(documents.get(r));
		}
		return contentList;
	}
}
