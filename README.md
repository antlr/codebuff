# CodeBuff smart formatter

## Abstract

Code formatting is not particularly exciting but many researchers would consider it either unsolved or not well-solved.  The two well-established solutions are:

1.  Build a custom program that formats code for specific a language with ad hoc techniques, typically subject to parameters such as "*always put a space between operators*".
2.  Define a set of formal rules that map input patterns to layout instructions such as "*line these expressions up vertically*".

Either techniques are painful and finicky.  

This repository is a step towards what we hope will be a universal code formatter that looks for patterns in a corpus and attempts to format code using those patterns.  

##  Mechanism

For a given language *L*, the input to CodeBuff is:

1. a grammar for *L*
2. a set of input files written in *L*
3. a file written in *L* but not in the corpus that you would like to format

