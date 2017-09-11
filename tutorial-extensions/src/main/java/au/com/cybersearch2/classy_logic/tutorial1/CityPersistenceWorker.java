/**
 * 
 */
package au.com.cybersearch2.classy_logic.tutorial1;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.Executable;

/**
 * @author andrew
 *
 */
public class CityPersistenceWorker extends PersistenceWorker 
{
	private ApplicationComponent component;

	public CityPersistenceWorker(ApplicationComponent component) 
	{
		super(PersistenceCities.PU_NAME, component.persistenceContext());
		this.component = component;
	}

	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PersistenceCities.PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
	}
}
