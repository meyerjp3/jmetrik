package com.itemanalysis.squiggle.base;

import java.util.Set;

import com.itemanalysis.squiggle.output.Output;

public class Parameter implements Matchable {
	public void write(Output out) {
		out.print("?");
	}

	public void addReferencedTablesTo(Set<Table> tables) {
	}
}
