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

/**
 * NameMap
 * @author Andrew Bowley
 * 19 Mar 2015
 */
public class NameMap 
{
	protected String termName;
	protected String fieldName;
	
	/**
	 *  Construct a NameMap object
	 *  @param termName
	 *  @param fieldName
	 */
	public NameMap(String termName, String fieldName) 
	{
		this.termName = termName;
		this.fieldName = fieldName;
	}

	/**
	 * @return the termName
	 */
	public String getTermName() 
	{
		return termName;
	}

	/**
	 * @param termName the termName to set
	 */
	public void setTermName(String termName) 
	{
		this.termName = termName;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() 
	{
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) 
	{
		this.fieldName = fieldName;
	}

}
