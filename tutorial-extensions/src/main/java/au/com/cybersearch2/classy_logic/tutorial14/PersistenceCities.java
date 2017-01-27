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
package au.com.cybersearch2.classy_logic.tutorial14;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classytask.Executable;

/**
 * PersistenceHighCities demonstrates how to source axioms from a database using
 * Classy Tools JPA implementation. Two test cases contrast default names taken
 * from the entity class with names defined by the client.
 * @author Andrew Bowley
 * 17 Mar 2015
 */
public class PersistenceCities 
{
    /** Named query to find all cities */
    static public final String ALL_CITIES = "all_cities";
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "cities";

    private ApplicationComponent component;
    private JpaEntityCollector<City> cityCollector;
    
    /**
     * Construct PersistenceHighCities object. It initializes dependency injection
     * and creates a in-memory Cities database.
     * @throws InterruptedException
     */
	public PersistenceCities() throws InterruptedException 
	{
        component = 
                DaggerApplicationComponent.builder()
                .citiesModule(new CitiesModule())
                .build();
        PersistenceWork setUpWork = new CitiesDatabase();
        // Execute work and wait synchronously for completion
        getExecutable(setUpWork).waitForTask();
        CityPersistenceWorker cityPersistenceWorker = new CityPersistenceWorker(component);
        cityCollector = new JpaEntityCollector<City>(City.class, cityPersistenceWorker);
	}

	/**
	 * Creates an axiom source which fetches City objects and converts
	 * them into axioms using entity names
	 * <p>Expected results:<br/> 
		city(id = 1, altitude = 1718, name = bilene)<br/>
		city(id = 2, altitude = 8000, name = addis ababa)<br/>
		city(id = 3, altitude = 5280, name = denver)<br/>
		city(id = 4, altitude = 6970, name = flagstaff)<br/>
		city(id = 5, altitude = 8, name = jacksonville)<br/>
		city(id = 6, altitude = 10200, name = leadville)<br/>
		city(id = 7, altitude = 1305, name = madrid)<br/>
		city(id = 8, altitude = 19, name = richmond)<br/>
		city(id = 9, altitude = 1909, name = spokane)<br/>
		city(id = 10, altitude = 1305, name = wichita)<br/>
		</p>	 
	 */
    public Iterator<Axiom> testEntityNamesQuery()
    {
    	JpaSource jpaSource = new JpaSource(cityCollector, "city");
    	return jpaSource.iterator();
    }

	/**
	 * Creates an axiom source which fetches City objects and converts
	 * them into axioms with specified names. Note case differences from
	 * previous test. 
	 * <p>Expected results:<br/> 
		city(Name = bilene, Altitude = 1718)<br/>
		city(Name = addis ababa, Altitude = 8000)<br/>
		city(Name = denver, Altitude = 5280)<br/>
		city(Name = flagstaff, Altitude = 6970)<br/>
		city(Name = jacksonville, Altitude = 8)<br/>
		city(Name = leadville, Altitude = 10200)<br/>
		city(Name = madrid, Altitude = 1305)<br/>
		city(Name = richmond, Altitude = 19)<br/>
		city(Name = spokane, Altitude = 1909)<br/>
		city(Name = wichita, Altitude = 1305)<br/>
		</p>	 
	  */
    public Iterator<Axiom> testSpecifiedNamesQuery()
    {
    	List<NameMap> termNameList = new ArrayList<NameMap>();
    	termNameList.add(new NameMap("Name", "name"));
    	termNameList.add(new NameMap("Altitude", "altitude"));
    	JpaSource jpaSource = new JpaSource(cityCollector, "city", termNameList); 
    	return jpaSource.iterator();
    }

    public Executable getExecutable(PersistenceWork persistenceWork)
    {
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }

    public static void main(String[] args)
	{
		try 
		{
			PersistenceCities cities = new PersistenceCities();
			System.out.println(">> Entity names");
			Iterator<Axiom> axiomIterator = cities.testEntityNamesQuery();
	        while (axiomIterator.hasNext())
	            System.out.println(axiomIterator.next().toString());
			System.out.println();
			System.out.println(">> Specified names");
			axiomIterator = cities.testSpecifiedNamesQuery();
	        while (axiomIterator.hasNext())
	            System.out.println(axiomIterator.next().toString());
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        catch (QueryExecutionException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
		System.exit(0);
	}

}
