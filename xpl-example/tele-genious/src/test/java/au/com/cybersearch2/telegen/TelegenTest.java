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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryParams;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.interfaces.SolutionHandler;
import au.com.cybersearch2.classy_logic.parser.ParseException;
import au.com.cybersearch2.classy_logic.parser.QueryParser;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.Solution;
import au.com.cybersearch2.classy_logic.terms.Parameter;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceWorker;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.entity.Check;
import au.com.cybersearch2.entity.Issue;
import au.com.cybersearch2.entity.TestChecks;
import au.com.cybersearch2.entity.TestIssues;
import dagger.Component;
import dagger.Subcomponent;

/**
 * TelegenTest
 * @author Andrew Bowley
 * 22 May 2015
 */
public class TelegenTest
{
    @Singleton
    @Component(modules = TelegenModule.class)  
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
        PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface PersistenceWorkSubcontext
    {
        Executable executable();
    }

    /** PersistenceUnitAdmin Unit name to look up configuration details in persistence.xml */
    static public final String PU_NAME = "telegen";
    static public final String  SUPPORT_CHECK_NAME = "Support";
    static public final String[] FIRST_CHECK_NAME_LIST = 
    {
        "Power cord",
        "Connections",
        "Batteries",
        "Programme",
        SUPPORT_CHECK_NAME
    };
/*
 *          { "Power cord", "Make sure the AC power cord is securely plugged in to the wall outlet" },
            { "Wall outlet", "Make sure the wall outlet is working" },
            { "Remote", "Try pressing the POWER button on the TV to make sure the problem is nto the remote." },
            { "Connections", "Check cable connections (remove and reconnect all cables connected to the TV nad external devices)" },
            { "Cables", "Set your external device's (Cable/Set Box, DVD, Blu-ray etc) video outputs to match the connections to the TV input." +
              "For example, if an external device's output is HDMI, it should be connected to an HDMI input on the TV." },
            { "Connected devices", "Make sure your connected devices are powered on." },
            { "Source", "Be sure to select the TV's correct source by pressing the SOURCE button on the remote control." },
            { "Running state",  "Reboot the connected device by unplugging and then reconnecting the device's power cable" },
            { "Batteries", "Replace the remote control batteries with the poles (+/-) in the right direction" },
            { "Sensor", "Clean the sensor's transmission window on the remote" },
            { "Pointing", "Try pointing the remote directly at the TV from 5-6 feet away" },
            { "Programme", "Programme the Cable/Set remote control to operate the TV. Refer to the Cable/Set user manual for the SAMSUNG TV code" }
    
 */
    static public final String TELEGEN_XPL =
        "axiom issue() : \"telegen\";\n" +
        "axiom check() : \"telegen\";\n" +
        "axiom issue_param(issue_name): parameter;\n" +
        "axiom check_param(check_name): parameter;\n" +
        "template issue (name, observation);\n" +
        "template check (name, instruction);\n" +
        "choice first_check\n"  +
        "    (  issue_name,           check_name) \n" +
        "    {\"Start\",            \"Power cord\"}\n" +
        "    {\"Video\",            \"Connections\"}\n" +
        "    {\"Remote\",           \"Batteries\"}\n" +
        "    {\"Set top box\",      \"Programme\"}\n" +
        "    {true,                 \"Support\"};\n" +
        "choice next_check\n"  +
        "    (  check_name,           next_check) \n" +
        "    {\"Power cord\",       \"Wall outlet\"}\n" +
        "    {\"Wall outlet\",      \"Remote\"}\n" +
        "    {\"Remote\",           \"Support\"}\n" +
        "    {\"Connections\",      \"Cables\"}\n" +
        "    {\"Cables\",           \"Connected devices\"}\n" +
        "    {\"Connected devices\",\"Source\"}\n" +
        "    {\"Source\",           \"Running state\"}\n" +
        "    {\"Running state\",    \"Support\"}\n" +
        "    {\"Batteries\",        \"Sensor\"}\n" +
        "    {\"Sensor\",           \"Pointing\"}\n" +
        "    {\"Pointing\",         \"Support\"}\n" +
        "    {\"Programme\",        \"Support\"}\n" +
        "    {true,                 \"Support\"};\n" +
        "query issues (issue:issue);\n" +
        "query checks (check:check);\n" +
        "query first_check_query (issue_param: first_check);\n" +
        "query next_check_query (check_param: next_check);";

    private ProviderManager providerManager;
    private ApplicationComponent component;

    @Before
    public void setUp() throws Exception
    {
        // Set up dependency injection
        component = 
                DaggerTelegenTest_ApplicationComponent.builder()
                .telegenModule(new TelegenModule())
                .build();
        getExecutable(new TestIssues()).waitForTask();
        getExecutable(new TestChecks()).waitForTask();
        providerManager = new ProviderManager();
        PersistenceWorker persistenceWorker = new PersistenceWorker(PU_NAME, component.persistenceContext()){

			@Override
			public Executable doWork(PersistenceWork persistenceWork) {
				return getExecutable(persistenceWork);
			}};
       providerManager.putAxiomProvider(new TelegenAxiomProvider(persistenceWorker));
    }
    
    @Test
    public void testTableCreation() throws Exception
    {
        PersistenceWork verificationWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
                int id = 1;
                for (String[] issueItem: TestIssues.ISSUE_DATA)
                {
                    Issue issue = entityManager.find(Issue.class, Integer.valueOf(id)); 
                    assertThat(issue.getName()).isEqualTo(issueItem[0]);
                    assertThat(issue.getObservation()).isEqualTo(issueItem[1]);
                    ++id;
                }
                id = 1;
                for (String[] checkItem: TestChecks.CHECK_DATA)
                {
                    Check check = entityManager.find(Check.class, Integer.valueOf(id)); 
                    assertThat(check.getName()).isEqualTo(checkItem[0]);
                    assertThat(check.getInstruction()).isEqualTo(checkItem[1]);
                    ++id;
                }
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
       getExecutable(verificationWork).waitForTask();
    }

    @Test
    public void testAxiomProvider() throws Exception
    {
        QueryProgram queryProgram = compileScript(TELEGEN_XPL);
        queryProgram.executeQuery("issues", new SolutionHandler(){
            int index = 0;
            @Override
            public boolean onSolution(Solution solution)
            {
                Axiom issue = solution.getAxiom(TelegenAxiomProvider.ISSUE);
                String[] issueItem = TestIssues.ISSUE_DATA[index++];
                assertThat(issue.getTermByName("name").getValue().toString()).isEqualTo(issueItem[0]);
                assertThat(issue.getTermByName("observation").getValue().toString()).isEqualTo(issueItem[1]);
                //System.out.println(solution.getAxiom("issue").toString());
                return true;
            }});
        queryProgram.executeQuery("checks", new SolutionHandler(){
            int index = 0;
            @Override
            public boolean onSolution(Solution solution)
            {
                Axiom check = solution.getAxiom(TelegenAxiomProvider.CHECK);
                String[] checkItem = TestChecks.CHECK_DATA[index++];
                assertThat(check.getTermByName("name").getValue().toString()).isEqualTo(checkItem[0]);
                assertThat(check.getTermByName("instruction").getValue().toString()).isEqualTo(checkItem[1]);
                //System.out.println(solution.getAxiom("check").toString());
                return true;
            }});
    }

    @Test
    public void testFirstCheck() throws Exception
    {
        int index = 0;
        QueryProgram queryProgram = compileScript(TELEGEN_XPL);
        for (String[] issueItem: TestIssues.ISSUE_DATA)
        {
            doFirstCheck(queryProgram, issueItem[0], FIRST_CHECK_NAME_LIST[index]);
            ++index;
        }
    }
    
    @Test
    public void testNextCheck() throws Exception
    {
        int issueIndex = 1;
        int checkIndex = 0;
        QueryProgram queryProgram = compileScript(TELEGEN_XPL);
        for (String[] checkItem: TestChecks.CHECK_DATA)
        {
            //TestIssues.ISSUE_DATA
            String check = checkItem[0];
            if (check.equals(SUPPORT_CHECK_NAME))
                break;
            String nextCheck = TestChecks.CHECK_DATA[checkIndex + 1][0];
            if (nextCheck.equals(FIRST_CHECK_NAME_LIST[issueIndex]))
            {
                nextCheck = SUPPORT_CHECK_NAME;
                ++issueIndex;
            }
            doNextCheck(queryProgram, check, nextCheck);
            ++checkIndex;
        }
    }

    public Executable getExecutable(PersistenceWork persistenceWork)
    {
    	PersistenceWorkModule persistenceWorkModule = new PersistenceWorkModule(PU_NAME, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }
   
    protected void doFirstCheck(QueryProgram queryProgram, String issue, final String check) throws Exception
    {
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "first_check_query");
        // Add a transacton_amount Axiom with a single 123,458 term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("issue_param", new Axiom("issue_param", new Parameter("issue_name", issue)));
       // Add a solution handler to display the final Calculator solution
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getString("first_check", "check_name")).isEqualTo(check);
                return true;
            }});
        queryProgram.executeQuery(queryParams);
    }

    protected void doNextCheck(QueryProgram queryProgram, String check, final String nextcheck) throws Exception
    {
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "next_check_query");
        // Add a transacton_amount Axiom with a single 123,458 term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("check_param", new Axiom("check_param", new Parameter("check_name", check)));
        // Add a solution handler to display the final Calculator solution
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getString("next_check", "next_check")).isEqualTo(nextcheck);
                return true;
            }});
        queryProgram.executeQuery(queryParams);
    }

    protected QueryProgram compileScript(String script) throws ParseException
    {
        InputStream stream = new ByteArrayInputStream(script.getBytes());
        QueryParser queryParser = new QueryParser(stream);
        QueryProgram queryProgram = new QueryProgram(providerManager);
        queryProgram.setResourceBase(new File("src/main/resources"));
        queryParser.input(queryProgram);
        return queryProgram;
    }
    
}
