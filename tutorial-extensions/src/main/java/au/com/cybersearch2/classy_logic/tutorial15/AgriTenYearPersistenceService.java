/**
 * 
 */
package au.com.cybersearch2.classy_logic.tutorial15;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;
import au.com.cybersearch2.classytask.Executable;

/**
 * @author andrew
 *
 */
public class AgriTenYearPersistenceService extends PersistenceService<Agri10Year> 
{
	private ApplicationComponent component;

	public AgriTenYearPersistenceService(ApplicationComponent component) 
	{
		super(PersistenceAgriculture.PU_NAME, component.persistenceContext());
		this.component = component;
	}


	/**
	 * PersistAgri10Year
	 * Set up task to create Agri10Year table in test database. 
	 * This requires fetching Country objects to insert in Agri10Year items.
	 * See AgriDatabase for construction of test database.
	 * @author Andrew Bowley
	 * 23 May 2015
	 */
	static class PersistAgri10Year implements PersistenceWork
	{
		protected Agri10Year agri10Year;

		/**
		 * Construct PersistAgri10Year object
		 * @param agri10Year The object to persist
		 */
		public PersistAgri10Year(Agri10Year agri10Year)
		{
			this.agri10Year =agri10Year;
		}

		/**
		 * Use OrmLite to run query to fetch Country object referenced by name in supplied Agri10Year object
		 * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
		 */
        @Override
	    public void doTask(EntityManagerLite entityManager)
	    {
	    	// Use OrmLite query to get Country object from database 
            EntityManagerDelegate delegate = (EntityManagerDelegate)entityManager.getDelegate();
            @SuppressWarnings("unchecked")
            PersistenceDao<Country, Integer> countryDao = 
                    (PersistenceDao<Country, Integer>) delegate.getDaoForClass(Country.class);
            QueryBuilder<Country, Integer> statementBuilder = countryDao.queryBuilder();
            SelectArg selectArg = new SelectArg();
            // build a query with the WHERE clause set to 'country = ?'
            PreparedQuery<Country> preparedQuery = null;
            try
            {
            	statementBuilder.where().eq("country", selectArg);
            	preparedQuery = statementBuilder.prepare();
            }
            catch (SQLException e)
            {
            	throw new PersistenceException("Database error", e);
            }
            // Now we can set the select arg (?) and run the query
            selectArg.setValue(agri10Year.getCountryName());
            List<Country> results = countryDao.query(preparedQuery);
            if (results.size() > 0)
            {
	            Country country = results.get(0);
	            entityManager.merge(country);
	            agri10Year.setCountry(country);
		    	entityManager.persist(agri10Year);
            }
            else
            	System.err.println("Cannot find country \"" + agri10Year.getCountryName() + "\"");
	    }

	    @Override
	    public void onPostExecute(boolean success)
	    {
	        if (!success)
	            throw new IllegalStateException("Database error.");
	    }

	    @Override
	    public void onRollback(Throwable rollbackException)
	    {
	    	//throw new IllegalStateException("Database error.", rollbackException);
	    	System.err.println(rollbackException.toString());
	    }
    }
	

	@Override
	public void onEntityReceived(Agri10Year entity) 
	{
		try 
		{
			doWork(new PersistAgri10Year(entity)).waitForTask();
		} 
		catch (InterruptedException e) 
		{
		}
	}


	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PersistenceAgriculture.PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
	}

}
