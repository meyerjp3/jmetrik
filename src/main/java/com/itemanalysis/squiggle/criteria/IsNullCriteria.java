package com.itemanalysis.squiggle.criteria;

import java.util.Set;

import com.itemanalysis.squiggle.base.Criteria;
import com.itemanalysis.squiggle.base.Matchable;
import com.itemanalysis.squiggle.base.Table;
import com.itemanalysis.squiggle.output.Output;

public class IsNullCriteria extends Criteria {
	private final Matchable matched;
	
	public IsNullCriteria(Matchable matched) {
		this.matched = matched;
	}

	@Override
	public void write(Output out) {
		matched.write(out);
		out.print(" IS NULL");
	}

	public void addReferencedTablesTo(Set<Table> tables) {
		matched.addReferencedTablesTo(tables);
	}
}
