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
package au.com.cybersearch2.classy_logic.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.ResourceProvider;
import au.com.cybersearch2.classy_logic.jpa.City;
import au.com.cybersearch2.classy_logic.jpa.CityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * TestAxiomProvider
 * @author Andrew Bowley
 * 11 Feb 2015
 */
public class TestAxiomProvider extends ProviderManager implements ResourceProvider 
{
	private PersistenceWorker persistenceWorker;

	public TestAxiomProvider(PersistenceWorker persistenceService)
	{
		this.persistenceWorker = persistenceService;
	}
	
	@Override
	public void open(Map<String, Object> properties) 
	{
		String persistenceUnit = null;
		try 
		{
			persistenceUnit = "cities";
			// Execute work and wait synchronously for completion
			List<Object> data = new ArrayList<Object>();
        	data.add(new City("bilene", 1718));
        	data.add(new City("addis ababa", 8000));
        	data.add(new City("denver", 5280));
        	data.add(new City("flagstaff", 6970));
        	data.add(new City("jacksonville", 8));
        	data.add(new City("leadville", 10200));
        	data.add(new City("madrid", 1305));
        	data.add(new City("richmond",19));
        	data.add(new City("spokane", 1909));
        	data.add(new City("wichita", 1305));
        	persistenceWorker.doWork(setUpWork(data)).waitForTask();
		} 
		catch (InterruptedException e) 
		{
			throw new QueryExecutionException("Work for persistence unit \"" + persistenceUnit + "\" interrupted", e);
		}
	}

    @Override
    public Iterator<Axiom> iterator(AxiomArchetype archetype)
    {
        CityCollector cityCollector = new CityCollector(persistenceWorker);
        cityCollector.createSelectAllQuery("all_cities");
        return new JpaSource(cityCollector, archetype).iterator();
    }
/*    
	public AxiomSource getAxiomSource(String axiomName,
			List<String> axiomTermNameList) 
	{
		AxiomSource axiomSource = null;
		if ("city".equals(axiomName))
		{
			List<NameMap> nameMapList = new ArrayList<NameMap>();
			for (String termName: axiomTermNameList)
				nameMapList.add(new NameMap(termName, termName));
			CityCollector cityCollector = new CityCollector(persistenceWorker);
	    	cityCollector.createSelectAllQuery("all_cities");
			axiomSource = new JpaSource(cityCollector, axiomName, nameMapList);
		}
		return axiomSource;
	}
*/
	@Override
	public boolean isEmpty() 
	{
		return false;
	}

	@Override
	public AxiomListener getAxiomListener(String name) 
	{   // Do-nothing listener for read-only provider
		return new AxiomListener()
		{
			@Override
			public void onNextAxiom(QualifiedName qname, Axiom axiom) 
			{
			}
		};
	}
	
	@Override
	public ResourceProvider getResourceProvider(QualifiedName name)
	{
		return this;
	}

    protected PersistenceWork setUpWork(final List<Object> data) 
    {
        return new PersistenceWork()
        {
	        @Override
	        public void doTask(EntityManagerLite entityManager)
	        {
	        	for (Object object: data)
	            	entityManager.persist(object);
	        }
	
	        @Override
	        public void onPostExecute(boolean success)
	        {
	            if (!success)
	                throw new IllegalStateException("Database set up failed. Check console for error details.");
	        }
	
	        @Override
	        public void onRollback(Throwable rollbackException)
	        {
	            throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
	        }
        };
    }

	@Override
	public String getName() 
	{
		return "cities";
	}

    @Override
    public void close()
    {
    }



}
