/**
    Copyright (C) 2016  www.cybersearch2.com.au

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
package au.com.cybersearch2.classy_logic.tutorial1;

import javax.inject.Singleton;

import au.com.cybersearch2.classy_logic.JavaTestResourceEnvironment;
import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.TestSystemEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;

/**
 * CitiesModule
 * @author Andrew Bowley
 * 25Jan.,2017
 */
@Module
public class CitiesModule
{
    public CitiesModule()
    {
        resourcePath = "src/main/resources/tutorial1";
        connectionType = ConnectionType.memory;
    }
    
    private SQLiteDatabaseSupport sqliteDatabaseSupport;
    private String resourcePath;
    private ConnectionType connectionType;

    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
   
    @Provides @Singleton TaskManager provideTaskManager()
    {
        return new TaskManager();
    }

    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new JavaTestResourceEnvironment(resourcePath);
    }

    @Provides @Singleton DatabaseSupport provideDatabaseSupport()
    {
        sqliteDatabaseSupport = new SQLiteDatabaseSupport(connectionType);
        return sqliteDatabaseSupport;    
    }
    
    @Provides @Singleton PersistenceFactory providePersistenceFactory(DatabaseSupport databaseSupport, ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory()
    {
        return sqliteDatabaseSupport;
    }

    @Provides @Singleton PersistenceContext providePersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }
    
    @Provides @Singleton ProviderManager provideProviderManager()
    {
        return new ProviderManager();
    }
}
