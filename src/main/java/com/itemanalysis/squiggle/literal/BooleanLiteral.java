package com.itemanalysis.squiggle.literal;

/**
 * @author Nat Pryce
 */
public class BooleanLiteral extends LiteralWithSameRepresentationInJavaAndSql {
	public static BooleanLiteral TRUE = new BooleanLiteral(true);
	public static BooleanLiteral FALSE = new BooleanLiteral(false);
	
	public BooleanLiteral(boolean literalValue) {
		this(Boolean.valueOf(literalValue));
	}
	
	public BooleanLiteral(Boolean literalValue) {
		super(literalValue);
	}
}
