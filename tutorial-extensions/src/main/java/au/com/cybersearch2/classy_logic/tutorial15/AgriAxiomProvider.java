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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.terms.Parameter;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;

/**
 * AgriAxiomProvider
 * An example of an Axiom Provider for more than one Axiom source.
 * The "surface_area_increase" Axiom source has a simple entity collector,
 * but the "Data" axiom source has a custom collector which fetches data in batch mode
 * for scalability and assembles axiom terms from two tables.
 * 
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class AgriAxiomProvider extends EntityAxiomProvider 
{
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";
    /** Axiom source name for percentage surface area over 50 year interval */
    static public final String PERCENT_AXIOM = "Data";
    /** Axiom source name for countries which increased agricultural surface area over 10 year interval */
    static public final String TEN_YEAR_AXIOM = "surface_area_increase";
    /** Identity value for next Agri10YearId object to be created */
    static int agri10YearId;
    
    
    protected PersistenceService<Agri10Year> agri10YearService;
    
	/**
	 * Construct AgriAxiomProvider object
	 */
	public AgriAxiomProvider(
			PersistenceWorker yearPercentWorker, 
			PersistenceService<Agri10Year> agri10YearService) 
	{
	    // Super class will construct TEN_YEAR_AXIOM collector
		super(PU_NAME, agri10YearService, new AgriDatabase());
		this.agri10YearService = agri10YearService;
		//this.persistenceService = persistenceWorker;
		addEntity(TEN_YEAR_AXIOM, Agri10Year.class);
		addCollector(PERCENT_AXIOM, new AgriPercentCollector(yearPercentWorker));
	}

	/**
	 * Returns Axiom Provider identity
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider#getName()
	 */
	@Override
	public String getName() 
	{
		return "agriculture";
	}

	/**
	 * 
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider#getAxiomSource(java.lang.String, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AxiomSource getAxiomSource(String axiomName,
			List<String> axiomTermNameList) 
	{
		List<NameMap> nameMapList = null;
		if (axiomTermNameList != null)
			nameMapList = new ArrayList<NameMap>();
		JpaEntityCollector<YearPercent> collector = (JpaEntityCollector<YearPercent>) collectorMap.get(axiomName);
		if (PERCENT_AXIOM.equals(axiomName))
		{
			if (axiomTermNameList != null)
				for (String termName: axiomTermNameList)
					nameMapList.add(new NameMap(termName, termName));
		}
		else if (TEN_YEAR_AXIOM.equals(axiomName))
		{
			if (axiomTermNameList != null)
			{
				for (String termName: axiomTermNameList)
				{
					NameMap nameMap = null;
					if (termName.equals("country"))
			    	    nameMap = new NameMap("country", "countryName");
					else if (termName.equals("surface_area"))
		    	        nameMap = new NameMap("surface_area", "surfaceArea");
					else
						nameMap = new NameMap(termName, termName);
					nameMapList.add(nameMap);
				}
			}
		}
		else
			throw new IllegalArgumentException("Axiom name \"" + axiomName + "\" not valid for Axiom Provider \"" + getName() + "\"");
     	return new JpaSource(collector, axiomName, nameMapList);
	}

	/**
	 * 
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider#getAxiomListener()
	 */
	@Override
	public AxiomListener getAxiomListener() 
	{   
		return new AxiomListener()
		{
			@Override
			public void onNextAxiom(Axiom axiom) 
			{
				if (!TEN_YEAR_AXIOM.equals(axiom.getName()))
					return;
				Agri10Year agri10Year = new Agri10Year();
				Parameter countryParam = (Parameter) axiom.getTermByName("country");
				if (countryParam == null)
					throw new ExpressionException("Axiom \"" + axiom.getName() + "\" does not have a term named \"country\"");
		    	agri10Year.setCountry(new Country(countryParam.getValue().toString()));
				//System.out.println(agri10Year.getCountryName() + " " + ++agri10YearCount);
		    	agri10Year.setId(agri10YearId++);
		    	agri10Year.setSurfaceArea((Double)axiom.getTermByName("surface_area").getValue());
		    	// Do task of persisting Agri10Year asychronously. (Subject to using multi-connection ConnectionSource).
		    	try 
		    	{
		    		agri10YearService.put(agri10Year);
				} 
		    	catch (InterruptedException e) 
		    	{

				}
		    	// Change above line for next two to do task synchronously
				//if (providerManager.doWork(PU_NAME, new PersistAgri10Year(agri10Year)) != WorkStatus.FINISHED)
			    //	throw new QueryExecutionException("Error persisting resource " + getName() + " axiom: " + axiom.toString());
			}
		};
	}
	
}
