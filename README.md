Solr Sense
=========
This document explains how the Solr-Sense plugin can be configured to provide a set of Solr request handlers which add out-of-the-box **conceptual search** capabilities to a standard Solr installation. These capabilities can be understood via two use cases, one for each request handler:

**SenseLikeThis (SLT)**: The user has a document indexed in their Solr instance and wishes to find documents which are conceptually similar.

**Sense**: The user has a block of text for which they’d like to find documents which are conceptually similar.


##Keyword Paradigm
The paradigm made popular by Google, and provided by default in a Solr installation is the retrieval of documents by keyword. If the user has read a document and wishes to find similar documents, it is their responsibility to reduce the document to keywords and iteratively search through the combinations, and their synonyms, until they find the answer they require. This is an excellent approach when the user is only looking for an answer via a single document, and knows with high certainty when they’ve read the answer. Examples are: “Who is the president?”, “What day of the week is Christmas 2012?”, “Example of add document in Solr”.

##Concept Searching
The idea of conceptual searching is in fact similar to the way people naturally search for information. Conceptual searching is the heart of the definition of research: “The systematic investigation into and study of sources in order to establish facts and reach new conclusions.” When we want to look at a set of documents which are related in overall idea, not simply on a keyword level, and gather their combined knowledge, conceptual search is the answer.

For example, when searching via keywords, if the query is “teenager”, it will not produce results containing the word adolescent, which is equally as valuable. Keyword search is excellent when searching for “an answer”, but the conceptual searching paradigm is the next step for information workers searching for “the answers”.  We like to think of it as having your own personal librarian.

This idea is explained very clearly by the Latent Semantic Indexing Wikipedia page:

> LSI overcomes two of the most problematic constraints of Boolean keyword queries: multiple words that have similar meanings (synonymy) and words that have more than one meaning (polysemy). Synonymy is often the cause of mismatches in the vocabulary used by the authors of documents and the users of information retrieval systems. As a result, Boolean or keyword queries often return irrelevant results and miss information that is relevant.

Typical use cases which easily differentiate the two paradigms are:

* **Legal Studies**: The user has a case summary for which they’d like to find all related legal precedents. The number of ways to keyword search for all related information is daunting; with a conceptual search a simple copy and paste does all the work.

* **Patent Studies**: Trying to find patents which your idea infringes on can be a challenging task. Conceptual search allows a user to copy and paste their abstract and find related patents within seconds.

* **Research References**: Looking to publish but are unsure if the important related works are cited? Provide the abstract and conceptually related documents are listed for quick review.

This Searchbox-Sense plugin provides this level of functionality naturally and without any messy installs.

##Technological Overview
Conceptual search is a branch of semantic search from the school of Latent Semantic Analysis (LSA).


* **Formally**: In its most elementary form, LSA creates a matrix model, referred to here as a Cognitive Knowledge Base (CKB), via a truncated Singular Value Decomposition (SVD) of a document term frequency matrix [1]. This model creates a semantic space within which projected documents having similar concepts are closer to each other, allowing for accurate retrieval and ranking of related documents.

* **Intuitively**: A model is created which has learned the meaning and relationship between words by examining all of the documents in the repository. By supplying this domain knowledge, we are able to identify terms/concepts which are similar in their underlying context automatically. This is no different than reading many books on a particular subject, forming the relationships of words and ideas in one’s mind, and then through that lens identifying related documents to a query.

While this approach has been well known and studied in academia for many years, showing superior results to keyword retrieval [2] [3], there were numerous hurdles preventing it from entering a production environment:

* **Computation of CKB**: The creation of a good model for small datasets has been well studied  [4][5][6], unfortunately, as the datasets have grown in size (consider PubMed with 11M documents), the computational cost rises exponentially as the number of documents and additional terms grows with the potential for reaching into weeks with even a powerful 12 core machine.

* **Memory Requirements**: As greater specificity is required for larger homogenous datasets to identify the optimally related documents, the size of the model must also increase putting stress on even today’s high memory instances. At this level, trying to hold large datasets in memory is completely intractable.

* **Efficiency of search**: The larger the model, the longer the search time will take as each document needs to be projected. Additionally, since production size datasets are too large to hold into memory, retrieving documents from disk also creates significant latency in requests. For moderately large datasets of 100k, search times reached into the 1 minute range, completely unusable for any production environment.

* **Software Framework**: The development of an entire enterprise level software solution (including the CRUD of documents, data import handlers, and necessary features for enterprise usability such as highlighting, spell checking, etc.) was an insurmountable task for small startups, let alone integrating conceptual search algorithms.

Over the last 3 years, the Searchbox research team has developed proprietary cutting edge algorithms which solve the aforementioned problems. Furthermore, the technical team has developed this Solr plugin, which brings the previously unavailable power of semantic search immediately into an existing Solr installation with good results.

As one might have determined, the power of the conceptual search comes from the CKB. With this plug-in we provide a specific CKB for the included demonstration dataset, thus providing very high relevancy in its domain. This CKB can of course be used for evaluation on different datasets, but typically we see about a 15% degradation of result quality as a consequence of out-of-domain knowledge errors (e.g. don’t ask a doctor how to fix a car). Searchbox is determined to provide the best possible results, please see the contact section so that we can create a repository specific CKB to meet your exact retrieval needs.


##Sense Install
In this section we list the requirements and setup steps needed to run the Searchbox-Sense plugin. As a demo, we have included a pre-indexed subset of pubmed with its specific tailored CKB. Within a few seconds this can give the user an idea of what to expect. Afterwards, we provide step by step instructions on how to use the same CKB on a locally available dataset, leading to a more complete view of how conceptual search can improve information retrieval.

###Requirements
There are very few specific requirements needed to support Searchbox-Sense, in the case of running the demo the minimum requirements are:

| Key        | Val           | 
| ------------- |:-------------:|
| Solr/Lucene     |4.0 |
| Memory      | 4GB      |
| Hard disk space | < 200 MB     |


###Demo install
We have bundled a very easy to setup core for demonstration of the conceptual search technology. From a clean download of Solr, we extend the Getting Started section of the Solr tutorial. (http://lucene.apache.org/solr/4_0_0/tutorial.html)

```bash
$ unzip -q solr-nightly.zip
$ cd solr-nightly/example/
$ cp –pr <download_dir>/pubmed_demo ./solr/
```

To enable our demonstration core alongside the standard “collection1″ core, we add the following line italicized line in solr/solr.xml:
```xml
<core name="collection1" instanceDir="collection1" />
<core name="pubmed_demo" instanceDir="pubmed_demo" />
```

Since our CKB is specifically tailored to the pubmed dataset, we recommend you to download the free subset at the following URL: http://www.ncbi.nlm.nih.gov/pmc/tools/ftp/ (XML for data mining via FTP, 4 tar.gz files, ~8Gb of compressed data)

As before, starting the Solr server is done via:
```
$ java -jar start.jar
```

Now Solr should be up and running with the Searchbox-Sense plugin enabled. Check to make sure everything is functioning properly by checking the command line logs and heading over to the Solr console (http://localhost:8983/solr/).

To see an explanation of usage continue to the usage section.

Here we show some example queries you can run to test the SearchBox-Sense approach  versus Solr’s MLT (More Like This) logic.

###Production Environment

Of course, the Searchbox-Sense plugin is not limited to the demonstration core. While slightly more complicated, this section aims at setting up the Searchbox-Sense plugin for a pre-existing core. Without loss of generality, we continue with the Solr-tutorial at http://lucene.apache.org/Solr/4_0_0/tutorial.html and explain how to enable conceptual search for their collection1 core. By changing core and field names to match what is in production, these instructions are applicable to all existing cores.

Assuming the demonstration tutorial is in place, we can issue the following commands to copy the needed files into destination core, removing the need to keep the demonstration core available:
```bash
$ cd solr/
$ cp -pr pubmed_demo/CKB/ collection1/
$ cp -pr pubmed_demo/lib/ collection1/
```

Now with all of the files in place, we need to add the appropriate request handlers to solr/collection1/conf/solrconfig.xml.

First we add the following line to enable caching:
```
<cache name="com.searchbox.sltcache" class="solr.LRUCache" size="4096" initialSize="2048" autowarmCount="0"/>
```

on line 504, inside of the <query> block near the Custom cache section.

Additionally, right before this text:
```xml
<requestHandler name="/select" class="solr.SearchHandler">
<!-- default values for query parameters can be specified, these
will be overridden by parameters in the request
-->
<lst name="defaults">
    <str name="echoParams">explicit</str>
    <int name="rows">10</int>
    <str name="df">text</str>
</lst>
```


We add this text (as seen in our demo file pubmed_demo/conf/solrconfig.xml):
```xml
<queryParser name="sense" class="com.searchbox.solr.SenseQParserPlugin">
    <lst name="ckbs">
        <lst name="pubmed">
            <str name="name">pubmed Sparse EN</str>
            <str name="certaintyValue">16.1014593340993</str>
            <str name="maximumDistance">1.4059477152206</str>
            <str name="type">SPARSE</str>
            <str name="locale">en</str>
            <str name="baseDirectory">solr/collection1/CKB/</str>
            <str name="modelFile">pubmed.cache</str>
            <str name="idfFile">pubmed.idflog</str>
            <str name="dictionaryFile">pubmed.tdic</str>
        </lst>
    </lst>
</queryParser>
<requestHandler name="/sense" class="com.searchbox.solr.SenseQueryHandler">
    <lst name="defaults">
        <str name="echoParams">explicit</str>
        <int name="rows">10</int>
        <str name="slt.sf">article-abstract</str>
        <str name="fl">id</str>
        <str name="fl">score</str>
        <str name="defType">sense</str>
        <float name="sw">.8</float>
        <str name="ckb">pubmed</str>
    </lst>
</requestHandler>
<requestHandler name="/slt" class="com.searchbox.solr.SenseLikeThisHandler">
    <lst name="defaults">
        <str name="slt.sf">article-abstract</str>
        <str name="ckb">pubmed</str>
        <float name="sw">.8</float>
    </lst>
</requestHandler>
```

We can see that the two specific variables which need to be adjusted for a different core are baseDirectory and slt.sf which indicates the default field for conceptual searching.

It is important to note that the SearchBox-Sense plugin requires that termVectors be stored. To know which fields have this functionality, and/or to add a new field which supports it, consult the core’s schema.xml. For example, collection1 contains this description:

```xml
<field name="includes" type="text_general" indexed="true" stored="true" termVectors="true" termPositions="true" termOffsets="true" />
```

Which does indeed set the needed termVectors to true, making this a candidate for conceptual search. This constraint is very similar to the built in Solr function MoreLikeThis, which means with a few clicks it is possible to try out conceptual search in place of MoreLikeThis!

Additionally, the CKB expects terms of a certain format to obtain optimal results. The formatting of these terms is identified in the schema.xml by the analyzers. For example in the demo schema.xml:


```xml
<fieldType name="text_general" class="solr.TextField" positionIncrementGap="100">
    <analyzer>
        <tokenizer />
        <filter ignoreCase="true" words="stopwords_net.txt" />
        <filter preserveOriginal="1" generateWordParts="1" generateNumberParts="0" catenateWords="1" catenateNumbers="1" catenateAll="1" splitOnCaseChange="1" splitOnNumerics="0" stemEnglishPossessive="1" />
        <filter />
        <filter />
        <filter min="3" max="255" />
    </analyzer>
</fieldType>
```

We can see that the analyzer class contains a tokenizer and five filter classes. These take the input terms and convert them into a more robust representation, and more importantly into the style which the CKB expects. Adding these filters and re-indexing creates the correct formatting, but does not change the query results as these parameters solely define internal representation.


##Sense Usage Via Request Handlers
The two added request handlers provided by the plugin are used in a similar fashion as other Solr endpoints.

###SenseLikeThis (SLT)

The **/slt** endpoint provides access to the SenseLikeThis functionality. Simply put, this request handler takes a Solr query, and returns a set of results which are conceptually similar to the first document in the query. A typical example is to use **q=id:abcd1234** to find similar documents to the document with id **abcd1234** based on the default sf field defined in the configuration file. It is possible to use a different field simply by setting **slt.sf** in the query, as with other Solr parameters. This query supports all other /select parameters such as **fl, df, wt, start, rows, sort, etc.**


###Sense Search Component
The **/sense** endpoint provides access to conceptual search by query text. This is similar to /slt, except that instead of taking a Solr query as the parameter, it instead takes a body of text and uses that to find similar documents based on the default **slt.sf** field.

###Optional parameters
For small document sets of less than 10,000 documents, no configuration of additional parameters is needed. As discussed in the technology section, on smaller datasets there is no efficiency issue associated with searching through all documents as results should be returned in under a second.

For large datasets, Searchbox’s proprietary algorithms allow 1 second search times and high quality results by intelligently searching inside of a subset of the overall repository. This is done by analyzing the terms in the query and determining with high probability which terms must be present in the result document for it to have a high score. The secret sauce of our scale approach is to only search in that subset. The default parameters are usually sufficient as they are tuned for individual specific CKBs, but in some cases it may be interesting to sacrifice additional time for a higher certainty in the most optimal results. Below we present a table of the tune-able parameters, their default values and comments explaining their usage.

| Parameter        | Default Value           | Comments  |
| ------------- |:-------------:| :-----|
| **slt.sf**     | article-abstract | Determines which field the conceptual search occurs on, should be either changed in the configuration file or specified as a query parameter as discussed in the Production Installation section. |
| **ntu**     | 5 | Number of terms to consider. The search time increases slowly as the value increases, but results in greater likelihood of having optimal results. As explained above, this value selects 5 terms from the query which are the most important in the result and uses those to seed the subset of documents to search in. |
| **ntt**     | 5000 | Term threshold. This parameter should not be changed unless instructed to do so by the Searchbox team. |
| **nmd**     | 5000 | Maximum number of documents in subset to search in. This variable controls the absolute largest size possible for the subset of documents and as such directly controls how long the query will take. Doubling this number will typically result in a doubling of the query time, but with an increased amount of certainty in the optimality of the result set. |

###Logging
The logging for the Searchbox-Sense plugin follows the slf4j paradigm used by Solr, such that varying levels of log can be defined by setting the appropriate level (debug,info,warn,error).

###Performance
Searchbox-Sense only performs actions at runtime. As a result, there is no effect on the indexing performance. Additionally, since computations are done in real-time, the performance of other request handlers are not affected.

During the usage of the request handlers, there are two discrete phases. The first phase determines which subset of documents to search within by probing the underlying distribution of documents in relation to their terms. These are a combination of standard Solr BooleanQuery clauses and as such are well cached and efficiently executed. The latter stage is the conceptual search which requires both reading of the term vectors and mathematical computation.  During this stage, both the cpu and hard disk are active.

For very small datasets (about 10,000), the first stage will be very small. On larger datasets, the first stage grows slightly to bound the overall time of the conceptual search. For a corpus of about 100,000 documents, expected result times are to be less than one second. This time can be adjusted upward (for better precision) or downward (for faster speed) by adjusting the variables in Optional Parameters.


###References

1. Deerwester, S., Dumais, S., Furnas, G., Landauer, T., & Harshman, R. (1990). An introduction to latent semantic analysis. Journal of the American Society for Information Science, 391-407.
2. Husbands, P., Simon, H., & Ding, C. (2000). On the Use of Singular Value Decomposition for Text Retrieval. SIAM Computer Information Retrieval, 145-156.
3. Dumais, S. (2003) Data-driven approaches to information access, Cognitive Science, 491-524
4. Berry, M., Dumais, S., & O’Brien, G. (1999). Using Linear Algebra for Intelligent Information Retrieval. SIAM Rev., Vol 37, No.4, 335-362.
5. Kumar, C. A., & Srinivas, S. (2006). Latent Semantic Indexing Using Eigevalue Analysis for Efficient Information Retrieval . International Journal of Applied Mathematics and Computer Science, 551-558.
6. Hofmann, T. (1999). Probabilistic latent semantic indexing. Proceedings of the 22nd annual international ACM SIGIR conference on Research and development in information retrieval (SIGIR), 50-57.
