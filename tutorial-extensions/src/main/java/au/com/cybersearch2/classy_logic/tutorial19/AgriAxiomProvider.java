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
package au.com.cybersearch2.classy_logic.tutorial19;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.Axiom;

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
    /** Axiom source name for countries which increased agricultural surface area over 10 year interval */
    static public final String TEN_YEAR_AXIOM = "surface_area_increase";
    
    
    protected AgriTenYearPersistenceService agri10YearService;
    
	/**
	 * Construct AgriAxiomProvider object
	 */
	public AgriAxiomProvider(
	        AgriTenYearPersistenceService agri10YearService) 
	{
	    // Super class will construct TEN_YEAR_AXIOM collector
		super("agri_10_year", agri10YearService);
		this.agri10YearService = agri10YearService;
		//this.persistenceService = persistenceWorker;
		addEntity(TEN_YEAR_AXIOM, Agri10Year.class);
	}

	/**
	 * Returns Axiom Provider identity
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider#getName()
	 */
	@Override
	public String getName() 
	{
		return "agri_10_year";
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
		JpaEntityCollector<Agri10Year> collector = (JpaEntityCollector<Agri10Year>) collectorMap.get(axiomName);
		if (TEN_YEAR_AXIOM.equals(axiomName))
		{
			if (axiomTermNameList != null)
			{
				for (String termName: axiomTermNameList)
				{
					NameMap nameMap = null;
					if (termName.equals(Agri10Year.COUNTRY_TERM))
			    	    nameMap = new NameMap(Agri10Year.COUNTRY_TERM, "country");
					else if (termName.equals(Agri10Year.SURFACE_AREA_TERM))
		    	        nameMap = new NameMap(Agri10Year.SURFACE_AREA_TERM, "surfaceArea");
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
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider#getAxiomListener()
	 */
	@Override
	public AxiomListener getAxiomListener(String name) 
	{   
		return new AxiomListener()
		{
			@Override
			public void onNextAxiom(QualifiedName qname, Axiom axiom) 
			{
				if (!TEN_YEAR_AXIOM.equals(axiom.getName()))
					return;
				Agri10Year agri10Year = new Agri10Year(axiom);
		    	// Do task of persisting Agri10Year asychronously. (Subject to using multi-connection ConnectionSource).
                try 
                {
                    agri10YearService.incrementCount();
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
