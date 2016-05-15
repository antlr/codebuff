/*
 *  Copyright (C) 2010 Duy Nguyen <duyn.ng@gmail.com>.
 */

package org.antlr.codebuff.kdtree;

import org.antlr.codebuff.Corpus;

import java.io.File;
import java.util.Arrays;

/**
 * A sample point in multi-dimensional space. Needed because each sample
 * may contain an arbitrary payload.
 *
 * @author duyn
 */
public class Exemplar {
	public final Corpus corpus;
	public final int[] features;
	public final int index; // global index into corpus exemplar list

	public Exemplar(Corpus corpus, int[] features, int index) {
		this.corpus = corpus;
		this.features = features;
		this.index = index;
	}

	//	public final InputDocument doc;
//	public final int ws;
//	public final int hpos;
//
//	public Exemplar(InputDocument doc, int[] features, int ws, int hpos) {
//		this.doc = doc;
//		this.features = features;
//		this.ws = ws;
//		this.hpos = hpos;
//	}

	public final boolean collocated(final Exemplar other) {
		return Arrays.equals(features, other.features);
	}

	@Override
	public String toString() {
		String fileName = corpus.documentsPerExemplar.get(index).fileName;
		return new File(fileName).getName()+": "+Arrays.toString(features)+"->"+corpus.injectWhitespace.get(index)+", "+corpus.hpos.get(index);
	}
}
