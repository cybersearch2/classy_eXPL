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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Agriculture demonstrates a custom JpaEntityCollector, AgriPercentCollector, used to
 * back a JpaSource axiom source. The database is populated using data in
 * agriculture-land.xpl script (AgriDatabase).
 * @author Andrew Bowley
 * 17 Mar 2015
 */
public class Agriculture 
{
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";

    private AgricultureComponent component;
    private AgriYearPercentPersistenceWorker persistenceWorker;

    /**
     * Construct PersistenceHighCities object. It initializes dependency injection
     * and creates a in-memory Cities database.
     * @throws InterruptedException
     */
	public Agriculture() throws InterruptedException 
	{
        component = 
                DaggerAgricultureComponent.builder()
                .agricultureModule(new AgricultureModule())
                .build();
        getExecutable(new AgriDatabase()).waitForTask();
        persistenceWorker = new AgriYearPercentPersistenceWorker(component);
	}

	/**
	 * Creates an axiom source which fetches  objects and converts
	 * them into axioms using entity names
	 * <p>Expected results:<br/> 
		</p>	 
	 */
    public Iterator<Axiom> testDataQuery()
    {
        JpaEntityCollector<AgriAreaPercent> yearPercentCollector = new AgriPercentCollector(persistenceWorker);
    	List<NameMap> termNameList = new ArrayList<NameMap>();
    	termNameList.add(new NameMap("country", "country"));
        for (int year = 1962; year < 2011; ++year)
            termNameList.add(new NameMap("y" + year, "Y" + year));
    	JpaSource jpaSource = new JpaSource(yearPercentCollector, "agri_area_percent", termNameList); 
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
    		Agriculture ariculture = new Agriculture();
    		Iterator<Axiom> axiomIterator = ariculture.testDataQuery();
    		File solutionPath = new File("src/test/resources");
    		if (!solutionPath.exists())
    		    solutionPath.mkdirs();
    		File solutionFile = new File(solutionPath, "agriculture_land.xpl");
    		PrintWriter writer = null;
    		try
    		{
    		    writer = new PrintWriter(solutionFile);
    		    writer.println("axiom agri_area_percent");
    		    writer.println("    (country,Y1962,Y1963,Y1964,Y1965,Y1966,Y1967,Y1968,Y1969,Y1970,Y1971,Y1972,Y1973,Y1974,Y1975,Y1976,Y1977,Y1978,Y1979,Y1980,Y1981,Y1982,Y1983,Y1984,Y1985,Y1986,Y1987,Y1988,Y1989,Y1990,Y1991,Y1992,Y1993,Y1994,Y1995,Y1996,Y1997,Y1998,Y1999,Y2000,Y2001,Y2002,Y2003,Y2004,Y2005,Y2006,Y2007,Y2008,Y2009,Y2010)");
                while (axiomIterator.hasNext())
                {
                    Axiom axiom = axiomIterator.next();
                    int index = 0;
                    writer.write("    {\"" + axiom.getTermByIndex(index++).getValue() + "\"");
                    while (index < 48)
                        writer.write("," + axiom.getTermByIndex(index++).getValue());
                    writer.println("}");
                }
    		}
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
    		finally
    		{
    		    if (writer != null)
                    writer.close();
    		}
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
