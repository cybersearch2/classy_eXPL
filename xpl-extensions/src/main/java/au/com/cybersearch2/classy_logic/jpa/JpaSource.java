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
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.interfaces.DataCollector;
import au.com.cybersearch2.classy_logic.interfaces.Term;
import au.com.cybersearch2.classy_logic.pattern.Archetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;

/**
 * JpaSource
 * AxiomSource JPA implementation 
 * @author Andrew Bowley
 * 8 Feb 2015
 */
public class JpaSource implements AxiomSource
{
    
	/** Name to use when creating axioms. Defaults to data object simple class name. */
	protected AxiomArchetype archetype;
	/** List of axiom term names. If not supplied, the term names come from data object field names */
	protected List<NameMap> termNameList;
	/** Executes JPA named queries to obtain data objects */
	protected DataCollector dataCollector;

	/**
	 * Constructs JpaSource object which builds axioms according to given specifications
	 * @param dataCollector Executes JPA named queries to obtain data objects
	 * @param archetype Axiom archetype
	 */
	public JpaSource(DataCollector dataCollector, AxiomArchetype archetype) 
	{
		this.dataCollector = dataCollector;
		this.archetype = archetype;
		termNameList = new ArrayList<NameMap>();
        if (archetype.getTermCount() > 0)
        {
            for (int index = 0; index < archetype.getTermCount(); ++index)
            {
                String termName = archetype.getMetaData(index).getName();
                NameMap nameMap = new NameMap(termName, termName);
                nameMap.setPosition(index);
                termNameList.add(nameMap);
            }
            lockArchetype();
        }
    }

    /**
     * Constructs JpaSource object which builds axioms according to given specifications
     * @param dataCollector Executes JPA named queries to obtain data objects
     * @param archetype Axiom archetype
     * @param termNameList List of axiom term names
     */
    public JpaSource(DataCollector dataCollector, AxiomArchetype archetype, List<NameMap> termNameList) 
    {
        this(dataCollector, archetype);
        if ((termNameList != null) && !termNameList.isEmpty())
        {
            if (this.termNameList.isEmpty())
                 this.termNameList = termNameList;
            else
            {
                Iterator<NameMap> iterator = this.termNameList.iterator();
                while (iterator.hasNext())
                {
                    NameMap nameMap = iterator.next();
                    Iterator<NameMap> iterator2 = termNameList.iterator();
                    while (iterator2.hasNext())
                    {
                        NameMap nameMap2 = iterator2.next();
                        if (nameMap2.getTermName().equalsIgnoreCase(nameMap.getTermName()))
                        {
                            nameMap.setFieldName(nameMap2.getFieldName());
                            break;
                        }
                    }
                }
            }
        }
        int index = 0;
        for (NameMap nameMap: termNameList)
            nameMap.setPosition(index++);
    }
    
	/**
	 * Returns Axiom iterator
	 * @see au.com.cybersearch2.classy_logic.interfaces.AxiomSource#iterator()
	 */
	@Override
	public Iterator<Axiom> iterator() 
	{
		return new JpaSourceIterator(dataCollector, archetype, termNameList);
	}

    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        return archetype;
    }

    /**
     * Lock archetype - override if archetype needs modification before being locked
     */
    protected void lockArchetype()
    {
        archetype.clearMutable();
    }

}
