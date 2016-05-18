package org.antlr.codebuff;

import org.antlr.codebuff.validation.AllJavaLeaveOneOutValidation;
import org.antlr.codebuff.validation.AllSQLLeaveOneOutValidation;
import org.antlr.codebuff.validation.DropAlignFeatures;
import org.antlr.codebuff.validation.DropWSFeatures;
import org.antlr.codebuff.validation.LeaveOneOutValidator;
import org.antlr.codebuff.validation.OneFileCapture;
import org.antlr.codebuff.validation.SubsetValidator;
import org.antlr.codebuff.validation.TestK;
import org.junit.Test;

public class TestGeneratePythonForGraphs {
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

	@Test
	public void genOneFileCapture() throws Exception {
		OneFileCapture.main(null);
	}

	@Test
	public void genAllJava() throws Exception {
		AllJavaLeaveOneOutValidation.main(null);
	}

	@Test
	public void genAllSQL() throws Exception {
		AllSQLLeaveOneOutValidation.main(null);
	}
}
