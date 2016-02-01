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

import au.com.cybersearch2.classy_logic.expression.DelegateOperand;
import au.com.cybersearch2.classy_logic.helper.Null;
import au.com.cybersearch2.classy_logic.interfaces.DataCollector;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.terms.Parameter;
import au.com.cybersearch2.classybean.BeanUtil;

/**
 * JpaSourceIterator
 * @author Andrew Bowley
 * 13 Feb 2015
 */
public class JpaSourceIterator implements Iterator<Axiom> 
{
	/** Name to use when creating axioms. Defaults to data object simple class name. */
	protected String axiomName;
	/** List of axiom term names. If not supplied, the term names come from data object field names */
	protected List<NameMap> termNameList;
	/** Executes JPA named queries to obtain data objects */
	protected DataCollector dataCollector;
	/** Data collection iterator */
	protected Iterator<Object> entityIterator;

	/**
	 * Constructs JpaSourceIterator object which builds axioms according to given specifications
	 * @param dataCollector Executes JPA named queries to obtain data objects
	 * @param axiomName ame to use when creating axioms
	 * @param termNameList List of axiom term names
	 */
	public JpaSourceIterator(DataCollector dataCollector, String axiomName, List<NameMap> termNameList) 
	{
		this.dataCollector = dataCollector;
		this.axiomName = axiomName;
		this.termNameList = termNameList;
    }

    /**
     * 
     * @see java.util.Iterator#hasNext()
     */
	@Override
	public boolean hasNext() 
	{
		if ((entityIterator== null) || (!entityIterator.hasNext() && dataCollector.isMoreExpected()))
			entityIterator = dataCollector.getData().iterator();
		return entityIterator.hasNext();
	}

	/**
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Axiom next() 
	{   // Don't assume hasNext() has been called prior
		if ((entityIterator== null) && ! hasNext())
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
		// Create new axiom
		Axiom axiom = new Axiom(axiomName);
		// Use Bean utilities to access entity object fields
		PropertyDescriptor[] descriptors = BeanUtil.getBeanInfo(entity).getPropertyDescriptors();
		// Special case for identity column, assuming name is "id" by convention
		Parameter id = null;
		ArrayList<Parameter> paramList = new ArrayList<Parameter>(descriptors.length);
		for (PropertyDescriptor descriptor: descriptors)
		{
			String termName = null;
			int termIndex = 0;
            String key = descriptor.getName();
            Object value = null;
			if ((termNameList != null) && !termNameList.isEmpty())
			{
				for (NameMap nameMap: termNameList)
				{
					if (nameMap.getFieldName().equalsIgnoreCase(key))
					{
						termName = nameMap.getTermName();
			            value = invoke(entity, descriptor); 
			            assignItem(paramList, termIndex, new Parameter(termName, value == null ? new Null() : value));
						break;
					}
					++termIndex;
				}
			}
			else 
			{
                value = invoke(entity, descriptor); 
			    if ((value != null) && DelegateOperand.isDelegateClass(value.getClass()))
			    {
                    // By default, all fields of expression-capable type are added as terms to the axiom
    				if ("id".equals(key))
    					id = new Parameter(key, value);
    				else 
    					paramList.add(new Parameter(key, value));
			    }
			}
		}
		if (id != null)
			axiom.addTerm(id);
		int index = 0;
		for (Parameter param: paramList)
		{   // When axiom specifications are provided, some param values may end up null
			if (param != null)
				axiom.addTerm(param);
			else
				axiom.addTerm(new Parameter(termNameList.get(index).getTermName(), new Null()));
			++index;
		}
		return axiom;
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
