package com.itemanalysis.squiggle.literal;

import java.math.BigDecimal;

/**
 * @author Nat Pryce
 */
public class BigDecimalLiteral extends LiteralWithSameRepresentationInJavaAndSql {
	public BigDecimalLiteral(BigDecimal literalValue) {
		super(literalValue);
	}
}
