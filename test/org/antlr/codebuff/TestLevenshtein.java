package org.antlr.codebuff;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLevenshtein {
	@Test
	public void simple() {
		float d = Dbg.normalizedLevenshteinDistance("abc", "abd");
		assertEquals(1.0/3.0, d, 0.00001);

		d = Dbg.normalizedLevenshteinDistance("kitten", "sitten");
		assertEquals(1.0/"kitten".length(), d, 0.00001);

		d = Dbg.normalizedLevenshteinDistance("sittin", "sitting");
		assertEquals(1.0/"sitting".length(), d, 0.00001);
	}
}
