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
package au.com.cybersearch2.classy_logic.tutorial;

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classy_logic.parser.ParseException;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.tutorial14.HighCitiesSorted;

/**
 * HighCitiesSortedTest
 * @author Andrew Bowley
 * 18 May 2015
 */
public class HighCitiesSortedTest
{
    
    @Before
    public void setUp()
    {
    }
    
    @Test
    public void test_HighCitiesSorted() throws SQLException, ParseException, InterruptedException
    {
        HighCitiesSorted highCitiesSorted = new HighCitiesSorted();
        Iterator<Axiom> iterator = highCitiesSorted.getHighCities();
        assertThat(iterator.next().toString()).isEqualTo("high_city(name = denver, altitude = 5280)");
        assertThat(iterator.next().toString()).isEqualTo("high_city(name = flagstaff, altitude = 6970)");
        assertThat(iterator.next().toString()).isEqualTo("high_city(name = addis ababa, altitude = 8000)");
        assertThat(iterator.next().toString()).isEqualTo("high_city(name = leadville, altitude = 10200)");
    }
    

}
