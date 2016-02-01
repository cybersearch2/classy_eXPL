# xpl
Classy Logic is a Java package for finding solutions to logical problems. It features 
a new query language called "Expression Pattern Language" (XPL) which aligns 
facts to match with solution-defining patterns.

It features a new query language called "Expression Pattern Language" (XPL) which aligns facts to match 
with solution-defining patterns. The inspiration for XPL comes from Prolog, a logic programming language 
originating in the early 1970s. 

Expert systems are one significant application for logic programming languages eg. a system to allow 
technicians diagnose service issues regarding piece of equipment. XPL is a query language implemented 
in Java and borrows concepts from Prolog to allow Java programmers develop expert systems by extending 
Java rather than having to use a different language. XPL also provides an alternative to Structure Query 
Language (SQL) with new ways to express logic and integrate with data sources.  

Classy Logic has a "unification" engine which is accessible programmatically as well as through XPL. 
A unification step is an attempt to pair a collection of facts called an "axiom" with a "template" 
consisting of constant and variable terms. Successful unification results in the axiom and template 
being equivalent and evaluation of the template to produce a solution. 

There is a great deal of flexibility in how Logic queries can be formed, for example, whether to 
repeatedly perform unification to find all solutions, or just find one solution. Queries can be 
chained to refine a solution or incorporate multiple axiom sources - analogous to SQL cartesian 
joins. Calculations are supported to assist with generating aggregate values, sorting and other 
data processing functions.


=== Classy logic features ===
   * Template term evaluation uses Java syntax
   * Templates can generate axioms for flexibility, adaptability
   * Internationalization and regular expressions support
   * Random access lists supported
   * Open Source GPLv3 license
   * Classy Tools library dependency provides lightweight JPA support
   * Tutorial with code exampled for getting up to speed with XPL

Classy Logic uses Classy Tools as a foundation library, so shares with it the use of Dagger Dependency, 
Bean utilities and most importantly, a Java Persistence API implementation.


