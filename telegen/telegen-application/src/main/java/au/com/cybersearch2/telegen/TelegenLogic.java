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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.classy_logic.ProviderManager;
import au.com.cybersearch2.classy_logic.QueryParams;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.interfaces.SolutionHandler;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.Solution;
import au.com.cybersearch2.classy_logic.terms.Parameter;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * TelegenLogic
 * @author Andrew Bowley
 * 17 Jun 2015
 */
public class TelegenLogic
{
    static public final String CALL_SUPPORT = "Support";
    
    static protected final String TELEGEN_XPL =
            "axiom issue() : resource \"telegen\";\n" +
            "axiom check() : resource \"telegen\";\n" +
            "axiom issue_param(issue_name): parameter;\n" +
            "axiom check_param(check_name): parameter;\n" +
            "template issue_item (name, observation);\n" +
            "choice first_check\n"  +
            "    (  issue_name,           check_name) \n" +
            "    {\"Start\",            \"Power cord\"}\n" +
            "    {\"Video\",            \"Connections\"}\n" +
            "    {\"Remote\",           \"Batteries\"}\n" +
            "    {\"Set top box\",      \"Programme\"}\n" +
            "    {true,                 \"" + CALL_SUPPORT + "\"};\n" +
            "choice next_check\n"  +
            "    (  check_name,           next_check) \n" +
            "    {\"Power cord\",       \"Wall outlet\"}\n" +
            "    {\"Wall outlet\",      \"Remote\"}\n" +
            "    {\"Remote\",           \"" + CALL_SUPPORT + "\"}\n" +
            "    {\"Connections\",      \"Cables\"}\n" +
            "    {\"Cables\",           \"Connected devices\"}\n" +
            "    {\"Connected devices\",\"Source\"}\n" +
            "    {\"Source\",           \"Running state\"}\n" +
            "    {\"Running state\",    \"" + CALL_SUPPORT + "\"}\n" +
            "    {\"Batteries\",        \"Sensor\"}\n" +
            "    {\"Sensor\",           \"Pointing\"}\n" +
            "    {\"Pointing\",         \"" + CALL_SUPPORT + "\"}\n" +
            "    {\"Programme\",        \"" + CALL_SUPPORT + "\"}\n" +
            "    {true,                 \"" + CALL_SUPPORT + "\"};\n" +
            "template first_check_item (name ? name == check_name, instruction);\n" +
            "template next_check_item (name ? name == next_check, instruction);\n" +
            "list issues_list(issue_item);\n" +
            "query issues (issue:issue_item);\n" +
            "query first_check_query (issue_param: first_check) >> (check:first_check_item);\n" +
            "query next_check_query (check_param: next_check) >> (check:next_check_item);";

    public static final String ISSUES_QUERY = "issues";
    
    protected String currentQuery;
    protected String currentCheck;
    protected QueryProgram queryProgram;

    public TelegenLogic(ProviderManager providerManager)
    {
        queryProgram = new QueryProgram(providerManager);
        queryProgram.parseScript(TELEGEN_XPL);
    }
 
    public String getCurrentQuery()
    {
        return currentQuery;
    }

    public String getCurrentCheck()
    {
        return currentCheck;
    }

    public List<ListItem> getIssues()
    {
        List<ListItem> fieldList = new ArrayList<ListItem>();
        currentQuery = ISSUES_QUERY;
        Result result = queryProgram.executeQuery(ISSUES_QUERY);
        Iterator<Axiom> iterator = result.getIterator(QualifiedName.parseGlobalName("issues_list"));
        while (iterator.hasNext())
        {
            Axiom axiom =iterator.next();
            fieldList.add(new ListItem(axiom.getTermByName("name").getValue().toString(), 
                                    axiom.getTermByName("observation").getValue().toString()));
        }
        return fieldList;
    }

/*
    protected Iterator<AxiomTermList> getChecks()
    {
        currentQuery = CHECKS_QUERY;
        Result result = queryProgram.executeQuery(CHECKS_QUERY);
        return result.getIterator("checks_list");
    }
*/
    public String getFirstCheck(String issue)
    {
        // Create QueryParams object for Global scope and query "first_check_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "first_check_query");
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("issue_param", new Axiom("issue_param", new Parameter("issue_name", issue)));
        final String[] checkHolder = new String[1];
        // Add a solution handler to capture the query result
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                currentCheck = solution.getString("first_check_item", "name");
                checkHolder[0] = solution.getString("first_check_item", "instruction");
                // End query
                return false;
            }});
        queryProgram.executeQuery(queryParams);
        return checkHolder[0];
    }

    public String getNextCheck()
    {
        // Create QueryParams object for Global scope and query "next_check_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "next_check_query");
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("check_param", new Axiom("check_param", new Parameter("check_name", currentCheck)));
        final String[] checkHolder = new String[1];
        // Add a solution handler to capture the query result
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                currentCheck = solution.getString("next_check_item", "name");
                checkHolder[0] = solution.getString("next_check_item", "instruction");
                // End query
                return false;
            }});
        queryProgram.executeQuery(queryParams);
        return checkHolder[0];
    }


}
