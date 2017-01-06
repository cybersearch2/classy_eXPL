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
import java.util.Iterator;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.TestModule;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * IncreasedAgriculture demonstrates Axiom Provider writing query results to a database.
 * Two queries are executed. The first produces a list of countries which have increased the area
 * under agriculture by more than 1% over the twenty years between 1990 and 2010. This query writes
 * it's results to a database table. The second query reads this table and prints it's contents row by row. 
 * @author Andrew Bowley
 * 20 Feb 2015
 */
public class IncreasedAgriculture 
{
	static final String AGRICULTURAL_LAND = 
	    "resource \"agriculture\";\n" +
		"axiom Data() : \"agriculture\";\n" +
		"include \"surface-land.xpl\";\n" +
	    "template agri_10y (country ? y2010 - y1990 > 1.0, double y1990, double y2010);\n" +
		"template surface_area_increase (country ? country == agri_10y.country, double surface_area = (agri_10y.y2010 - agri_10y.y1990)/100 * surface_area_Km2);\n" +
	    "// Specify term list which writes to persistence resource 'agriculture'\n" +
		"list<term> surface_area_axiom(surface_area_increase : \"agriculture\");\n" +
	    "query more_agriculture(Data : agri_10y, surface_area : surface_area_increase);"; 

	static final String AGRI_10_YEAR =
	    "resource \"agriculture\";\n" +
		"axiom surface_area_increase (country, surface_area, id) : \"agriculture\";\n" +
	    "template increased(country, surface_area, id);\n" +
		"list increased_list(increased);\n" +
		"query increased_query(surface_area_increase : increased);";
	
	/** ProviderManager is Axiom source for eXPL compiler */
	private ProviderManager providerManager;
    private ApplicationComponent component;

	/**
	 * Construct IncreasedAgriculture object
	 */
	public IncreasedAgriculture()
	{
        component = 
                DaggerApplicationComponent.builder()
                .testModule(new TestModule())
                .build();
		PersistenceWorker yearPercentService = 
				new AgriYearPercentPersistenceService(component); 
		PersistenceService<Agri10Year> agri10YearService = 
				new AgriTenYearPersistenceService(component);  
		providerManager = new ProviderManager();
		providerManager.putAxiomProvider(new AgriAxiomProvider(yearPercentService,agri10YearService ));
	}
	
	/**
	 * Compiles the AGRICULTURAL_LAND script and runs the "more_agriculture" query, 
	 * then compiles the AGRI_10_YEAR script and runs the "increased_query" query,
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
		QueryProgram queryProgram2 = new QueryProgram(providerManager);
		queryProgram2.setResourceBase(new File("src/main/resources"));
		queryProgram2.parseScript(AGRI_10_YEAR);
		Result result = queryProgram2.executeQuery("increased_query");
		return result.getIterator(QualifiedName.parseGlobalName("increased_list"));
	}

	/**
	 * Run tutorial
	 * @param args
	 */
	public static void main(String[] args)
	{
		try 
		{
	        IncreasedAgriculture increasedAgri = new IncreasedAgriculture();
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
