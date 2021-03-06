/*
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003-2005 Nick Lothian. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        developers of Classifier4J (http://classifier4j.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name "Classifier4J" must not be used to endorse or promote 
 *    products derived from this software without prior written 
 *    permission. For written permission, please contact   
 *    http://sourceforge.net/users/nicklothian/.
 *
 * 5. Products derived from this software may not be called 
 *    "Classifier4J", nor may "Classifier4J" appear in their names 
 *    without prior written permission. For written permission, please 
 *    contact http://sourceforge.net/users/nicklothian/.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.classifier4J.Utilities;
import net.sf.classifier4J.summariser.ISummariser;

public class Summariser {

    private Integer findMaxValue(List input) {
        Collections.sort(input);
        return (Integer) input.get(0);
    }

    /**
     * @see net.sf.classifier4J.summariser.ISummariser#summarise(java.lang.String)
     */
    public String summarise(String input, String title, int numSentences) {
        // get the frequency of each word in the input
        Map wordFrequencies = Utilities.getWordFrequency(input);
        Map titleFrequencies = Utilities.getWordFrequency(title);

        // now create a set of the 10 most frequent words
        Set mostFrequentWords = new HashSet(), topTitleTerms = new HashSet();
        if(wordFrequencies.size() > 0)
        	mostFrequentWords = Utilities.getMostFrequentWords(10, wordFrequencies);
        if(titleFrequencies.size() > 0)
        	topTitleTerms = Utilities.getMostFrequentWords(10, titleFrequencies);

        // break the input up into sentences
        // workingSentences is used for the analysis, but
        // actualSentences is used in the results so that the 
        // capitalisation will be correct.
        String[] workingSentences = Utilities.getSentences(input.toLowerCase());
        String[] actualSentences = Utilities.getSentences(input);

        // iterate over the most frequent words, and add the first sentence 
        // that includes each word to the result
//        Set outputSentences = new LinkedHashSet();
//        Iterator it = mostFrequentWords.iterator();
//        while (it.hasNext()) {
//            String word = (String) it.next();
//            for (int i = 0; i < workingSentences.length; i++) {
//                if (workingSentences[i].indexOf(word) >= 0) {
//                    outputSentences.add(actualSentences[i]);
//                    break;
//                }
//                if (outputSentences.size() >= numSentences) {
//                    break;
//                }
//            }
//            if (outputSentences.size() >= numSentences) {
//                break;
//            }
//
//        }
        
        //Luhn's keyword cluster method
        double [] scores1 = new double[actualSentences.length];
        for (int i = 0; i < workingSentences.length; i++) {
          Iterator it = mostFrequentWords.iterator();
          while (it.hasNext()) {
        	  scores1[i]+=getNumOccurences((String)it.next(), workingSentences[i]);
          }
          scores1[i] = Math.pow(scores1[i], 2)/workingSentences[i].length();
        }
        
        //Title term frequency method
        double [] scores2 = new double[actualSentences.length];
        for (int i = 0; i < workingSentences.length; i++) {
          Iterator it = topTitleTerms.iterator();
          while (it.hasNext()) {
        	  scores2[i]+=getNumOccurences((String)it.next(), workingSentences[i]);
          }
          scores2[i] = Math.pow(scores2[i], 2)/workingSentences[i].length();
        }
        
        //Location score - score first and last 5%
        int importantPosSentences = workingSentences.length/20;
        double locationScore = 1/workingSentences.length;
        double [] scores3 = new double[actualSentences.length];
        for(int i = 0; i < importantPosSentences; i++) {
        	scores3[i] = locationScore;
        }
        for(int i = workingSentences.length-importantPosSentences; i < workingSentences.length; i++) {
        	scores3[i] = locationScore;
        }
        
        double [] finalScores = new double[actualSentences.length];
        for(int i = 0; i < finalScores.length; i++) {
        	finalScores[i] = scores1[i] + scores2[i] + scores3[i];
        }
        
        HashMap<Double,String> map = new HashMap<Double, String>();
        ValueComparator bvc =  new ValueComparator(map);
        TreeMap<Double,String> sorted_map = new TreeMap<Double, String>(bvc);
        for(int i = 0; i < finalScores.length; i++) {
        	sorted_map.put(finalScores[i], actualSentences[i]);
        	map.put(finalScores[i], actualSentences[i]);
        }
        
        String result = "";
        int iterations = 0;
        for (double key : sorted_map.keySet()) {
        	result = result + map.get((Double)key) + "\n";
        	iterations++;
            if(iterations == numSentences) break;
        }
        
        result = result.substring(0, result.length()-1);
        
        return result;

//        List reorderedOutputSentences = reorderSentences(outputSentences, input);
//
//        StringBuffer result = new StringBuffer("");
//        it = reorderedOutputSentences.iterator();
//        while (it.hasNext()) {
//            String sentence = (String) it.next();
//            result.append(sentence);
//            result.append("."); // This isn't always correct - perhaps it should be whatever symbol the sentence finished with
//            if (it.hasNext()) {
//                result.append(" ");
//            }
//        }
//
//        return result.toString();
    }

    private double getNumOccurences(String str, String longerStr) {
	  int len = str.length();
	  int result = 0;
	  if (len > 0) {  
		  int start = longerStr.indexOf(str);
		  while (start != -1) {
			  result++;
			  start = longerStr.indexOf(str, start+len);
	  	  }
	  }
	  return result;
	}

	/**
     * @param outputSentences
     * @param input
     * @return
     */
    private List reorderSentences(Set outputSentences, final String input) {
        // reorder the sentences to the order they were in the 
        // original text
        ArrayList result = new ArrayList(outputSentences);

        Collections.sort(result, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                String sentence1 = (String) arg0;
                String sentence2 = (String) arg1;

                int indexOfSentence1 = input.indexOf(sentence1.trim());
                int indexOfSentence2 = input.indexOf(sentence2.trim());
                int result = indexOfSentence1 - indexOfSentence2;

                return result;
            }

        });
        return result;
    }
    
    private class ValueComparator implements Comparator {
    	Map base;
    	public ValueComparator(Map base) {
    		this.base = base;
    	}

    	public int compare(Object a, Object b) {
    		if((Double)a < (Double)b) {
    			return 1;
    		} else if((Double)a == (Double)b) {
    			return 0;
    		} else {
    			return -1;
    		}
    	}
    }

}
