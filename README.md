# CodeBuff smart formatter

## Abstract

Code formatting is not particularly exciting but many researchers would consider it either unsolved or not well-solved.  The two well-established solutions are:

1.  Build a custom program that formats code for specific a language with ad hoc techniques, typically subject to parameters such as "*always put a space between operators*".
2.  Define a set of formal rules that map input patterns to layout instructions such as "*line these expressions up vertically*".

Either techniques are painful and finicky.  

This repository is a step towards what we hope will be a universal code formatter that uses machine learning to look for patterns in a corpus and to format code using those patterns.  

## Introduction

When looking at code, programmers can easily pick out formatting patterns for various constructs such as how `if` statements and array initializers are laid out.  Rule-based formatting systems allow us to specify these input to output patterns.  The key idea with our approach is to mimic what programmers do during the act of entering code or formatting.   No matter how complicated the formatting structure is for a particular input phrase, formatting always boils down to the following four canonical operations:

1. *nl*: Inject newline
2. *ws*: Inject whitespace
3. *align*: Align current token with some previous token
4. *indent*: Indent current token from some previous token

The first operation predicates the other three operations in that injecting a newline triggers an alignment or indentation. Not injecting a newline triggers injection of 0 or more spaces.

The basic formatting engine works as follows. At each token in an input sentence, decide which of the canonical operations to perform then emit the current token.  Repeat until all tokens have been emitted.

To make this approach work, we need a model that maps context information about the current token to one or more canonical operations in {*nl*, *ws*, *align*, *indent*}. To create a formatter for a given language *L*, `CodeBuff` takes as input:

1. A grammar for *L*
2. A set of input files written in *L*
3. A file written in *L* but not in the corpus that you would like to format

`CodeBuff` trains a *k-Nearest-Neighbor* (*kNN*) machine learning model based upon the corpus. The *kNN* model is particularly attractive because it is very powerful yet simple and mirrors how programmers format code. Programmers scan their memory for similar context situations and apply the same rule or the rule they do most often.

## Mechanism

### Features

1. INDEX_PREV_TYPE      
1. INDEX_PREV_EARLIEST_RIGHT_ANCESTOR
1. INDEX_CUR_TYPE
1. INDEX_MATCHING_TOKEN_DIFF_LINE
1. INDEX_FIRST_ON_LINE          
1. INDEX_EARLIEST_LEFT_ANCESTOR
1. INDEX_ANCESTORS_CHILD_INDEX
1. INDEX_ANCESTORS_PARENT_RULE
1. INDEX_ANCESTORS_PARENT_CHILD_INDEX
1. INDEX_ANCESTORS_PARENT2_RULE
1. INDEX_ANCESTORS_PARENT2_CHILD_INDEX
1. INDEX_ANCESTORS_PARENT3_RULE
1. INDEX_ANCESTORS_PARENT3_CHILD_INDEX
1. INDEX_ANCESTORS_PARENT4_RULE
1. INDEX_ANCESTORS_PARENT4_CHILD_INDEX
