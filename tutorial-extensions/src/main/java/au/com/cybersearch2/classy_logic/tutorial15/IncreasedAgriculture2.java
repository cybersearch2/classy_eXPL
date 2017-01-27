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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;

/**
 * IncreasedAgriculture2 demonstrates Axiom Provider writing query results to a database.
 * Two queries are executed. The first produces a list of countries which have increased the area
 * under agriculture by more than 1% over the twenty years between 1990 and 2010. This query writes
 * it's results to a database table. The second query reads this table and prints it's contents row by row. 
 * @author Andrew Bowley
 * 20 Feb 2015
 */
public class IncreasedAgriculture2 
{
	static final String AGRICULTURAL_LAND = 
	    "include \"agriculture-land.xpl\";" +
		"include \"surface-land.xpl\";\n" +
	    "template agri_10y (country ? y2010 - y1990 > 1.0, double y1990, double y2010);\n" +
		"template surface_area_increase\n" +
		"(\n" +
		"  country ? country == agri_10y.country,\n" +
		"  double surface_area = (agri_10y.y2010 - agri_10y.y1990)/100 * surface_area_Km2\n" +
		");\n" +
	    "// Specify term list which writes to persistence resource 'agriculture'\n" +
		"list<term> surface_area_list(surface_area_increase : \"agriculture\");\n" +
	    "query more_agriculture(agri_area_percent : agri_10y, surface_area : surface_area_increase);"; 

    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agri_10_year";

	/** ProviderManager is Axiom source for eXPL compiler */
	private ProviderManager providerManager;
    private ApplicationComponent component;
    private AgriTenYearPersistenceService agri10YearService;

	/**
	 * Construct IncreasedAgriculture2 object
	 */
	public IncreasedAgriculture2()
	{
        component = 
                DaggerApplicationComponent.builder()
                .agriModule(new AgriModule())
                .build();
		agri10YearService = 
				new AgriTenYearPersistenceService(component);  
		providerManager = new ProviderManager();
		providerManager.putAxiomProvider(new AgriAxiomProvider(agri10YearService ));
	}
	
	/**
	 * Compiles the AGRICULTURAL_LAND script and runs the "more_agriculture" query, 
	 * displaying the solution on the console.<br/>
	 * The expected result first 3 lines:<br/>
        increased(country = Albania, surface_area = 986.1249999999999, id = 0)<br/>
        increased(country = Algeria, surface_area = 25722.79200000004, id = 1)<br/>
        increased(country = American Samoa, surface_area = 10.0, id = 2)<br/><br/>
     * The full result can be viewed in file src/main/resources/increased-agri-list.txt
     * @return Axiom iterator
	 */
	public Iterator<Axiom> displayIncreasedAgri()
	{
		QueryProgram queryProgram1 = new QueryProgram(providerManager);
		queryProgram1.setResourceBase(new File("src/main/resources"));
		queryProgram1.parseScript(AGRICULTURAL_LAND);
		queryProgram1.executeQuery("more_agriculture"
        // Uncomment following SolutionHandler parameter to see intermediate result 
		/*, new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				System.out.println(solution.getAxiom("surface_area_increase").toString());
				return true;
			}}*/);
		// Wait for the service quere to clear,
		waitForService();
		List<NameMap> nameMapList = getNameMapList();
		JpaEntityCollector<Agri10Year> collector = 
		    new JpaEntityCollector<Agri10Year>(Agri10Year.class, agri10YearService);
		JpaSource jpaSource = new JpaSource(collector, "surface_area_increase", nameMapList);
        return jpaSource.iterator();
	}

	private List<NameMap> getNameMapList()
    {
        List<NameMap> nameMapList = new ArrayList<NameMap>();
        nameMapList.add(new NameMap(Agri10Year.COUNTRY_TERM, "country"));
        nameMapList.add(new NameMap(Agri10Year.SURFACE_AREA_TERM, "surfaceArea"));
        nameMapList.add(new NameMap("id", "id"));
        return nameMapList;
    }

    private void waitForService()
    {
        if (agri10YearService.getCount() > 0)
            synchronized(agri10YearService)
            {
                try
                {
                    agri10YearService.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
    }

    /**
	 * Run tutorial
	 * @param args
	 */
	public static void main(String[] args)
	{
		try 
		{
	        IncreasedAgriculture2 increasedAgri = new IncreasedAgriculture2();
		    Iterator<Axiom> iterator = increasedAgri.displayIncreasedAgri();
            while(iterator.hasNext())
                System.out.println(iterator.next().toString());

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
