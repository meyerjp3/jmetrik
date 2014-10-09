package com.itemanalysis.squiggle.base;


import com.itemanalysis.squiggle.output.Output;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public class Column extends Projection implements Matchable {
	private final String name;
	
    public Column(Table table, String name) {
        super(table);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void write(Output out) {
        out.print(getTable().getAlias()).print('.').print(getName());
    }

	public int hashCode() {
		final int prime = 31;
		int result = getTable().hashCode();
		result = prime * result + name.hashCode();
		return result;
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
		final Column that = (Column)o;
		
		return this.name.equals(that.name) 
		    && this.getTable().equals(that.getTable()); 
	}
}
