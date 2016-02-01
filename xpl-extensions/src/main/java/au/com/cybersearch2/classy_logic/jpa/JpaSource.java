/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classy_logic.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.interfaces.DataCollector;
import au.com.cybersearch2.classy_logic.pattern.Axiom;

/**
 * JpaSource
 * AxiomSource JPA implementation 
 * @author Andrew Bowley
 * 8 Feb 2015
 */
public class JpaSource implements AxiomSource
{
    public static final List<String> EMPTY_LIST;
    
	/** Name to use when creating axioms. Defaults to data object simple class name. */
	protected String axiomName;
	/** List of axiom term names. If not supplied, the term names come from data object field names */
	protected List<NameMap> termNameList;
	/** Executes JPA named queries to obtain data objects */
	protected DataCollector dataCollector;
    /** The term names */
    protected List<String> axiomTermNameList;

    static
    {
        EMPTY_LIST = Collections.emptyList();
    }

	/**
	 * Constructs default JpaSource object.
	 * @param dataCollector Executes JPA named queries to obtain data objects
	 */
	public JpaSource(DataCollector dataCollector, String axiomName) 
	{
		this(dataCollector, axiomName, null);
	}

	/**
	 * Constructs JpaSource object which builds axioms according to given specifications
	 * @param dataCollector Executes JPA named queries to obtain data objects
	 * @param axiomName ame to use when creating axioms
	 * @param termNameList List of axiom term names
	 */
	public JpaSource(DataCollector dataCollector, String axiomName, List<NameMap> termNameList) 
	{
		this.dataCollector = dataCollector;
		this.axiomName = axiomName;
		this.termNameList = termNameList;
		if ((termNameList != null) && !termNameList.isEmpty())
		{
		    List<String> axiomTermNameList = new ArrayList<String>();
		    for (NameMap nameMap: termNameList)
		        axiomTermNameList.add(nameMap.getTermName());
		}
    }

	/**
	 * Returns Axiom iterator
	 * @see au.com.cybersearch2.classy_logic.interfaces.AxiomSource#iterator()
	 */
	@Override
	public Iterator<Axiom> iterator() 
	{
		return new JpaSourceIterator(dataCollector, axiomName, termNameList);
	}

    @Override
    public List<String> getAxiomTermNameList()
    {
        return axiomTermNameList == null ? EMPTY_LIST : axiomTermNameList;
    }

}
