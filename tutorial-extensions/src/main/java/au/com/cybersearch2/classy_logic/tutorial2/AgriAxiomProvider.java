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

import java.util.List;

import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.AxiomListener;
import au.com.cybersearch2.classy_logic.interfaces.AxiomSource;
import au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;

/**
 * AgriAxiomProvider
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class AgriAxiomProvider extends EntityAxiomProvider 
{
    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "agriculture";
    /** Axiom source name for countries which increased agricultural surface area over 10 year interval */
    static public final String TEN_YEAR_AXIOM = "surface_area_increase";
    
    
    protected Agri20YearPersistenceService agri10YearService;
    
	/**
	 * Construct AgriAxiomProvider object
	 */
	public AgriAxiomProvider(
	        Agri20YearPersistenceService agri10YearService) 
	{
	    // Super class will construct TWENTY_YEAR_AXIOM collector
		super(PU_NAME, agri10YearService);
		this.agri10YearService = agri10YearService;
	}

	public PersistenceService<Agri20Year> getPersistenceService()
	{
	    return agri10YearService;
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
				Agri20Year agri20Year = new Agri20Year(axiom);
		    	// Do task of persisting Agri20Year asychronously. (Subject to using multi-connection ConnectionSource).
		    	try 
		    	{
		    	    agri10YearService.incrementCount();
		    		agri10YearService.put(agri20Year);
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
