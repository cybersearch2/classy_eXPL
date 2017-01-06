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

import java.util.Collection;
import java.util.Collections;

import javax.persistence.Query;

import au.com.cybersearch2.classy_logic.interfaces.DataCollector;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * JpaEntityCollector
 * 
 * Base class for executing JPA queries to deliver data for axiom providers.
 * Performs persistence work to execute named query identified by field "namedJpaQuery".
 * However, method doInBackground() can be overriden to do something different.
 * Supports paging and this is enabled by setting "maxResults" field to a positive number.
 * @author Andrew Bowley
 * 10 Feb 2015
 */
public class JpaEntityCollector<E> implements DataCollector, PersistenceWork 
{
	/** Named query to be performed if doInBackground() is not overriden */
    protected String namedJpaQuery;
    /** List of objects to be translated into an axiom source */
    protected Collection<Object> data;
    /** Maximum number of objects to return from a single query */
    protected int maxResults;
    /** The start position of the first result, numbered from 0 */
    protected int startPosition;
    /** flag set true if a call to getData() may deliver more results */
    protected boolean moreExpected;
    /** Flag set true if processBatch() to be called after persistence work performed */
    protected boolean batchMode;
    /** Flag set true if user-controlled transactions */
    protected boolean userTransactionMode;

    /** Helper to perform persistence work */
    protected PersistenceWorker persistenceWorker;
    protected Class<E> entityClass;

    /**
     * Construct a JpaEntityCollector object for a specific entity class.
     * This maps all the class fields with a supported type to axiom terms
     * @param entityClass Class of entity to be collected
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
     */
    public JpaEntityCollector(Class<E> entityClass, PersistenceWorker persistenceService)
    {
        this.persistenceWorker = persistenceService;
        this.entityClass = entityClass;
    }
    
    /**
     * Set user transaction mode. The transaction is accessed by calling EntityManager getTransaction() method.
     * @param value boolean
     */
    public void setUserTransactionMode(boolean value)
    {
    	userTransactionMode = value;
    }
 
	/**
	 * Returns flag set true if a call to getData() may deliver more results.
	 * @return boolean
	 */
    @Override
	public boolean isMoreExpected() 
	{
		return moreExpected;
	}

    /**
     * Get data using JPA entity manager. Number of results can be limited by controlled by setting maxResults
     * @see #setMaxResults(int)
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
	@SuppressWarnings("unchecked")
	@Override
	public void doTask(EntityManagerLite entityManager) 
	{
		if (namedJpaQuery == null)
            createSelectAllQuery("all_" + entityClass.getName());
        Query query = entityManager.createNamedQuery(namedJpaQuery);
        if (maxResults > 0)
        {   // Paging enabled
        	query.setMaxResults(maxResults);
        	query.setFirstResult(startPosition);
        }
        data = (Collection<Object>) query.getResultList();
        if (maxResults > 0)
        {   // Advance start position or 
        	// clear "moreExpected" flag if no more results avaliable
        	if (data.size() > 0)
        	{
        		startPosition += data.size() + 1;
        		moreExpected = true;
        	}
        	else
        		moreExpected = false;
        }
	}

	public void createSelectAllQuery(String queryName) 
	{
		namedJpaQuery = queryName;
        PersistenceContext persistenceContext = persistenceWorker.getPersistenceContext();
        String persistenceUnit = persistenceWorker.getPersistenceUnit();
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(persistenceUnit);
        QueryForAllGenerator allEntitiesQuery = 
                new QueryForAllGenerator(persistenceAdmin);
        persistenceAdmin.addNamedQuery(entityClass, queryName, allEntitiesQuery);
	}

	/**
	 * 
	 * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
	 */
	@Override
	public void onPostExecute(boolean success) 
	{
        if (!success)
        {
        	moreExpected = false;
            if (data != null)
        	    data.clear();
        }
	}

	/**
	 * 
	 * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
	 */
	@Override
	public void onRollback(Throwable rollbackException) 
	{
    	moreExpected = false;
    	if (data != null)
    	    data.clear();
	}

	/**
	 * Returns list of objects from persistence system. 
	 * Note calling thread may be blocked waiting for results
	 * @return Object collection
	 */
	@Override
	public Collection<Object> getData() 
	{
		WorkStatus status = null;
		try 
		{
			status = persistenceWorker.doWork(this).waitForTask();
		} 
		catch (InterruptedException e) 
		{
			return Collections.emptyList();
		}
		if (status != WorkStatus.FINISHED)
			throw new QueryExecutionException("Error fetching axioms from persistence unit " + persistenceWorker.getPersistenceUnit());
		if (batchMode)
			processBatch();
		if (data == null)
			return Collections.emptyList();
		else
		{
			Collection<Object> result = data;
			data = null;
			return result;
		}
	}

	/**
	 * Override and set batchMode true if processBatch() to be called after persistence work performed
	 */
	protected void processBatch() 
	{
	}

	/**
	 * Returns limit set on number of results a query will produce
	 * @return Limit number or 0 if no limit
	 */
	public int getMaxResults() 
	{
		return maxResults;
	}

	/**
	 * Set limit on number of results a query will produce
	 * @param maxResults int greater than 0
	 */
	public void setMaxResults(int maxResults) 
	{
		this.maxResults = maxResults;
	}

	public PersistenceWorker getPersistenceService() 
	{
		return persistenceWorker;
	}

	
}
