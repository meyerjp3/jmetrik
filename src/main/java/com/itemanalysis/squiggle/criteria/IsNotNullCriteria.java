package com.itemanalysis.squiggle.criteria;

import java.util.Set;

import com.itemanalysis.squiggle.base.Criteria;
import com.itemanalysis.squiggle.base.Matchable;
import com.itemanalysis.squiggle.base.Table;
import com.itemanalysis.squiggle.output.Output;

public class IsNotNullCriteria extends Criteria {
	private final Matchable matched;
	
	public IsNotNullCriteria(Matchable matched) {
		this.matched = matched;
	}

	@Override
	public void write(Output out) {
		matched.write(out);
		out.print(" IS NOT NULL");
	}

	public void addReferencedTablesTo(Set<Table> tables) {
		matched.addReferencedTablesTo(tables);
	}
}
