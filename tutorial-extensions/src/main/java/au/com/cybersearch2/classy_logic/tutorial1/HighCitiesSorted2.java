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
package au.com.cybersearch2.classy_logic.tutorial1;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.QueryProgramParser;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;

/**
 * HighCities
 * Solves:  Given list of cities with their elevations, which cities are at 5,000 feet or higher.
 * The cities are defined as an axiom source with each axiom containing a name term and an evelation term.
 * The terms are anonymous, so unification term pairing is performed by position.
 * @author Andrew Bowley
 * 20 Feb 2015
 */
public class HighCitiesSorted2 
{
/* high-cities-sorted.xpl
resource city axiom (altitude, name) = "cities";

// Solution is a list named 'high_cities'
export list<axiom> high_cities {};

// Template to filter high cities
template high_city
(
  altitude ? altitude > 5000,
  high_cities += axiom high_city { name, altitude }
);

// Calculator to perform insert sort on high_cities
calc insert_sort 
(
  // i is index to last item appended to the list
  integer i = high_cities.length - 1,
  // Skip first time when only one item in list
  : i < 1,
  // j is the swap index
  integer j = i - 1,
  // Save axiom to swap
  temp = high_cities[i],
  // Shuffle list until sort order restored
  {
    ? altitude < high_cities[j].altitude,
    high_cities[j + 1] = high_cities[j],
    ? --j >= 0
  },
  // Insert saved axiom in correct position
  high_cities[j + 1] = temp
);

query cities_query (city : high_city) -> (insert_sort);

*/

    private ApplicationComponent component;
    private QueryProgramParser queryProgramParser;

	public HighCitiesSorted2() throws InterruptedException
	{
        component = 
                DaggerApplicationComponent.builder()
                .citiesModule(new CitiesModule())
                .build();
		EntityResourceProvider entityResourceProvider = new EntityResourceProvider("cities", new CityPersistenceWorker(component), new CitiesDatabase());
		entityResourceProvider.addEntity("city", City.class); 
        File resourcePath = new File("src/main/resources/tutorial1");
        queryProgramParser = new QueryProgramParser(resourcePath, entityResourceProvider);
	}
	
	/**
	 * Compiles the high-cities-sorted.xpl script and runs the "cities_query" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
	 * high_city(name=denver, altitude=5280)<br/>
	 * high_city(name=flagstaff, altitude=6970)<br/>
	 * high_city(name=addis ababa, altitude=8000)<br/>
	 * high_city(name=leadville, altitude=10200)<br/>
	 * @return Axiom iterator
	 */
    public Iterator<Axiom> getHighCities()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("high-cities-sorted.xpl");
		Result result = queryProgram.executeQuery("cities_query");
		return result.axiomIterator(QualifiedName.parseGlobalName("high_cities"));
	}

	public static void main(String[] args) throws SQLException, InterruptedException
	{
		try 
		{
	        HighCitiesSorted2 highCities = new HighCitiesSorted2();
			Iterator<Axiom> iterator = highCities.getHighCities();
	        while(iterator.hasNext())
	            System.out.println(iterator.next().toString());
		} 
		catch (ExpressionException e) 
		{   // Display nested ParseException
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
