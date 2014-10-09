package com.itemanalysis.squiggle.base;

import com.itemanalysis.squiggle.output.Output;
import com.itemanalysis.squiggle.output.Outputable;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public class Table implements Outputable {
    private final String name;
    private final String alias;

    public Table(String name) {
        this(name, null);
    }

    public Table(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    /**
     * Name of table
     */
    public String getName() {
        return name;
    }

    /**
     * Whether this table has an alias assigned.
     */
    private boolean hasAlias() {
        return alias != null;
    }

    /**
     * Short alias of table
     */
    public String getAlias() {
        return alias != null ? alias : name;
    }
    
    /**
     * Get a column for a particular table.
     */
    public Column getColumn(String columnName) {
        return new Column(this, columnName);
    }

    public WildCardColumn getWildcard() {
        return new WildCardColumn(this);
    }

    public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
        Table that = (Table)o;
        
		return getAlias().equals(that.getAlias());
    }
    
    public int hashCode() {
    	return getAlias().hashCode();
    }
    
    public void write(Output out) {
        out.print(getName());
        if (hasAlias()) {
            out.print(' ');
            out.print(getAlias());
        }
    }
}
