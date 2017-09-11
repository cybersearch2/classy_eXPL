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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.ResourceProvider;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * EntityAxiomProvider
*  An eXPL resource object which receives and transmits axioms on a connection to a relational database
*  using Classy Tools JPA.
 * @author Andrew Bowley
 * 23 May 2015
 */
public class EntityAxiomProvider implements ResourceProvider
{
    /** Collection of Collectors which each fetch all rows in one database entity table */
    protected Map<String, JpaEntityCollector<?>> collectorMap;
    /** Helper to perform persistence work */
    protected PersistenceWorker persistenceWorker;
    /** The optional task to set up the entity table. Intended for testing use only */
    protected PersistenceWork setUpTask;
    /** Name of provider */
    protected String name;
    /** Flag set true if database creation completed successfully */
    protected boolean databaseCreated;

    /**
     * EntityAxiomProvider
     * @param name Name of provider
     * @param persistenceWorker Executes tasks
     */
    public EntityAxiomProvider(String name, PersistenceWorker persistenceWorker)
    {
        this(name, persistenceWorker, null);
    }

    /**
     * EntityAxiomProvider
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
     * @param persistenceWorker Executes tasks
     * @param setUpTask PersistenceWork object to perform one-time initialization
     */
    public EntityAxiomProvider(String name, PersistenceWorker persistenceWorker, PersistenceWork setUpTask)
    {
        this.name = name;
        // TODO - validate parameters
        this.persistenceWorker = persistenceWorker;
        this.setUpTask = setUpTask;
        collectorMap = Collections.emptyMap();
        if (setUpTask == null)
            databaseCreated = true;
    }

    /**
     * Associate Entity Class with axiom name 
     * @param axiomName
     * @param entityClass Entity class
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void addEntity(String axiomName, Class<?> entityClass)
    {
        addCollector(axiomName, new JpaEntityCollector(entityClass, persistenceWorker));
    }

    /**
     * Associate Entity Collector with axiom name
     * @param axiomName
     * @param jpaEntityCollector JpaEntityCollector object
     */
	public void addCollector(String axiomName, JpaEntityCollector<?> jpaEntityCollector)
    {
        if (collectorMap.size() == 0)
            collectorMap = new HashMap<String, JpaEntityCollector<?>>();
        collectorMap.put(axiomName, jpaEntityCollector);
    }
    
    /**
     * Returns Axiom Provider identity, which defaults to PersistenceUnitAdmin Unit name
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#getName
     */
    @Override
    public String getName() 
    {
        return name;
    }

    /**
     * Open Axiom Provider
     * @param properties Optional properties to add to the persistence unit properties
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#open(java.util.Map)
     */
    @Override
    public void open(Map<String, Object> properties) 
    {
        // If properties provide, add them to the persistence unit's properties
        if (properties != null)
        	persistenceWorker.getPersistenceContext()
        	.getPersistenceAdmin(persistenceWorker.getPersistenceUnit())
        	.getProperties()
        	.putAll(properties);
        if ((setUpTask != null) && !databaseCreated)
            createDatabase();
    }

    @Override
    public Iterator<Axiom> iterator(AxiomArchetype archetype)
    {
        if ((setUpTask != null) && !databaseCreated)
            createDatabase();
        if (isEmpty())
            throw new QueryExecutionException("No axiomSource available for \"" + archetype.getName() + "\"");
        archetype.clearMutable();
        JpaEntityCollector<?> jpaEntityCollector = collectorMap.get(archetype.getName());
        return new JpaSource(jpaEntityCollector, archetype).iterator();
    }

    /**
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#isEmpty()
     */
    @Override
    public boolean isEmpty() 
    {
        return !databaseCreated && (collectorMap.size() > 0);
    }

    /**
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#getAxiomListener()
     */
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

    /**
     * @see au.com.cybersearch2.classy_logic.interfaces.AxiomProvider#close()
     */
    @Override
    public void close()
    {
    }

    /**
     * Execute setup task to create database 
     */
    synchronized private void createDatabase()
    {
        if (!databaseCreated)
        {
            // Execute work and wait synchronously for completion
            try 
            {
                persistenceWorker.doWork(setUpTask).waitForTask();
                databaseCreated = true;
            } 
            catch (InterruptedException e) 
            {
            }
        }
    }

    
}
