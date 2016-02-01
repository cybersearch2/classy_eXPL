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
package au.com.cybersearch2.classy_logic.tutorial14;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * CitiesDatabase persists City enity objects. 
 * Note This is an option provided to facilitate testing and an actual implementation
 * would have some other arrangement for creating and populating a database.
 * @author Andrew Bowley
 * 18 Mar 2015
 */
public class CitiesDatabase implements PersistenceWork 
{

    @Override
    public void doTask(EntityManagerLite entityManager)
    {
    	entityManager.persist(new City("bilene", 1718));
    	entityManager.persist(new City("addis ababa", 8000));
    	entityManager.persist(new City("denver", 5280));
    	entityManager.persist(new City("flagstaff", 6970));
    	entityManager.persist(new City("jacksonville", 8));
    	entityManager.persist(new City("leadville", 10200));
    	entityManager.persist(new City("madrid", 1305));
    	entityManager.persist(new City("richmond",19));
    	entityManager.persist(new City("spokane", 1909));
    	entityManager.persist(new City("wichita", 1305));
        // Database updates commited upon exit
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
}
