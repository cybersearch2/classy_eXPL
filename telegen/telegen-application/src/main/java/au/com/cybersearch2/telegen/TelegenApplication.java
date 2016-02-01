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
package au.com.cybersearch2.telegen;

import javax.persistence.PersistenceException;

import com.j256.ormlite.dao.DaoManager;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.AsyncBackgroundTask;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.entity.Check;
import au.com.cybersearch2.entity.Issue;
import au.com.cybersearch2.telegen.module.TelegenApplicationModule;

/**
 * TelegenApplication
 * Launches Telegen persistence 
 * @author Andrew Bowley
 * 19 Jun 2015
 */
public class TelegenApplication extends Application
{

    public static final String PU_NAME = "telegen";
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android
    
    public static final String TAG = "TelegenApplication";
    private static TelegenApplication singleton;
    protected TelegenComponent telegenComponent;
    protected Object startMonitor;

    public TelegenApplication()
    {
        startMonitor = new Object();
       singleton = this;
    }
    
    @Override public void onCreate() 
    {
        super.onCreate();
        init(this);
    }

    public void init(final Context context)
    {
        AsyncBackgroundTask starter = new AsyncBackgroundTask(context)
        {
            @Override
            public Boolean loadInBackground()
            {
                Log.i(TAG, "Loading in background...");
                // Get perisistence context to trigger database initialization
                // Build Dagger2 configuration
                if (Log.isLoggable(TAG, Log.INFO))
                    Log.i(TAG, "ClassyFy application Dagger build");
                try
                {
                    // Clear out ORMLite internal caches.
                    DaoManager.clearCache();
                    // Create application Object Graph for Dependency Injection
                    telegenComponent = 
                            DaggerTelegenComponent.builder()
                            .telegenApplicationModule(new TelegenApplicationModule(context))
                            .build();
                    //startApplicationSetup(telegenComponent.persistenceContext());
                    // Get persistence context first time initializes databases
                    PersistenceContext persistenceContext = telegenComponent.persistenceContext();
                    PersistenceWorker issueWorker = new PersistenceWorker(PU_NAME, persistenceContext){

                        @Override
                        public Executable doWork(PersistenceWork persistenceWork) {
                            return getExecutable(persistenceWork);
                        }};
                    PersistenceWorker checkWorker = new PersistenceWorker(PU_NAME, persistenceContext){

                        @Override
                        public Executable doWork(PersistenceWork persistenceWork) {
                            return getExecutable(persistenceWork);
                        }};
                    telegenComponent.providerManager()
                    .putAxiomProvider(new TelegenAxiomProvider(issueWorker, checkWorker));

                }
                catch (PersistenceException e)
                {
                    Log.e(TAG, "Database error on initialization", e);
                    return Boolean.FALSE;
                }
                synchronized(startMonitor)
                {
                     startMonitor.notifyAll();
                }
                return Boolean.TRUE;
            }
        };
        starter.startLoading();
    }

    /**
     * Returns Dagger2 application component but blocks if
     * it is not available due to initialization in progress.
     * NOTE
     * MainActivity and other injectees must call inject() from 
     * background thread when responding to OnCreate event.
     * @return
     */
    public TelegenComponent getTelegenComponent()
    {
        if (telegenComponent == null)
        {
            synchronized(startMonitor)
            {
                if (telegenComponent == null)
                    try
                    {
                        startMonitor.wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
            }
        }
        return telegenComponent;
    }

    public static TelegenApplication getInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TelegenApplication called while not initialized");
        return singleton;
    }

    protected Executable getExecutable(PersistenceWork persistenceWork)
    {
        PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return telegenComponent.plus(persistenceWorkModule).executable();
    }
}
