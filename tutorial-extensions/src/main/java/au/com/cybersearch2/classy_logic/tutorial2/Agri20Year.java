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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.terms.Parameter;

/**
 * Agri20Year
 * @author Andrew Bowley
 * 19 Mar 2015
 */
@Entity(name = "Agri20Year")
public class Agri20Year 
{
    /** Identity value for next Agri20YearId object to be created */
    static int agri20YearId;
	static public final String COUNTRY_TERM = "country";
	static public final String SURFACE_AREA_TERM = "surface_area";
    
	public Agri20Year() 
	{
	}

    public Agri20Year(Axiom axiom) 
    {
        Parameter countryParam = (Parameter) axiom.getTermByName(COUNTRY_TERM);
        if (countryParam == null)
            throw new ExpressionException("Axiom \"" + axiom.getName() + "\" does not have a term named \"" + COUNTRY_TERM + "\"");
        setCountry(countryParam.getValue().toString());
        //System.out.println(agri20Year.getCountryName() + " " + ++agri20YearCount);
        setId(agri20YearId++);
        setSurfaceArea((Double)axiom.getTermByName(SURFACE_AREA_TERM).getValue());
    }
    
	/** This id is generated by the application */
    @Id
 	int id;

    @Column(nullable = false)
    String country;
	
	@Column(nullable = false)
	Double surfaceArea;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Double getSurfaceArea() {
		return surfaceArea;
	}

	public void setSurfaceArea(Double surfaceArea) {
		this.surfaceArea = surfaceArea;
	}

	/**
	 * @return the country name
	 */
	public String getCountry () 
	{
		return country;
	}

	/**
	 * @param countryName the country name to set
	 */
	public void setCountry(String country) 
	{
		this.country = country;
	}
	
	
}