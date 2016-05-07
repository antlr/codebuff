package org.antlr.codebuff.validation;

/** Walk corpus and group feature vectors and then consider the
 *  diversity index / entropy of the predicted categories. This
 *  is a measure of how consistent a corpus is.  If each specific
 *  context predicts many different categories and mostly at
 *  same likelihood, entropy is high meaning one or both of:
 *
 *  a) model does not distinguish between contexts well enough
 *  b) corpus is highly inconsistent in formatting
 *
 *  To tease out (a) and (b), we can train on a single file
 *  and measure entropy. That should tell us how much the model captures
 *  assuming a single file is internally consistent (not always true;
 *  in principle, we could do divide up files into regions such as methods.)
 */
public class MeasureEntropy {
}
