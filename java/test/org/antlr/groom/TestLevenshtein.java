package org.antlr.groom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLevenshtein {
	@Test
	public void simple() {
		int d = Tool.levenshteinDistance("abc", "abd");
		assertEquals(1, d);

		d = Tool.levenshteinDistance("kitten", "sitten");
		assertEquals(1, d);

		d = Tool.levenshteinDistance("sittin", "sitting");
		assertEquals(1, d);
	}
}
