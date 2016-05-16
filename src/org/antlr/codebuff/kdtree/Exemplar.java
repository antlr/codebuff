/*
 * Copyright 2001-2005 Daniel F. Savarese
 * Copyright 2006-2009 Savarese Software Research Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.savarese.com/software/ApacheLicense-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antlr.codebuff.kdtree;

import org.antlr.codebuff.Corpus;
import org.antlr.codebuff.Trainer;

import java.io.File;
import java.util.Arrays;

/**
 * The Point interface represents a point in a k-dimensional space.
 * It is used to specify point keys that index into spatial data
 * structures.
 */
public class Exemplar extends GenericPoint {
	public final Corpus corpus;
	public final int[] features; // a copy of super.__coordinates
	public final int index; // global index into corpus exemplar list

	public Exemplar(Corpus corpus, int[] features, int index) {
		super(features.length);
		this.corpus = corpus;
		this.features = features;
		this.index = index;
		for (int i = 0; i<features.length; i++) {
			setCoord(i, features[i]);
		}
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

	@Override
	public String toString() {
		String fileName = corpus.documentsPerExemplar.get(index).fileName;
		return new File(fileName).getName()+": "+Arrays.toString(features)+"->"+corpus.injectWhitespace.get(index)+", "+corpus.hpos.get(index);
	}

	/**
	 * Returns the value of the coordinate of the given dimension.
	 *
	 * @return The value of the coordinate of the given dimension.
	 * @throws IllegalArgumentException if the Point does not
	 *                                  support the dimension.
	 */
	public int getCoord(int dimension) { return features[dimension]; }

	/**
	 * Returns the number of dimensions in the point.
	 *
	 * @return The number of dimensions in the point.
	 */
	public int getDimensions() {
		return Trainer.NUM_FEATURES;
	}
}
