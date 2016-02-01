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
package au.com.cybersearch2.telegen.module;

import javax.inject.Singleton;

import android.content.Context;
import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classydb.AndroidConnectionSourceFactory;
import au.com.cybersearch2.classydb.AndroidSqliteParams;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.OpenEventHandler;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.telegen.TelegenApplication;
import au.com.cybersearch2.telegen.TelegenLogic;
import dagger.Module;
import dagger.Provides;

/**
 * TelegenApplicationModule
 * @author Andrew Bowley
 * 25 Jan 2016
 */
@Module(includes = TelegenEnvironmentModule.class)
public class TelegenApplicationModule
{
    private Context context;
    
    public TelegenApplicationModule(Context context)
    {
        this.context = context;
    }

    /**
     * Returns Android Application Context
     * @return Context
     */
    @Provides @Singleton Context provideContext()
    {
        return context;
    }

    @Provides @Singleton OpenEventHandler provideOpenEventHandler(Context context, PersistenceFactory persistenceFactory)
    {
        // NOTE: This class extends Android SQLiteHelper 
        return new OpenEventHandler(new AndroidSqliteParams(context, TelegenApplication.PU_NAME, persistenceFactory));
    }
    
    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory(OpenEventHandler openEventHandler)
    {
        return new AndroidConnectionSourceFactory(openEventHandler);
    }
    
    @Provides @Singleton PersistenceContext providePersistenceContext(
            PersistenceFactory persistenceFactory, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }

    @Provides @Singleton ProviderManager provideProviderManager()
    {
        ProviderManager providerManager = new ProviderManager();
        return providerManager;
    }
    
    @Provides @Singleton TelegenLogic provideTelegenLogic(ProviderManager providerManager)
    {
        return new TelegenLogic(providerManager);
    }
}
