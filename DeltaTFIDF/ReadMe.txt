@harsh jhamtani
16 October 2015
This code generates the delta-tfidf values for a list of documents
The IDF values are learned using a set of positive labelled documents and a set of negative labelled documents

----------------------Usage
First call 'learnIDFScores(list_of_pos,list_of_neg)'
Example:
	list_of_pos = [ "This is awesome.", "Why is it good and wonderful?", "Are these awesome books written by you?"]
	list_of_neg = [ "Are these aweful books written by you?", "It is not awesome", "seriously it is aweful","This is very bad"]
	learnIDFScores(list_of_pos,list_of_neg)

- list_of_docs is a list of strings, where each string represents a document
Example: list_of_docs = [ "This is awesome.", "It is aweful.", "Why is it awesome and wonderful?", "Are these awesome books written by you?"]

--
To save idf dictionary for future use, call 'dumpIDFDict(location)'
Example: dumpIDFDict("C:\\Documents\\")

To load idf dictionary which was saved earlier, call loadIDFDict(location)
--

-Parameters
Parameters are there in config_lm.py
	list_of_additionl_stopwords = ['#text#', '#title#', '#header#']
	do_remove_punctuation = True
	do_remove_stopwords = True
	do_lemmatize = True
	do_stemming = False
	include_bigrams = False
	include_trigrams = False
	cnt_threshhold = 2 # token should occur in at least these many documents

	To change parameter values, do one of the following:
1) Change the parameters directly in config_lm.py
2) Call 'setParams(list_of_additionl_stopwords = ['#text#', '#title#', '#header#'], do_remove_punctuation = True, do_remove_stopwords = True, do_lemmatize = True, do_stemming = False,include_bigrams = False,include_trigrams = False,cnt_threshhold = 2)' with appropriate values


-----------------------Description
For each document, perform following preprocessing steps:
	- tokenize : using split()
	- convert all tokens to lower case
	- remove punctuations : using nltk
	- remove stopwords : currently leveraging english stopwords from nltk corpora
	- lemmatize : currently using nltk lemmatizer
	- stem : currently using porter's stemming algorithm from nltk
	- generate bigrams
	- generate trigrams
	
################ TO DO : UPDATE FOLLOWING DESCRIPTION
Generate idf values
	- Count the number of documents in which a token occurs : for all tokens
	- idf (token) = math.log( m / num_of_docs[token]), m = total number of documents
	
For each document, generate set of tfidf values
	- For each token in document, tfidf(token) = tf(token) * idf(token)
	tf(token) = count(token, doc) / total_num_tokens(doc)