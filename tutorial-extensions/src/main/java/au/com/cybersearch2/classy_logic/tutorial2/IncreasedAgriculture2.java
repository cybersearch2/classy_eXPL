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
package au.com.cybersearch2.classy_logic.tutorial2;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.QueryProgramParser;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Archetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
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
/* more_agriculture.xpl
resource surface_area_increase export = "agriculture";

include "agriculture-land.xpl";
include "surface-land.xpl";
template agri_20y (double agri_change = Y2010 - Y1990, country ? agri_change > 1.0);

calc surface_area_increase (
  country? country == agri_20y.country,
  double surface_area = (agri_20y.agri_change)/100.0 * surface_area_Km2
 );

query<axiom> more_agriculture(agri_area_percent : agri_20y, surface_area : surface_area_increase);  

*/

    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agri_20_year";

    private ApplicationComponent component;
    private Agri20YearPersistenceService agri20YearService;
    private QueryProgramParser queryProgramParser;

	/**
	 * Construct IncreasedAgriculture2 object
	 */
	public IncreasedAgriculture2()
	{
        component = 
                DaggerApplicationComponent.builder()
                .agriModule(new AgriModule())
                .build();
		agri20YearService = 
				new Agri20YearPersistenceService(component);  
		// Case-insensitive match required for matching terms in database entities to axiom terms
        Archetype.CASE_INSENSITIVE_NAME_MATCH = true;
        File resourcePath = new File("src/main/resources");
        queryProgramParser = new QueryProgramParser(resourcePath, new AgriResourceProvider(agri20YearService));
	}
	
	/**
	 * Compiles the more_agriculture.xpl script and runs the "more_agriculture" query, 
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
        QueryProgram queryProgram = queryProgramParser.loadScript("tutorial2/more_agriculture.xpl");
		queryProgram.executeQuery("more_agriculture"
        // Uncomment following SolutionHandler parameter to see intermediate result 
		/*, new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				System.out.println(solution.getAxiom("surface_area_increase").toString());
				return true;
			}}*/);
		// Wait for the service query to clear,
		waitForService();
		List<NameMap> nameMapList = getNameMapList();
		JpaEntityCollector<Agri20Year> collector = 
		    new JpaEntityCollector<Agri20Year>(Agri20Year.class, agri20YearService);
        AxiomArchetype archetype = new AxiomArchetype(QualifiedName.parseGlobalName("surface_area_increase"));
        archetype.addTermName("id");
        archetype.addTermName("country");
        archetype.addTermName("surface_area");
        archetype.clearMutable();
		JpaSource jpaSource = new JpaSource(collector, archetype, nameMapList);
        return jpaSource.iterator();
	}

	private List<NameMap> getNameMapList()
    {
        List<NameMap> nameMapList = new ArrayList<NameMap>();
        nameMapList.add(new NameMap(Agri20Year.COUNTRY_TERM, "country"));
        nameMapList.add(new NameMap(Agri20Year.SURFACE_AREA_TERM, "surfaceArea"));
        nameMapList.add(new NameMap("id", "id"));
        return nameMapList;
    }

    private void waitForService()
    {
        if (agri20YearService.getCount() > 0)
            synchronized(agri20YearService)
            {
                try
                {
                    agri20YearService.wait();
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
            {
                Axiom axiom = iterator.next();
                String surface_area = axiom.getValueByName("surface_area").toString();
                System.out.println(axiom.getValueByName("country").toString() + " " + surface_area.substring(0, surface_area.indexOf(".") ));
            }

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
		finally
		{
		    System.exit(0);
		}
	}
}
