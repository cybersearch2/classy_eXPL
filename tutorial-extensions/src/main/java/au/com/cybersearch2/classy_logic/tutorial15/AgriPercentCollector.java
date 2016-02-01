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
package au.com.cybersearch2.classy_logic.tutorial15;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.Query;

import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * AgriPercentCollector extends JpaEntityCollector to create an external axiom source
 * for   axioms translated from    JPA entity objects. The data is
 * obtained from "" named query.

 * @author Andrew Bowley
 * 10 Feb 2015
 */
public class AgriPercentCollector extends JpaEntityCollector<YearPercent> 
{
    protected YearPercent yearPercent;
    protected Data fact;
    protected BeanMap beanMap;
    protected String currentCountry = "";
    protected Collection<YearPercent> yearPercentList;
    
	/**
	 * 
	 */
	public AgriPercentCollector(PersistenceWorker persistenceService) 
	{
		super(YearPercent.class, persistenceService);
		setUserTransactionMode(true);
    	setMaxResults(48 * 10); // Batch size 10
    	batchMode = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doTask(EntityManagerLite entityManager) 
	{
		if (namedJpaQuery == null)
            createSelectAllQuery("all_" + entityClass.getName());
		// Collect all year percent items 
        Query query = entityManager.createNamedQuery(namedJpaQuery);
        if (maxResults > 0)
        {
        	query.setMaxResults(maxResults);
        	query.setFirstResult(startPosition);
        }
        yearPercentList = (Collection<YearPercent>) query.getResultList();
	}
	
	@Override
	protected void processBatch()
	{
        startPosition += yearPercentList.size();
		//System.out.println("Size = " + yearPercentList.size() + ", Position = " + startPosition);
        // Collate into country list
        Iterator<YearPercent> iterator = yearPercentList.iterator();
        if (!iterator.hasNext())
        {
        	yearPercentList.clear();
        	if (moreExpected)
        	{
        		moreExpected = false;
    			if (data == null)
    				data = new ArrayList<Object>();
    			data.add(fact);
         	}
   			return;
        }
        while (iterator.hasNext())
        {
        	yearPercent = iterator.next();
        	String year = yearPercent.getYear();
        	String country = yearPercent.getCountry().getCountry();
        	if (!currentCountry.equals(country))
        	{
        		currentCountry = country;
        		if (fact != null)
        		{
        			if (data == null)
        				data = new ArrayList<Object>();
        			data.add(fact);
        		}
        		fact = new Data();
        		fact.setCountry(country);
        		beanMap = new BeanMap(fact);
        	}
        	// Sqlite does not support NaN. So use special value "-0.001" to indicate NaN
        	if (Double.valueOf(-0.001).equals(yearPercent.getPercent()))
        		yearPercent.setPercent(Double.NaN);
        	if ((year == null) || (yearPercent == null))
        	    System.out.println("Eureka!");
        	beanMap.put(year, yearPercent.getPercent());
        }
        yearPercentList.clear();
    	moreExpected = true;
	}

}
