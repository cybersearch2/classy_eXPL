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
package au.com.cybersearch2.classy_logic.jpa;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.TestModule;
import au.com.cybersearch2.classy_logic.compile.ParserAssembler;
import au.com.cybersearch2.classy_logic.compile.ParserContext;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.parser.ParseException;
import au.com.cybersearch2.classy_logic.parser.QueryParser;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

/**
 * AgricultureJpaTest
 * @author Andrew Bowley
 * 10 Feb 2015
 */
public class AgricultureJpaTest 
{
    @Singleton
    @Component(modules = TestModule.class)  
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
        PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface PersistenceWorkSubcontext
    {
        Executable executable();
    }

    /** Named query to find all cities */
    static public final String ALL_AGRI_PERCENT = "all_agri_percent";
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";
    static public final String AFGHANISTAN = 
        	"Data(country = Afghanistan, Y1962 = 57.8, Y1963 = 57.89, Y1964 = 57.97, Y1965 = 58.07, Y1966 = 58.07, Y1967 = 58.13, " +
            "Y1968 = 58.23, Y1969 = 58.23, Y1970 = 58.26, Y1971 = 58.27, Y1972 = 58.32, Y1973 = 58.33, Y1974 = 58.34, Y1975 = 58.34, " +
        	"Y1976 = 58.34, Y1977 = 58.34, Y1978 = 58.34, Y1979 = 58.34, Y1980 = 58.34, Y1981 = 58.34, Y1982 = 58.34, Y1983 = 58.34, " +
            "Y1984 = 58.34, Y1985 = 58.34, Y1986 = 58.34, Y1987 = 58.34, Y1988 = 58.33, Y1989 = 58.32, Y1990 = 58.32, Y1991 = 58.32, " +
        	"Y1992 = 58.31, Y1993 = 58.31, Y1994 = 58.16, Y1995 = 57.97, Y1996 = 57.88, Y1997 = 57.88, Y1998 = 57.94, Y1999 = 58.06, " +
            "Y2000 = 57.88, Y2001 = 57.88, Y2002 = 57.88, Y2003 = 57.88, Y2004 = 58.12, Y2005 = 58.13, Y2006 = 58.12, Y2007 = 58.12, " +
        	"Y2008 = 58.12, Y2009 = 58.12, Y2010 = 58.12)";
     
    private ApplicationComponent component;
    
    @Before
    public void setUp() throws InterruptedException 
    {
        // Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object
    	component = 
    	        DaggerAgricultureJpaTest_ApplicationComponent.builder()
    	        .testModule(new TestModule())
    	        .build();
        PersistenceWork setUpWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
        		ParserAssembler parserAssembler = null;
				try 
				{
					parserAssembler = openScript("include \"agriculture-land.xpl\";");
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				}
        	    AxiomSource agriSource = parserAssembler.getAxiomSource(QualifiedName.parseGlobalName("Data"));
        	    Iterator<Axiom> iterator = agriSource.iterator();
        	    while (iterator.hasNext())
        	    {
        	    	Axiom axiom = iterator.next();
        	    	String country = axiom.getTermByIndex(0).getValue().toString();
        	    	Country countryEntity = new Country(country);
        	    	entityManager.persist(countryEntity);
        	    	entityManager.refresh(countryEntity);
        	    	//System.out.println(countryEntity.getCountry() + " id = " + countryEntity.getId());
    	    		int year = 1962;
    	    		for (int i = 1; i < axiom.getTermCount(); ++i)
    	    		{
    	    			Double percent = (Double) axiom.getTermByIndex(i).getValue();
    	                if (Double.isNaN(percent))
    	                     // Sqlite does not support NaN. So use special value "-0.001" to indicate NaN
    	                    percent = Double.valueOf(-0.001); // NaN is persisted by SQLite as null, so represent as near zero
    	    			YearPercent yearPercent = new YearPercent();
    	    			yearPercent.setYear("y" + year++);
    	    			yearPercent.setPercent(percent);
    	    			yearPercent.setCountry(countryEntity);
    	    			entityManager.persist(yearPercent);
    	    			entityManager.refresh(yearPercent);
    	    			//System.out.println(yearPercent.getId() + ", " + yearPercent.getCountry().getCountry());
    	    			//System.out.println(axiom.getTermByIndex(i).getValue());
    	    		}
    	    		if (year != 2011)
    	    		    throw new IllegalArgumentException("Invalid year: " + year);
        	    }
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
        getExecutable(setUpWork).waitForTask();
    }

    /**
     * Test round trip from axiom source to peristence unit and back again.
     * @throws SQLException
     * @throws ParseException
     * @throws InterruptedException
     */
    @Test
    public void test_query_term_names() throws Exception
    {
        ParserAssembler parserAssembler = null;
        parserAssembler = openScript("include \"agriculture-land.xpl\";");
        AxiomSource agriSource = parserAssembler.getAxiomSource(QualifiedName.parseGlobalName("Data"));
        Iterator<Axiom> dataIterator = agriSource.iterator();
        AgriPersistenceService agriPersistenceService = 
        		new AgriPersistenceService(component.persistenceContext(), this);
        AgriPercentCollector agriPercentCollector = 
        		new AgriPercentCollector(agriPersistenceService);
        List<NameMap> termNameList = new ArrayList<NameMap>();
        termNameList.add(new NameMap("country", "country"));
        int index = 0;
        for (int year = 1962; year < 2011; ++year)
        {
            NameMap nameMap = new NameMap("y" + year, "Y" + year);
            nameMap.setPosition(++index);
            termNameList.add(nameMap);
        }
        AxiomArchetype archetype = new AxiomArchetype(QualifiedName.parseGlobalName("Data"));
        JpaSource jpaSource = new JpaSource(agriPercentCollector, archetype, termNameList); 
        Iterator<Axiom>  aricultureIterator = jpaSource.iterator();
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
        queryProgram.setResourceBase(new File("src/test/resources"));
        ParserContext context = new ParserContext(queryProgram);
        queryParser.input(context);
        return queryProgram.getGlobalScope().getParserAssembler();
    }
    
    public Executable getExecutable(PersistenceWork persistenceWork)
    {
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }
}
