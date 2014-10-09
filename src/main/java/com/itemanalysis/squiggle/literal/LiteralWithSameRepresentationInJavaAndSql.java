package com.itemanalysis.squiggle.literal;

import com.itemanalysis.squiggle.base.Literal;
import com.itemanalysis.squiggle.output.Output;

/**
 * @author Nat Pryce
 */
public abstract class LiteralWithSameRepresentationInJavaAndSql extends Literal {
	private final Object literalValue;

	protected LiteralWithSameRepresentationInJavaAndSql(Object literalValue) {
		this.literalValue = literalValue;
	}
	
	public void write(Output out) {
		out.print(literalValue);
	}
}
