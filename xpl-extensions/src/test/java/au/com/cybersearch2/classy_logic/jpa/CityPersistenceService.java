/**
 * 
 */
package au.com.cybersearch2.classy_logic.jpa;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.Executable;

/**
 * @author andrew
 *
 */
public class CityPersistenceService extends PersistenceWorker 
{
	private HighCitiesJpaTest owner;

	public CityPersistenceService(PersistenceContext persistenceContext, HighCitiesJpaTest owner) 
	{
		super(HighCitiesJpaTest.PU_NAME, persistenceContext);
		this.owner = owner;
	}

	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
		return owner.getExecutable(persistenceWork);
	}
}
