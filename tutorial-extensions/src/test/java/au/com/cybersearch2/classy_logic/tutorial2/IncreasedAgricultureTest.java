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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.junit.Test;

import au.com.cybersearch2.classy_logic.pattern.Axiom;

/**
 * IncreasedAgricultureTest
 * @author Andrew Bowley
 * 5 Jun 2015
 */
public class IncreasedAgricultureTest
{

    @Test
    public void test_IncreasedAgriculture_1_resource() throws Exception
    {
        IncreasedAgriculture2 increasedAgriculture2 = new IncreasedAgriculture2();
        Iterator<Axiom> iterator = increasedAgriculture2.displayIncreasedAgri();
        File testFile = new File("src/main/resources/tutorial2", "increased-agri-list.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        assertThat(iterator.hasNext()).isTrue();
        while(iterator.hasNext())
        {
            Axiom axiom = iterator.next();
            String surface_area = axiom.getValueByName("surface_area").toString();
            String increase = axiom.getValueByName("country").toString() + " " + surface_area.substring(0, surface_area.indexOf(".") );
            String line = reader.readLine();
            assertThat(increase).isEqualTo(line);
        }
        reader.close();
    }

}
