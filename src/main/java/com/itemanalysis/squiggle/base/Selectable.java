package com.itemanalysis.squiggle.base;

import java.util.Set;

import com.itemanalysis.squiggle.output.Outputable;

/**
 * Something that can be returned from a select query
 * 
 * @author Nat Pryce
 */
public interface Selectable extends Outputable {
	void addReferencedTablesTo(Set<Table> tables);
}
