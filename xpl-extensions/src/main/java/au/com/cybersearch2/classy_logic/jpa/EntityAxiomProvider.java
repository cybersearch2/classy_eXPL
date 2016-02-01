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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.AxiomProvider;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * EntityAxiomProvider
 * @author Andrew Bowley
 * 23 May 2015
 */
public class EntityAxiomProvider implements AxiomProvider
{
    /** Collection of Collectors which each fetch all rows in one database entity table */
    protected Map<String, JpaEntityCollector<?>> collectorMap;
    /** The optional task to set up the entity table. Intended for testing use only */
    protected PersistenceWork setUpTask;
    protected String name;
    protected boolean databaseCreated;

    /**
     * EntityAxiomProvider
     * @param name Name of provider
     */
    public EntityAxiomProvider(String name)
    {
        this(name, null);
    }

    /**
     * EntityAxiomProvider
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
     * @param setUpTask PersistenceWork object to perform one-time initialization
     */
    public EntityAxiomProvider(String name, PersistenceWork setUpTask)
    {
        this.name = name;
        collectorMap = Collections.emptyMap();
        if (setUpTask == null)
            databaseCreated = true;
        else
            this.setUpTask = setUpTask;
    }

    /**
     * 
     * @param axiomName
     * @param entityClass Class of entity to be collected
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void addEntity(String axiomName, Class<?> entityClass, PersistenceWorker persistenceService)
    {
        addCollector(axiomName, new JpaEntityCollector(entityClass, persistenceService));
    }
    
	public void addCollector(String axiomName, JpaEntityCollector<?> jpaEntityCollector)
    {
        if (collectorMap.size() == 0)
            collectorMap = new HashMap<String, JpaEntityCollector<?>>();
        collectorMap.put(axiomName, jpaEntityCollector);
    }
    
    /**
     * Returns Axiom Provider identity, which defaults to PersistenceUnitAdmin Unit name
     */
    @Override
    public String getName() 
    {
        return name;
    }

    /**
     * Initialize Axiom Provider
     * @param axiomName Axiom key (not used)
     * @param properties Optional properties to add to the persistence unit properties
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#setResourceProperties(java.lang.String, java.util.Map)
     */
    @Override
    public void setResourceProperties(String axiomName, Map<String, Object> properties) 
    {
    	JpaEntityCollector<?> collector = collectorMap.get(axiomName);
    	if (collector == null)
    		throw new IllegalArgumentException("AxiomProvider " + name + ": no collector found with name " + axiomName);
    	PersistenceWorker persistenceService = collector.getPersistenceService();
        // If properties provide, add them to the persistence unit's properties
        if (properties != null)
        	persistenceService.getPersistenceContext()
        	.getPersistenceAdmin(persistenceService.getPersistenceUnit())
        	.getProperties()
        	.putAll(properties);
        if ((setUpTask != null) && !databaseCreated)
        {
            // Execute work and wait synchronously for completion
            try 
            {
            	persistenceService.doWork(setUpTask).waitForTask();
                databaseCreated = true;
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#getAxiomSource(java.lang.String, java.util.List)
     */
    @Override
    public AxiomSource getAxiomSource(String axiomName,
            List<String> axiomTermNameList) 
    {
        if (isEmpty())
            throw new QueryExecutionException("No axiomSource available for \"" + axiomName + "\"");
        List<NameMap> nameMapList = new ArrayList<NameMap>();
        for (String termName: axiomTermNameList)
            nameMapList.add(new NameMap(termName, termName));
        JpaEntityCollector<?> jpaEntityCollector = collectorMap.get(axiomName);
        return new JpaSource(jpaEntityCollector, axiomName, nameMapList);
    }

    @Override
    public boolean isEmpty() 
    {
        return !databaseCreated && (collectorMap.size() > 0);
    }

    @Override
    public AxiomListener getAxiomListener() 
    {   // Do-nothing listener for read-only provider
        return new AxiomListener()
        {
            @Override
            public void onNextAxiom(Axiom axiom) 
            {
            }
        };
    }
}
