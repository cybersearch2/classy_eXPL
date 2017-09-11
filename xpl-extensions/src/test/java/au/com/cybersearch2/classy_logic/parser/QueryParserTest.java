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
package au.com.cybersearch2.classy_logic.parser;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryParams;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.TestModule;
import au.com.cybersearch2.classy_logic.compile.ParserAssembler;
import au.com.cybersearch2.classy_logic.compile.ParserContext;
import au.com.cybersearch2.classy_logic.pattern.KeyName;
import au.com.cybersearch2.classy_logic.pattern.Template;
import au.com.cybersearch2.classy_logic.query.LogicQueryExecuter;
import au.com.cybersearch2.classy_logic.query.QuerySpec;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

/**
 * QueryParserTest
 * @author Andrew Bowley
 * 6 Dec 2014
 */
public class QueryParserTest 
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

    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "cities";
	static final String HIGH_CITIES_JPA_XPL =
	        "resource city axiom  (name, altitude) = \"cities\";\n" +
			"template high_city(string name, altitude ? altitude > 5000);\n"
			;

    private ApplicationComponent component;
    private ProviderManager providerManager;

    @Before
    public void setup() throws Exception
    {
        component = 
                DaggerQueryParserTest_ApplicationComponent.builder()
                .testModule(new TestModule())
                .build();
        PersistenceWorker persistenceService = new CityPersistenceService(component.persistenceContext(), this);
        providerManager = new TestAxiomProvider(persistenceService);
    }

	@Test
	public void test_high_cities_jpa() throws Exception
	{
		InputStream stream = new ByteArrayInputStream(HIGH_CITIES_JPA_XPL.getBytes());
		QueryParser queryParser = new QueryParser(stream);
		QueryProgram queryProgram = new QueryProgram(providerManager);
        ParserContext context = new ParserContext(queryProgram);
        queryParser.input(context);
	    ParserAssembler parserAssembler = queryProgram.getGlobalScope().getParserAssembler();
	    Template highCities = parserAssembler.getTemplateAssembler().getTemplate("high_city");
	    highCities.setKey("city");
        QuerySpec querySpec = new QuerySpec("TEST", true);
		KeyName keyName = new KeyName("city", "high_city");
		querySpec.addKeyName(keyName);
		QueryParams queryParams = new QueryParams(queryProgram.getGlobalScope(), querySpec);
		queryParams.initialize();
	    LogicQueryExecuter highCitiesQuery = new LogicQueryExecuter(queryParams);
	    int count = 0;
 	    if (highCitiesQuery.execute())
 	    {
  	    	assertThat(highCitiesQuery.toString()).isEqualTo("high_city(name=addis ababa, altitude=8000)");
  	    	++count;
 	    }
 	    if (highCitiesQuery.execute())
 	    {
  	    	assertThat(highCitiesQuery.toString()).isEqualTo("high_city(name=denver, altitude=5280)");
  	    	++count;
 	    }
 	    if (highCitiesQuery.execute())
 	    {
  	    	assertThat(highCitiesQuery.toString()).isEqualTo("high_city(name=flagstaff, altitude=6970)");
  	    	++count;
 	    }
 	    if (highCitiesQuery.execute())
 	    {
  	    	assertThat(highCitiesQuery.toString()).isEqualTo("high_city(name=leadville, altitude=10200)");
  	    	++count;
 	    }
 	    assertThat(count).isEqualTo(4);
	}
	
    public Executable getExecutable(PersistenceWork persistenceWork)
    {
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }
}
