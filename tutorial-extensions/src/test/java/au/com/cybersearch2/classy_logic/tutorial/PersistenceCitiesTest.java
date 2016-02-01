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

import java.util.Iterator;

import org.junit.Test;

import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.tutorial14.PersistenceCities;

/**
 * PersistenceCitiesTest
 * @author Andrew Bowley
 * 2 Jun 2015
 */
public class PersistenceCitiesTest
{
    static String[] ENTITY_NAMES_LIST =
    {
        "city(id = 1, altitude = 1718, name = bilene)",
        "city(id = 2, altitude = 8000, name = addis ababa)",
        "city(id = 3, altitude = 5280, name = denver)",
        "city(id = 4, altitude = 6970, name = flagstaff)",
        "city(id = 5, altitude = 8, name = jacksonville)",
        "city(id = 6, altitude = 10200, name = leadville)",
        "city(id = 7, altitude = 1305, name = madrid)",
        "city(id = 8, altitude = 19, name = richmond)",
        "city(id = 9, altitude = 1909, name = spokane)",
        "city(id = 10, altitude = 1305, name = wichita)"
    };

    static String[] SPECIFIED_NAMES_LIST =
    {
        "city(Name = bilene, Altitude = 1718)",
        "city(Name = addis ababa, Altitude = 8000)",
        "city(Name = denver, Altitude = 5280)",
        "city(Name = flagstaff, Altitude = 6970)",
        "city(Name = jacksonville, Altitude = 8)",
        "city(Name = leadville, Altitude = 10200)",
        "city(Name = madrid, Altitude = 1305)",
        "city(Name = richmond, Altitude = 19)",
        "city(Name = spokane, Altitude = 1909)",
        "city(Name = wichita, Altitude = 1305)"
    };

    @Test
    public void test_PersistenceCities() throws InterruptedException
    {
        PersistenceCities cities = new PersistenceCities();
        Iterator<Axiom> axiomIterator = cities.testEntityNamesQuery();
        int index = 0;
        while (axiomIterator.hasNext())
            assertThat(axiomIterator.next().toString()).isEqualTo(ENTITY_NAMES_LIST[index++]);
        axiomIterator = cities.testSpecifiedNamesQuery();
        index = 0;
        while (axiomIterator.hasNext())
            assertThat(axiomIterator.next().toString()).isEqualTo(SPECIFIED_NAMES_LIST[index++]);
    }

}
