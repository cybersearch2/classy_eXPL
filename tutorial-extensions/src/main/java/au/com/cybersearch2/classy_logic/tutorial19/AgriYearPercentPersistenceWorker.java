/**
 * 
 */
package au.com.cybersearch2.classy_logic.tutorial19;

import au.com.cybersearch2.classy_logic.agriculture.AgricultureComponent;
import au.com.cybersearch2.classy_logic.agriculture.Agriculture;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.Executable;

/**
 * AgriYearPercentPersistenceWorker
 * Persistence service for Agri10Year entity
 * @author Andrew Bowley
 * 23 May 2015
 */
public class AgriYearPercentPersistenceWorker extends PersistenceWorker
{
	private AgricultureComponent component;

	public AgriYearPercentPersistenceWorker(AgricultureComponent component) 
	{
		super(Agriculture.PU_NAME, component.persistenceContext());
		this.component = component;
	}

	@Override
	public Executable doWork(PersistenceWork persistenceWork) 
	{
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(Agriculture.PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
	}

}
