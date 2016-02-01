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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Test;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.compile.ParserAssembler;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.parser.ParseException;
import au.com.cybersearch2.classy_logic.parser.QueryParser;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.tutorial15.PersistenceAgriculture;


/**
 * PersistenceAgricultureTest
 * @author Andrew Bowley
 * 2 Jun 2015
 */
public class PersistenceAgricultureTest
{

    /**
     * Test round trip from axiom source to peristence unit and back again.
     * @throws SQLException
     * @throws ParseException
     * @throws InterruptedException
     */
    @Test
    public void test_PersistenceAgriculture() throws SQLException, ParseException, InterruptedException
    {
        ParserAssembler parserAssembler = null;
        PersistenceAgriculture ariculture = new PersistenceAgriculture();
        try 
        {
            parserAssembler = openScript("include \"agriculture-land.xpl\";");
        } 
        catch (ParseException e) 
        {
            throw new IllegalStateException("Error compiling \"agriculture-land.xpl\"", e);
        }
        AxiomSource agriSource = parserAssembler.getAxiomSource(QualifiedName.parseGlobalName("Data"));
        Iterator<Axiom> dataIterator = agriSource.iterator();
        Iterator<Axiom>  aricultureIterator = ariculture.testDataQuery();
        while ( aricultureIterator.hasNext())
        {
           assertThat(dataIterator.hasNext());
           Axiom output = aricultureIterator .next();
           Axiom input = dataIterator.next();
           assertThat(output.getTermCount()).isEqualTo(input.getTermCount());
           assertThat(output.getTermByName("country").toString()).isEqualTo(input.getTermByName("country").toString());
           for (int i = 1; i < input.getTermCount(); i++)
           {
               String inputTerm = input.getTermByIndex(i).toString();
               String outputTerm = output.getTermByIndex(i).toString();
               assertThat(outputTerm.toUpperCase()).isEqualTo(inputTerm.toUpperCase());
           }
        }
    }

    protected ParserAssembler openScript(String script) throws ParseException
    {
        InputStream stream = new ByteArrayInputStream(script.getBytes());
        QueryParser queryParser = new QueryParser(stream);
        queryParser.enable_tracing();
        QueryProgram queryProgram = new QueryProgram();
        queryProgram.setResourceBase(new File("src/main/resources"));
        queryParser.input(queryProgram);
        return queryProgram.getGlobalScope().getParserAssembler();
    }
}
