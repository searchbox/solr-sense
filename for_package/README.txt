Welcome to Searchbox-Sense!
-----------------------------------

This document explains how Searchbox-Sense plugin can be configured to 
provide a set of Solr request handlers which add out-of-the-box 
*conceptual search* capabilities to a standard Solr installation. 

These capabilities can be understood via two use cases, one for 
each request handler: 

SenseLikeThis (SLT): The user has a document indexed in their Solr instance 
nd wishes to find documents which are conceptually similar. 

Sense: The user has a block of text for which they’d like to find documents 
which are conceptually similar.


We recommend you head over to our website and register your email address 
to receive notifications about software updates and CKBs. We're rolling out 
additional semantic features and CKBs for different domains in the coming
months, so stay tuned!


Getting Started
---------------

For a the complete documentation please see the Searchbox web site at

http://www.searchbox.by/products/searchbox-plugins/searchbox-sense/

and the knoweldge base:

http://help.searchbox.com/solr-plugins/searchbox-sense


   
Files included in this Searchbox-Sense distribution
----------------------------------------------------

./README.txt
	This readme file

./compare_2handlers.py
	A python script on how to compare 2 result producing request 
	handlers (mlt/slt/sltnoredux). For more information check the FAQ
	"How can I compare morelikethis to senselikethis?" here: 
	http://help.searchbox.com/faqs/
	
pubmed_demo/
	A fully functional demonstration core of the Searchbox-Sense technology
	
pubmed_demo/CKB
	The cognitive knowledgebase produced specifically for the included
	pubmed dataset, thus providing very high relevancy for conceptual queries

pubmed_demo/conf
	Configuration files which specify the request handler endpoints and their default
	parameters 
	
pubmed_demo/data
	A subset of about 10,000 documents from the publically available Pubmed dataset
	
pubmed_demo/lib
	The location of the Searchbox-Sense jar file: searchbox-sense-1.37.jar
