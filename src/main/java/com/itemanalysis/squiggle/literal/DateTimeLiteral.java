package com.itemanalysis.squiggle.literal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeLiteral extends StringLiteral {
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss.S";

	public DateTimeLiteral(Date literalValue) {
        super(new SimpleDateFormat(FORMAT).format(literalValue));
	}
}