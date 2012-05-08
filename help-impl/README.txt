
INTRO
===============================================================================

This is a two part project.  

The Maven part, driven by pom.xml, generates the documentation artifact used by 
the rest of Cytoscape to provide documentation for the application and 
distribution.

The Ant part is used to download the manual from the wiki, download all of the
images from the wiki, and generate a PDF version of the manual.  Maven does
not execute these tasks because they take a long time, burden our wiki, and 
don't generally need to happen more than once or twice in a release cycle 
(i.e. once everyone's updated their portion of the manual). 


THE PROCEDURE
===============================================================================

90% of the time (when you don't need to update): 
------------------------------------------------

1. "mvn install" should be all you need to generate the artifacts needed by
   Cytoscape to build and deploy normally.

2. There is no step 2.


10% of the time (when you DO need to update): 
---------------------------------------------

1. "ant update" will download everything, pre-process the XML, and generate the PDF.

2. Check all the newly generated files into subversion.

3. "mvn install" to generate the artifacts.



ANT TASK EXPLANATION
===============================================================================

"ant"         Shows the usage message. 

"ant update"  Will re-download the manual, all associated images, and regenerate
              the PDF version of the manual.  The project should be left in a 
			  a state such that everything can be added and checked in.  


POTENTIAL GENERATION ERRORS
===============================================================================

"Error: CALS tables must specify the number of columns."



This happens because you see the following structure in the docbook:

   <table cols="1">
       <caption/>
	       <tgroup>
		        <colspec colname="xxx1"/>

where the number of columns is specified in the table element rather than the
tgroup element, where it should be.  The following is correct

   <table>
       <caption/>
	       <tgroup cols="1">
		        <colspec colname="xxx1"/>

This is best solved by fixing the tables in the document rather than trying to
tweak the XML.  This is really a bug in how MoinMoin exports the DocBook.

This problem occurs in the XML when the first row of a table spans some or all 
of the columns in the table.  Simply remove this and the xml is exported
correctly. 

