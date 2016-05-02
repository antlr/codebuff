package org.antlr.codebuff;

import org.antlr.codebuff.validation.DropAlignFeatures;
import org.antlr.codebuff.validation.DropWSFeatures;
import org.antlr.codebuff.validation.LeaveOneOutValidator;
import org.antlr.codebuff.validation.SubsetValidator;
import org.antlr.codebuff.validation.TestK;
import org.junit.Test;

public class GeneratePythonForGraphs {
	@Test
	public void genLeaveOneOutValidation() throws Exception {
		LeaveOneOutValidator.main(null);
	}

	@Test
	public void genCorpusSizeVsErrorRate() throws Exception {
		SubsetValidator.main(null);
	}

	@Test
	public void genDropWSFeatures() throws Exception {
		DropWSFeatures.main(null);
	}

	@Test
	public void genDropAlignFeatures() throws Exception {
		DropAlignFeatures.main(null);
	}

	@Test
	public void genIncreaseK() throws Exception {
		TestK.main(null);
	}
}
