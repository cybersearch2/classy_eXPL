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
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.TestModule;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classytask.Executable;

/**
 * PersistenceAgriculture demonstrates a custom JpaEntityCollector, AgriPercentCollector, used to
 * back a JpaSource axiom source. The database is populated using data in
 * agriculture-land.xpl script (AgriDatabase).
 * @author Andrew Bowley
 * 17 Mar 2015
 */
public class PersistenceAgriculture 
{
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";

    private ApplicationComponent component;
    private AgriYearPercentPersistenceService persistenceService;

    /**
     * Construct PersistenceHighCities object. It initializes dependency injection
     * and creates a in-memory Cities database.
     * @throws InterruptedException
     */
	public PersistenceAgriculture() throws InterruptedException 
	{
        component = 
                DaggerApplicationComponent.builder()
                .testModule(new TestModule())
                .build();
        getExecutable(new AgriDatabase()).waitForTask();
        persistenceService = new AgriYearPercentPersistenceService(component);
	}

	/**
	 * Creates an axiom source which fetches  objects and converts
	 * them into axioms using entity names
	 * <p>Expected results:<br/> 
		</p>	 
	 */
    public Iterator<Axiom> testDataQuery()
    {
        JpaEntityCollector<YearPercent> yearPercentCollector = new AgriPercentCollector(persistenceService);
    	List<NameMap> termNameList = new ArrayList<NameMap>();
    	termNameList.add(new NameMap("country", "country"));
        for (int year = 1962; year < 2011; ++year)
            termNameList.add(new NameMap("y" + year, "Y" + year));
    	JpaSource jpaSource = new JpaSource(yearPercentCollector, "Data", termNameList); 
    	return jpaSource.iterator();
    }

    public Executable getExecutable(PersistenceWork persistenceWork)
    {
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }


	public static void main(String[] args) throws InterruptedException
	{
        try 
        {
    		PersistenceAgriculture ariculture = new PersistenceAgriculture();
    		Iterator<Axiom> axiomIterator = ariculture.testDataQuery();
            while (axiomIterator.hasNext())
                System.out.println(axiomIterator.next().toString());
            System.out.println("Done!");
        } 
        catch (ExpressionException e) 
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
