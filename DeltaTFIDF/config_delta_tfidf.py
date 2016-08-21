
#------------------------------------------------------------------------------------------------------------------------------------------
list_of_additional_stopwords = ['#text#', '#title#', '#header#']
do_remove_punctuation = True
do_remove_stopwords = True
do_lemmatize = True
do_stemming = False
include_bigrams = False
include_trigrams = False
cnt_threshhold = 1 # token should occur in at least these many documents
#------------------------------------------------------------------------------------------------------------------------------------------

## To set parameters programatically :: def setParams(list_of_additional_stopwords = ['#text#', '#title#', '#header#'], do_remove_punctuation = True, do_remove_stopwords = True, do_lemmatize = True, do_stemming = False,include_bigrams = False,include_trigrams = False,cnt_threshhold = 2)