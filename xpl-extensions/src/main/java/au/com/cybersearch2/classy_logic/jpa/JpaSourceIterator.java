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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.openbeans.PropertyDescriptor;

import au.com.cybersearch2.classy_logic.helper.Null;
import au.com.cybersearch2.classy_logic.interfaces.DataCollector;
import au.com.cybersearch2.classy_logic.operator.DelegateOperator;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classy_logic.terms.Parameter;
import au.com.cybersearch2.classybean.BeanUtil;

/**
 * JpaSourceIterator
 * Implements Iterator interface to marshall axioms fetched by data collector 
 * @author Andrew Bowley
 * 13 Feb 2015
 */
public class JpaSourceIterator implements Iterator<Axiom> 
{
	/** Name to use when creating axioms. Defaults to data object simple class name. */
	protected String axiomName;
	/** Axiom archetype */
	protected AxiomArchetype archetype;
	/** List of axiom term names. If not supplied, the term names come from data object field names */
	protected List<NameMap> termNameList;
	/** Executes JPA named queries to obtain data objects */
	protected DataCollector dataCollector;
	/** Data collection iterator */
	protected Iterator<Object> entityIterator;
    /** Flag set true if entity defines field names */
    protected boolean isEntityNameMap;

	/**
	 * Constructs JpaSourceIterator object which builds axioms according to given specifications
	 * @param dataCollector Executes JPA named queries to obtain data objects
	 * @param axiomName ame to use when creating axioms
	 * @param termNameList List of axiom term names
	 */
	public JpaSourceIterator(DataCollector dataCollector, AxiomArchetype archetype, List<NameMap> termNameList)
	{
		this.dataCollector = dataCollector;
		this.archetype = archetype;
		this.termNameList = termNameList;
        isEntityNameMap = archetype.getTermCount() == 0;

    }

    /**
     * hasNext
     * @see java.util.Iterator#hasNext()
     */
	@Override
	public boolean hasNext() 
	{   // Ensure entity iterator is ready to navigate database table
		if ((entityIterator== null) || (!entityIterator.hasNext() && dataCollector.isMoreExpected()))
			entityIterator = dataCollector.getData().iterator();
		return entityIterator.hasNext();
	}

	/**
	 * next
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Axiom next() 
	{   // Don't assume hasNext() has been called prior
		if ((entityIterator== null) && !hasNext())
			return null;
		// next() starts here
		Object entity = entityIterator.next();
		return getAxiomFromEntity(entity);
	}

	/**
	 * Returns Axiom marshalled from entity object.
	 * Normally, termNameList is provided to specify which terms to include
	 * and how to order them. Field names are matched to term names case insensitive.
	 * If no termNameList provided, terms are added for fields of type known to XPL.
	 * This may suffice for simple cases.
	 * @param entity Object
	 * @return Axiom object
	 */
	protected Axiom getAxiomFromEntity(Object entity)
	{
		// Use Bean utilities to access entity object fields
		PropertyDescriptor[] descriptors = BeanUtil.getBeanInfo(entity).getPropertyDescriptors();
        ArrayList<Parameter> paramList = new ArrayList<Parameter>(descriptors.length);
		if (isEntityNameMap)
		    createNameMap(entity, descriptors, paramList);
		else
    		for (PropertyDescriptor descriptor: descriptors)
    		{
    			String termName = null;
                String key = descriptor.getName();
                Object value = null;
    			for (NameMap nameMap: termNameList)
    			{
    				if (nameMap.getFieldName().equalsIgnoreCase(key))
    				{
    					termName = nameMap.getTermName();
    		            value = invoke(entity, descriptor); 
    		            assignItem(paramList, nameMap.getPosition(), new Parameter(termName, value == null ? new Null() : value));
    					break;
    				}
    			}
    		}
        Axiom axiom = new Axiom(archetype);
		for (Parameter param: paramList)
			axiom.addTerm(param);
        if (isEntityNameMap)
        {
            isEntityNameMap = false;
            archetype.clearMutable();
        }
		return axiom;
	}

	private void createNameMap(Object entity, PropertyDescriptor[] descriptors, ArrayList<Parameter> paramList)
    {
        List<NameMap> newTermNameList = new ArrayList<NameMap>();
        for (PropertyDescriptor descriptor: descriptors)
        {
            String termName = null;
            String key = descriptor.getName();
            Object value = invoke(entity, descriptor);
            if ((value == null) || !DelegateOperator.isDelegateClass(value.getClass()))
                continue;
            termName = key;
            Iterator<NameMap> iterator = termNameList.iterator();
            int position = -1;
            while (iterator.hasNext())
            {
                NameMap nameMap = iterator.next();
                if (nameMap.getFieldName().equalsIgnoreCase(key))
                {
                    termName = nameMap.getTermName();
                    position = nameMap.getPosition();
                    break;
                }
            }
            if (position == -1)
                continue;
            Parameter param = new Parameter(termName, value == null ? new Null() : value);
            assignItem(paramList, position, param);
            NameMap nameMap = new NameMap(termName, key);
            nameMap.setPosition(position);
            newTermNameList.add(nameMap);
        }
        termNameList = newTermNameList;
    }

    /**
	 * Not implemented
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() 
	{
	}

    /**
     * Invoke getter
     *@param property Bean PropertyDescriptor
     *@return Object returned by getter
     */
    protected Object invoke(Object bean, PropertyDescriptor property) 
    {
        Method method = property.getReadMethod();
        if (method == null)
            return null; // No getter defined if method == null
        return BeanUtil.invoke(method, bean, BeanUtil.NO_ARGS);
    }

    /**
     * Add parameter to list in specified position
     * @param paramList Parameter list
     * @param index Position
     * @param value The parameter
     */
	public void assignItem(ArrayList<Parameter> paramList, int index, Parameter value) 
	{
		if (index < paramList.size())
			paramList.set(index, value);
		else
		{
			paramList.ensureCapacity(index + 1);
			for (int i = paramList.size(); i < index; i++)
				paramList.add(null);
			paramList.add(index, value);
		}
	}
}
