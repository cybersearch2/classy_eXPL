/**
    Copyright (C) 2015  www.cybersearch2.com.au

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
package au.com.cybersearch2.classy_logic.tutorial16;

import java.util.Iterator;

import au.com.cybersearch2.classy_logic.FunctionManager;
import au.com.cybersearch2.classy_logic.QueryProgram;
import au.com.cybersearch2.classy_logic.Result;
import au.com.cybersearch2.classy_logic.expression.ExpressionException;
import au.com.cybersearch2.classy_logic.helper.QualifiedName;
import au.com.cybersearch2.classy_logic.pattern.Axiom;
import au.com.cybersearch2.classy_logic.query.QueryExecutionException;

/**
 * SchoolMarks
 * Demonstrates library function call
 * @author Andrew Bowley
 * 14 Sep 2015
 */
public class SchoolMarks
{
    static final String GRADES_SUM = 
            "axiom grades (student, english, math, history)\n" +
                " {\"Amy\", 14, 16, 6}\n" +
                " {\"George\", 15, 13, 16}\n" +
                " {\"Sarah\", 12, 17, 14};\n" +
            "template score(student, integer total = math.add(english, math, history));\n" +
            "list report(score);\n" +
            "query marks(grades : score);";

    /**
     * Compiles the GRADES_SUM script and runs the "marks" query.<br/>
     * The expected results:<br/>
        score(student = Amy, total = 36)<br/>
        score(student = George, total = 44)<br/>
        score(student = Sarah, total = 43)<br/>
     * @return Axiom iterator
     */
    public Iterator<Axiom>  generateReport()
    {
        QueryProgram queryProgram = new QueryProgram(provideFunctionManager());
        queryProgram.parseScript(GRADES_SUM);
        Result result = queryProgram.executeQuery("marks");
        return result.getIterator(QualifiedName.parseGlobalName("report"));
    }

    FunctionManager provideFunctionManager()
    {
        FunctionManager functionManager = new FunctionManager();
        MathFunctionProvider mathFunctionProvider = new MathFunctionProvider();
        functionManager.putFunctionProvider(mathFunctionProvider.getName(), mathFunctionProvider);
        return functionManager;
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            SchoolMarks schoolMarks = new SchoolMarks();
            Iterator<Axiom> iterator = schoolMarks.generateReport();
            while(iterator.hasNext())
            {
                System.out.println(iterator.next().toString());
            }
        } 
        catch (ExpressionException e) 
        { 
            e.printStackTrace();
            System.exit(1);
        }
        catch (QueryExecutionException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
