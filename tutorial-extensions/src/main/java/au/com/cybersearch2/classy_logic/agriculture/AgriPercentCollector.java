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
package au.com.cybersearch2.classy_logic.agriculture;


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
 * for axioms translated from JPA entity objects. The data is obtained from "" named query.
 * @author Andrew Bowley
 * 10 Feb 2015
 */
public class AgriPercentCollector extends JpaEntityCollector<AgriAreaPercent> 
{
    protected Collection<AgriAreaPercent> yearPercentList;
    
	/**
	 * 
	 */
	public AgriPercentCollector(PersistenceWorker persistenceWorker) 
	{
		super(AgriAreaPercent.class, persistenceWorker);
		setUserTransactionMode(true);
    	setMaxResults(10); // Batch size 10
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
        yearPercentList = (Collection<AgriAreaPercent>) query.getResultList();
	}
	
	@Override
	protected void processBatch()
	{
        startPosition += yearPercentList.size();
		//System.out.println("Size = " + yearPercentList.size() + ", Position = " + startPosition);
        // Collate into country list
        Iterator<AgriAreaPercent> iterator = yearPercentList.iterator();
        if (!iterator.hasNext())
        {
        	yearPercentList.clear();
        	moreExpected = false;
   			return;
        }
        while (iterator.hasNext())
        {
            AgriAreaPercent agriAreaPercent = iterator.next();
            BeanMap beanMap = new BeanMap(agriAreaPercent);
            for (int year = 1962; year <= 2010; year++)
            {
                String yearName = "y" + year;
                Double percent = (Double) beanMap.get(yearName);
                // Sqlite does not support NaN. So use special value "-0.001" to indicate NaN
                if (Double.valueOf(-0.001).equals(percent))
                    beanMap.put(yearName, Double.NaN);
            }
            if (data == null)
                data = new ArrayList<Object>();
            data.add(agriAreaPercent);
        }
        yearPercentList.clear();
    	moreExpected = true;
	}

}
