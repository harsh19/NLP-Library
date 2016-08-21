'''
This code generates the delta tfidf values for a list of labelled positive and negative class of documents
'''

import config_delta_tfidf as config

from nltk.corpus import stopwords
from collections import Counter
import string
import math
from nltk.stem import WordNetLemmatizer
from nltk.stem.porter import PorterStemmer
import pickle

############################################
english_stopwords = []
excluded_characters = set(string.punctuation)
all_tokens = []
if config.do_lemmatize:
	wordnet_lemmatizer =  WordNetLemmatizer()
if config.do_stemming:
	stemmer = PorterStemmer() 

############################################

def setParams(list_of_additional_stopwords = ['#text#', '#title#', '#header#'], do_remove_punctuation = True, do_remove_stopwords = True, do_lemmatize = True, do_stemming = False,include_bigrams = False,include_trigrams = False,cnt_threshhold = 2):
	config.list_of_additional_stopwords = list_of_additional_stopwords
	config.do_remove_punctuation = do_remove_punctuation
	config.do_remove_stopwords = do_remove_stopwords
	config.do_lemmatize = do_lemmatize
	config.do_stemming = do_stemming
	config.include_bigrams = include_bigrams
	config.include_trigrams = include_trigrams
	config.cnt_threshhold = cnt_threshhold

############################################
# Utility functions

def loadInit():
	global english_stopwords
	english_stopwords = stopwords.words('english')
	lst = config.list_of_additional_stopwords
	english_stopwords.extend(lst)
	
def getLemmatized(word):
	return wordnet_lemmatizer.lemmatize(word)
	
def getStemmed(word):
	return stemmer.stem(word)

def ascii_only(s):
	ret = ""
	for ch in s:
		if ord(ch)<=128:
			ret =  ret + ch
	return ret

def removePuntuation(s):
	return ''.join([ch for ch in s if ch not in excluded_characters])

def getBigrams(words_list):
	m = len(words_list)
	i=0
	bigrams = []
	while i<m-1:
		bigrams.append(words_list[i] + " " + words_list[i+1])
		i = i+1
	return bigrams
	
def getTrigrams(words_list):
	m = len(words_list)
	i = 0
	trigrams = []
	while i<m-2:
		trigrams.append(words_list[i] + " " + words_list[i+1] + " " + words_list[i+2])	
		i = i+1
	return trigrams

############################################
def getTFIDFDoc(doc, idf_scores_dict):
	tf = {}
	for word in doc:
		if word not in idf_scores_dict:
			continue
		if word in tf:
			tf[word] += 1
		else:
			tf[word] = 1
	lm = {token: (1.0 * tf[token] * idf_scores_dict[token]) for token in tf}
	return lm

def getTFIDF(list_of_docs, idf_scores_dict):
	list_of_docs = map(preprocessDoc,list_of_docs)
	return [getTFIDFDoc(doc,idf_scores_dict) for doc in list_of_docs]


############################################
def getIDFScores(list_of_docs):
	idf_scores = {}
	for doc in list_of_docs:
		set_of_tokens = set(sorted(doc))
		for token in set_of_tokens:
			if token in idf_scores:
				idf_scores[token] = idf_scores[token] + 1
			else:
				idf_scores[token] = 1
	#print idf_scores
	total_num_docs = len(list_of_docs)
	idf_scores = { word: math.log( (total_num_docs * 1.0) / (1.0 * idf_scores[word]) ) for word in idf_scores if idf_scores[word] >= config.cnt_threshhold } # division would be fine since occurence of a token means it is present at least once
	return idf_scores

def getDeltaIDFScores(list_of_pos_docs, list_of_neg_docs):
	pos_idf_scores = getIDFScores(list_of_pos_docs)
	neg_idf_scores = getIDFScores(list_of_neg_docs)
	idf_scores = {}
	#all_items = pos_idf_scores.keys()
	#all_items.extend( pos_idf_scores.keys() )
	for item in pos_idf_scores.keys():
		idf_scores[item] = pos_idf_scores[item]
	for item in neg_idf_scores.keys():
		if item in idf_scores:
			idf_scores[item] -= neg_idf_scores[item]
		else:
			idf_scores[item] = -neg_idf_scores[item]
	return idf_scores
	
	

############################################
def preprocessDoc(doc):
	tokens = []
	words = doc.split()
	words = [word.lower() for word in words]
	if config.do_remove_punctuation: # Remove punctuation symbols
		words = [removePuntuation(word) for word in words]
	if config.do_remove_stopwords: # Stopword removal
		words = [word for word in words if word not in english_stopwords]
	if config.do_lemmatize: # Lemmatization
		words = [getLemmatized(word) for word in words]
	if config.do_stemming: # Stemming
		words = [getStemmed(word) for word in words]
	words = [word for word in words if word!=None and len(word)>0]
	tokens.extend(words)
	if config.include_bigrams:
		tokens.extend( getBigrams(words) )
	if config.include_trigrams:
		tokens.extend( getTrigrams(words) )
	tokens = [token for token in tokens if token!=None and len(token)>0]
	return tokens

############################################
idf_scores_dict = {}
def learnIDFScores(list_of_pos_docs, list_of_neg_docs):
	global idf_scores_dict
	loadInit()
	list_of_pos_docs = map(preprocessDoc,list_of_pos_docs)
	list_of_neg_docs = map(preprocessDoc,list_of_neg_docs)
	idf_scores_dict = getDeltaIDFScores(list_of_pos_docs, list_of_neg_docs)

def getTFIDFListOfDocs(list_of_docs):
	list_of_tfidf_dict = getTFIDF(list_of_docs, idf_scores_dict) # this is a list of dictionaries, each dictionary is of the form -> word:tfidf_score
	return list_of_tfidf_dict

###########################################################
def dumpIDFDict(file_dst):
	pickle.dump(idf_scores_dict, open(file_dst + "idf_scores_dict.obj",'wb'))

def loadIDFDict(file_src):
	global idf_scores_dict
	idf_scores_dict = pickle.load( open(file_src + "idf_scores_dict.obj", "rb") )

def getTopKByAbsoluteValues(K):
	lst = [ [item, abs(idf_scores_dict[item]) ] for item in idf_scores_dict ]
	lst = sorted(lst, key = lambda x : -x[1])
	lim = len(lst)
	return lst[0:min(lim,K)]

###########################################################
###########################################################
###########################################################
def driver():
	list_of_pos = [ "This is awesome good.", "Why is it good and wonderful?", "Are these good and awesome books written by you?"]
	list_of_neg = [ "Are these aweful books written by you?", "It is not awesome", "seriously it is aweful","This is very bad"]
	learnIDFScores(list_of_pos,list_of_neg)
	
	## Print idf scores
	fw = open("idf_scores.tsv","w")
	for item,score in idf_scores_dict.items():
		fw.write(item + "\t" + str(score) + "\n")
	fw.close()
	
	dumpIDFDict("")#current location
	
	print getTopKByAbsoluteValues(5)
	
	## Check with test list of docs
	list_of_doc = [ "Isn't this awesome?", "Surely it is aweful" ]
	#print getTFIDFListOfDocs(list_of_doc)
driver()


