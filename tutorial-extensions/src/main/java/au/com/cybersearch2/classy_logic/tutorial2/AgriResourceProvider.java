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

import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;

/**
 * AgriResourceProvider
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class AgriResourceProvider extends EntityResourceProvider 
{
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";
    /** Axiom source name for countries which increased agricultural surface area over 20 year interval */
    static public final String TEN_YEAR_AXIOM = "surface_area_increase";
    
    
    protected Agri20YearPersistenceService agri20YearService;
    
	/**
	 * Construct AgriResourceProvider object
	 */
	public AgriResourceProvider(
	        Agri20YearPersistenceService agri20YearService) 
	{
	    // Super class will construct TWENTY_YEAR_AXIOM collector
		super(PU_NAME, agri20YearService);
		this.agri20YearService = agri20YearService;
	}

	public PersistenceService<Agri20Year> getPersistenceService()
	{
	    return agri20YearService;
	}
	
	/**
	 * Returns Axiom Provider identity
	 * @see au.com.cybersearch2.classy_logic.jpa.EntityResourceProvider#getName()
	 */
	@Override
	public String getName() 
	{
		return "agriculture";
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
				if (!TEN_YEAR_AXIOM.equals(axiom.getName()))
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
				//if (providerManager.doWork(PU_NAME, new PersistAgri20Year(agri20Year)) != WorkStatus.FINISHED)
			    //	throw new QueryExecutionException("Error persisting resource " + getName() + " axiom: " + axiom.toString());
			}
		};
	}
	
}
