pot-gtranslate-helper
=====================

Is a kick starter google translatable po generator in 2 steps.


Usage:

[help]	Prints this help.

to-google POT_FILE [OUTPUT_SUFIX]

	Creates a TXT output file to be translated by Google Translate.
	(This will take a POT file and output a text file to be loaded 
	to Google Translate. Once translated, copy&paste the translated
	output, ignore the first line saying that it was translated by 
	Google Translate, and put it on a file. This file will be an 
	input to phase 2.)

from-google POT_FILE GOOLE_TRANSLATED_FILE [OUTPUT_SUFIX]

	Uses the Google Translated file and generate a PO file.
	(The inpit will be the original POT file and the file with the 
	translation from Google Translate, and will output the PO file.
	All entries will be marked as fuzzy.)
