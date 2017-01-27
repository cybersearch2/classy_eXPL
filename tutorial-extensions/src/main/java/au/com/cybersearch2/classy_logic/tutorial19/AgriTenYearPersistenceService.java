/**
 * 
 */
package au.com.cybersearch2.classy_logic.tutorial19;

import java.util.concurrent.atomic.AtomicInteger;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceService;
import au.com.cybersearch2.classytask.Executable;

/**
 * AgriTenYearPersistenceService
 * Persistence service for Agri10Year entity
 * @author Andrew Bowley
 * 23 May 2015
 */
public class AgriTenYearPersistenceService extends PersistenceService<Agri10Year> 
{
	private ApplicationComponent component;
    protected AtomicInteger referenceCount;

	public AgriTenYearPersistenceService(ApplicationComponent component) 
	{
		super(IncreasedAgriculture3.PU_NAME, component.persistenceContext());
		this.component = component;
        referenceCount = new AtomicInteger();
	}

    public int incrementCount()
    {
        return referenceCount.incrementAndGet();
    }

    public int getCount()
    {
        return referenceCount.get();
    }
    
	/**
	 * PersistAgri10Year
	 * Set up task to create Agri10Year table in test database. 
	 * This requires fetching Country objects to insert in Agri10Year items.
	 * See AgriDatabase for construction of test database.
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
		   	entityManager.persist(agri10Year);
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

	/**
	 * @see au.com.cybersearch2.classyjpa.persist.PersistenceService#onEntityReceived(java.lang.Object)
	 */
	@Override
	public void onEntityReceived(Agri10Year entity) 
	{
		try 
		{
			doWork(new PersistAgri10Year(entity)).waitForTask();
            if (referenceCount.decrementAndGet() == 0)
                synchronized(this)
                {
                    notifyAll();
                }
		} 
		catch (InterruptedException e) 
		{
		}
	}

    /**
     * @see au.com.cybersearch2.classyjpa.persist.PersistenceWorker#doWork(au.com.cybersearch2.classyjpa.entity.PersistenceWork)
     */
	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(IncreasedAgriculture3.PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
	}

}
