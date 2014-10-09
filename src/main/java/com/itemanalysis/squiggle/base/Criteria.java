package com.itemanalysis.squiggle.base;

import java.util.Set;

import com.itemanalysis.squiggle.output.Output;
import com.itemanalysis.squiggle.output.Outputable;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public abstract class Criteria implements Outputable {
    public abstract void write(Output out);
	public abstract void addReferencedTablesTo(Set<Table> tables);
}
