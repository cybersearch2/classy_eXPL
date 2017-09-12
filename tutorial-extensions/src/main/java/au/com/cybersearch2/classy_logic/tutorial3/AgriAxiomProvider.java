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
package au.com.cybersearch2.classy_logic.tutorial3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider;
import au.com.cybersearch2.classy_logic.jpa.JpaEntityCollector;
import au.com.cybersearch2.classy_logic.jpa.JpaSource;
import au.com.cybersearch2.classy_logic.jpa.NameMap;
import au.com.cybersearch2.classy_logic.pattern.AxiomArchetype;
import au.com.cybersearch2.classy_logic.pattern.Axiom;

/**
 * AgriResourceProvider
 * An example of an Axiom Provider for more than one Axiom source.
 * The "surface_area_increase" Axiom source has a simple entity collector,
 * but the "Data" axiom source has a custom collector which fetches data in batch mode
 * for scalability and assembles axiom terms from two tables.
 * 
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class AgriAxiomProvider extends EntityResourceProvider 
{
    /** Axiom source name for countries which increased agricultural surface area over 10 year interval */
    static public final String TWENTY_YEAR_AXIOM = "surface_area_increase";
    
    
    protected Agri20YearPersistenceService agri20YearService;
    
	/**
	 * Construct AgriResourceProvider object
	 */
	public AgriAxiomProvider(
	        Agri20YearPersistenceService agri20YearService) 
	{
	    // Super class will construct TWENTY_YEAR_AXIOM collector
		super("agri_20_year", agri20YearService);
		this.agri20YearService = agri20YearService;
		//this.persistenceService = persistenceWorker;
		addEntity(TWENTY_YEAR_AXIOM, Agri20Year.class);
	}

	/**
	 * Returns Axiom Provider identity
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider#getName()
	 */
	@Override
	public String getName() 
	{
		return "agri_20_year";
	}

	/**
	 * 
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider#getAxiomSource(java.lang.String, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Axiom> iterator(AxiomArchetype archetype) 
	{
		JpaEntityCollector<Agri20Year> collector = (JpaEntityCollector<Agri20Year>) collectorMap.get(archetype.getName());
		if (!TWENTY_YEAR_AXIOM.equals(archetype.getName()))
			throw new IllegalArgumentException("Axiom name \"" + archetype.getName() + "\" not valid for Axiom Provider \"" + getName() + "\"");
        List<NameMap> nameMapList = new ArrayList<NameMap>();
        nameMapList.add(new NameMap(Agri20Year.COUNTRY_TERM, "country"));
        nameMapList.add(new NameMap(Agri20Year.SURFACE_AREA_TERM, "surfaceArea"));
        nameMapList.add(new NameMap("id", "id"));
        archetype.clearMutable();
        JpaSource jpaSource = new JpaSource(collector, archetype, nameMapList);
     	return jpaSource.iterator();
	}

	/**
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider#getAxiomListener()
	 */
	@Override
	public AxiomListener getAxiomListener(String name) 
	{   
		return new AxiomListener()
		{
			@Override
			public void onNextAxiom(QualifiedName qname, Axiom axiom) 
			{
				if (!TWENTY_YEAR_AXIOM.equals(axiom.getName()))
					return;
				Agri20Year agri20Year = new Agri20Year(axiom);
		    	// Do task of persisting Agri20Year asychronously. (Subject to using multi-connection ConnectionSource).
                try 
                {
                    agri20YearService.incrementCount();
                    agri20YearService.put(agri20Year);
                } 
                catch (InterruptedException e) 
                {

                }
		    	// Change above line for next two to do task synchronously
				//if (providerManager.doWork(PU_NAME, new PersistAgri10Year(agri20Year)) != WorkStatus.FINISHED)
			    //	throw new QueryExecutionException("Error persisting resource " + getName() + " axiom: " + axiom.toString());
			}
		};
	}
	
}
