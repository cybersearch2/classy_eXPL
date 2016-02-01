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

import au.com.cybersearch2.classy_logic.jpa.EntityAxiomProvider;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.entity.Check;
import au.com.cybersearch2.entity.Issue;

/**
 * TelegenAxiomProvider
 * @author Andrew Bowley
 * 24 May 2015
 */
public class TelegenAxiomProvider extends EntityAxiomProvider
{
    static public final String ISSUE = "issue";
    static public final String CHECK = "check";
    
    /**
     * TelegenAxiomProvider
     */
    public TelegenAxiomProvider(
        PersistenceWorker issueWorker, 
        PersistenceWorker checkWorker)
    {
        this(issueWorker, checkWorker, null);

    }

    /**
     * TelegenAxiomProvider
     */
    public TelegenAxiomProvider(
        PersistenceWorker issueWorker, 
        PersistenceWorker checkWorker,
        PersistenceWork setUpTask)
    {
        super("telegen", setUpTask);
        addEntity(ISSUE, Issue.class, issueWorker);
        addEntity(CHECK, Check.class, checkWorker);
    }

}
