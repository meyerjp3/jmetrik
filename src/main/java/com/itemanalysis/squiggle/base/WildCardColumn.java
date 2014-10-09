package com.itemanalysis.squiggle.base;

import com.itemanalysis.squiggle.output.Output;

/**
 * Special column to represent For SELECT * FROM ...
 * 
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public class WildCardColumn extends Projection {
    public WildCardColumn(Table table) {
        super(table);
    }

	public void write(Output out) {
        out.print(getTable().getAlias()).print(".*");
	}
}
