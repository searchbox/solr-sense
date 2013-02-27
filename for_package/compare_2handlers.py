#!/usr/bin/env python

import sys
import glob
import fnmatch
import os
import httplib2

if(len(sys.argv)!=4):
    print "USAGE: handler1 handler2 id \nExample: slt mlt 56f1c0c3-b6a5-4460-a326-89e74ab13cd7"
    os._exit(0)

handlers=[sys.argv[1],sys.argv[2]]    #["slt","mlt"]
id=sys.argv[3] 		             #"56f1c0c3-b6a5-4460-a326-89e74ab13cd7"


testtops=[1,5,10,25,50,100]
headers={'Content-Type': 'text/plain'}

h = httplib2.Http()
results={}
for handler in handlers:
    print "Exectuing %s"%handler
    testurl='http://localhost:8983/pubmed_demo/%s?q=id:%s&rows=100&fl=id&wt=csv&indent=true'%(handler,id)
    resp, content = h.request(testurl,"GET", "",headers=headers)
    results[handler]=content.split('\n')
    del results[handler][0]
    


for testtop in testtops:
    print 'Number matched %d:\t%d'%(testtop,len(set(results[handlers[0]][0:testtop])&set(results[handlers[1]][0:testtop])))

