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
package au.com.cybersearch2.classy_logic.agriculture;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.compile.ParserAssembler;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.parser.ParseException;
import au.com.cybersearch2.classy_logic.parser.QueryParser;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * CitiesDatabase persists YearPercent enity objects.
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class AgriDatabase implements PersistenceWork 
{

    @Override
    public void doTask(EntityManagerLite entityManager)
    {
        // Clear table
        EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
        @SuppressWarnings("unchecked")
        PersistenceDao<AgriAreaPercent, Integer> dao = (PersistenceDao<AgriAreaPercent, Integer>) delegate.getDaoForClass(AgriAreaPercent.class);
        int id = 0;
        while (dao.queryForId(id) != null)
        {
            dao.deleteById(id++);
        }
        dao.clearObjectCache();
		ParserAssembler parserAssembler = null;
		try 
		{
			parserAssembler = openScript("include \"agriculture-land.xpl\";");
		} 
		catch (ParseException e) 
		{
			throw new IllegalStateException("Error compiling \"agriculture-land.xpl\"", e);
		}
	    AxiomSource agriSource = parserAssembler.getAxiomSource(QualifiedName.parseGlobalName("agri_area_percent"));
	    Iterator<Axiom> iterator = agriSource.iterator();
	    while (iterator.hasNext())
	    {
	    	Axiom axiom = iterator.next();
	    	String countryName = axiom.getTermByIndex(0).getValue().toString();
	    	AgriAreaPercent agriAreaPercent = new AgriAreaPercent();
	    	agriAreaPercent.setCountry(countryName);
            BeanMap beanMap = new BeanMap(agriAreaPercent);
	    	//System.out.println(countryEntity.getCountry() + " id = " + countryEntity.getId());
    		int year = 1962;
    		for (int i = 1; i < axiom.getTermCount(); ++i)
    		{
    			Double percent = (Double) axiom.getTermByIndex(i).getValue();
    			if (Double.isNaN(percent))
    		         // Sqlite does not support NaN. So use special value "-0.001" to indicate NaN
    				percent = Double.valueOf(-0.001); // NaN is persisted by SQLite as null, so represent as near zero
    	        String yearName = "y" + year++;
                beanMap.put(yearName, percent);
    			//System.out.println(yearPercent.getId() + ", " + yearPercent.getCountry().getCountry());
    			//System.out.println(axiom.getTermByIndex(i).getValue());
    		}
            entityManager.persist(agriAreaPercent);
            entityManager.refresh(agriAreaPercent);
	    }
    }

    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success)
    {
        if (!success)
            throw new IllegalStateException("Database set up failed. Check console for error details.");
    }

    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException)
    {
        throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
    }

    /**
     * Returns ParserAssembler after parsing given eXPL script
     * @param script
     * @return ParserAssembler object
     * @throws ParseException
     */
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

