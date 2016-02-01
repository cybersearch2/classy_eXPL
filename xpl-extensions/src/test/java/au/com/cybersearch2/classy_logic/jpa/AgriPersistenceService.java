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
public class AgriPersistenceService extends PersistenceWorker
{
    private AgricultureJpaTest owner;
    
	public AgriPersistenceService(PersistenceContext persistenceContext, AgricultureJpaTest owner) 
	{
		super(AgricultureJpaTest.PU_NAME, persistenceContext);
		this.owner = owner;
	}

	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
		return owner.getExecutable(persistenceWork);
	}

}
