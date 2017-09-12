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
package au.com.cybersearch2.classy_logic.tutorial3;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.QueryProgramParser;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.agriculture.AgriAreaPercent;
import au.com.cybersearch2.classy_logic.agriculture.AgriPercentCollector;
import au.com.cybersearch2.classy_logic.agriculture.AgricultureComponent;
import au.com.cybersearch2.classy_logic.agriculture.AgricultureModule;
import au.com.cybersearch2.classy_logic.agriculture.DaggerAgricultureComponent;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.SolutionHandler;
import au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Archetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classy_logic.query.Solution;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * IncreasedAgriculture3 demonstrates Resource Provider writing query results to a database.
 * Two queries are executed. The first produces a list of countries which have increased the area
 * under agriculture by more than 1% over the twenty years between 1990 and 2010. This query writes
 * it's results to a database table. The second query reads this table and prints it's contents row by row. 
 * @author Andrew Bowley
 * 20 Feb 2015
 */
public class IncreasedAgriculture3 
{
/* more_agriculture.xpl
resource agri_area_percent axiom() = "agriculture";
resource surface_area_increase export = "agri_20_year"; 
  
include "surface-land.xpl";

template agri_20y (double agri_change = Y2010 - Y1990, country ? agri_change > 1.0);

calc surface_area_increase (
  country? country == agri_20y.country,
  double surface_area = (agri_20y.agri_change)/100.0 * surface_area_Km2);

query more_agriculture(agri_area_percent : agri_20y, surface_area : surface_area_increase);

*/
/* agri-20-year.xpl
resource surface_area_increase axiom (country, surface_area, id) = "agri_20_year";

template increased(country, surface_area, id);

query<axiom> increased_query(surface_area_increase : increased);
 
*/
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agri_20_year";

    /** ProviderManager is Axiom source for eXPL compiler */
    private AgricultureComponent component1;
    private ApplicationComponent component2;
    private Agri20YearPersistenceService agri20YearService; 
    private QueryProgramParser queryProgramParser;

	/**
	 * Construct IncreasedAgriculture2 object
	 */
	public IncreasedAgriculture3()
	{
        component1 = 
                DaggerAgricultureComponent.builder()
                .agricultureModule(new AgricultureModule())
                .build();
        component2 = 
                DaggerApplicationComponent.builder()
                .agriModule(new AgriModule())
                .build();
		PersistenceWorker yearPercentWorker = 
				new AgriYearPercentPersistenceWorker(component1); 
		agri20YearService = new Agri20YearPersistenceService(component2);  
        final JpaEntityCollector<AgriAreaPercent> yearPercentCollector = new AgriPercentCollector(yearPercentWorker);
        final List<NameMap> termNameList = new ArrayList<NameMap>();
        termNameList.add(new NameMap("country", "country"));
        int index = 0;
        for (int year = 1962; year < 2011; ++year)
        {
            NameMap nameMap = new NameMap("y" + year, "Y" + year);
            nameMap.setPosition(++index);
            termNameList.add(nameMap);
        }
        EntityResourceProvider entityResourceProvider = new EntityResourceProvider("agriculture", yearPercentWorker)
        {
            @Override
            public Iterator<Axiom> iterator(AxiomArchetype archetype) 
            {
                JpaSource jpaSource = new JpaSource(yearPercentCollector, archetype, termNameList);
                return jpaSource.iterator();
            }
        };
        // Case-insensitive match required for matching terms in database entities to axiom terms
        Archetype.CASE_INSENSITIVE_NAME_MATCH = true;
        File resourcePath = new File("src/main/resources");
        // Use an external axiom source which is bound in AgriResourceProvider dependency class
        // to AxiomSource class LexiconSource
        queryProgramParser = new QueryProgramParser(resourcePath, entityResourceProvider, new AgriAxiomProvider(agri20YearService));
	}
	
	/**
	 * Compiles the more_agriculture.xpl script and runs the "more_agriculture" query, 
	 * then compiles the agri-20-year.xpl script and runs the "increased_query" query,
	 * displaying the solution on the console.<br/>
	 * The expected result first 3 lines:<br/>
        Albania 986<br/>
        Algeria 25722<br/>
        American Samoa 10<br/><br/>
     * The full result can be viewed in file src/main/resources/increased-agri-list.txt
     * @return Axiom iterator
	 */
	public Iterator<Axiom> displayIncreasedAgri()
	{
        QueryProgram queryProgram1 = queryProgramParser.loadScript("tutorial3/more_agriculture.xpl");
		queryProgram1.setResourceBase(new File("src/main/resources"));
		queryProgram1.executeQuery("more_agriculture"
        // Uncomment following SolutionHandler parameter to see intermediate result 
		/*, new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				System.out.println(solution.getAxiom("surface_area_increase").toString());
				return true;
			}}*/);
        QueryProgram queryProgram2 = queryProgramParser.loadScript("tutorial3/agri-20-year.xpl");
		Result result = queryProgram2.executeQuery("increased_query");
		return result.axiomIterator(QualifiedName.parseGlobalName("increased_query"));
	}

	/**
	 * Run tutorial
	 * @param args
	 */
	public static void main(String[] args)
	{
		try 
		{
	        IncreasedAgriculture3 increasedAgri = new IncreasedAgriculture3();
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
