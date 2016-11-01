
/**
 *
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package eu.edisonproject.term.extraction;

import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michele Sparamonti
 */
public final class Apriori {

  /**
   * the words
   */
  private List<String> words;
  /**
   * the list of current itemsets
   */
  private List<String[]> itemsets;
  /**
   * the list of all frequent itemsets
   */
  private List<String> listOfItemsets;
  /**
   * the name of the transcation file
   */
  private String text;
  /**
   * number of different items in the dataset
   */
  private int numItems;
  /**
   * total number of transactions in transaFile
   */
  private int numTransactions;
  /**
   * minimum support for a frequent itemset in percentage, e.g. 0.8
   */
  private double minimumSupport;
  private int lenOfRule;

  /**
   * generates the apriori itemsets from a text
   *
   * @param args configuration parameters: args[0] is a set of lines, args[1]
   * the min support (e.g. 0.8 for 80%)
   * @throws java.lang.Exception
   */
  public Apriori(String[] args) throws Exception {
    setText(args[0]);
    setMinimumSupport(Double.valueOf(args[1]));
    setMaxRuleLen(Integer.valueOf(args[2]));
    configure();
  }

  public Apriori() throws Exception {
    configure();
  }

  /**
   * starts the algorithm after configuration
   *
   * @return
   * @throws java.lang.Exception
   */
  public List<String> go() throws Exception {
    String[] lines = getText().split("\n");

    words = new LinkedList<>();
    for (String l : lines) {
      String[] lineWords = l.split(" ");
      for (String s : lineWords) {
        if (!words.contains(s)) {
          words.add(s);
        }
      }
    }
    numItems = words.size();
    for (int i = 1; i < lines.length; i++) {
      numTransactions++;
    }

    //start timer
//        long start = System.currentTimeMillis();
    // first we generate the candidates of size 1
    createItemsetsOfSize1();
    int itemsetNumber = 1; //the current itemset being looked at
//        int nbFrequentSets = 0;

    // the variable lenOfRule represent the max length of the association rules. 
    // e.g 3 stay for max 3 words for each rules
    //int lenOfRule = 3;
    //for(int i=0;i<numberOfRound;i++){
    int i = 0;
    while (itemsets.size() > 0 && i < getLenOfRule()) {
      calculateFrequentItemsets();

      if (!itemsets.isEmpty() && i + 1 < getLenOfRule()) {
//                nbFrequentSets += itemsets.size();

        Logger.getLogger(Apriori.class.getName()).log(Level.INFO, "Found {0} frequent itemsets of size {1} (with support {2}%)", new Object[]{itemsets.size(), itemsetNumber, getMinimumSupport() * 100});
        createNewItemsetsFromPreviousOnes();
      }
      i++;
      itemsetNumber++;
    }

    return listOfItemsets;
  }

  /**
   * computes numItems, numTransactions, and sets minimumSupport
   */
  private void configure() throws Exception {

    listOfItemsets = new LinkedList<>();
    // setting minsupport
    // check if minsupport is a valid number in the range [0,1]
    if (getMinimumSupport() > 1 || getMinimumSupport() < 0) {
      throw new Exception("minSup: bad value");
    }

    // going though the file to compute numItems and  numTransactions
    numItems = 0;
    numTransactions = 0;

  }

  /**
   * puts in itemsets all sets of size 1, i.e. all possibles items of the
   * datasets
   */
  private void createItemsetsOfSize1() {
    itemsets = new ArrayList<>();
    for (int i = 0; i < numItems; i++) {
      String[] cand = {words.get(i)};
      itemsets.add(cand);
    }
  }

  /**
   * if m is the size of the current itemsets, generate all possible itemsets of
   * size n+1 from pairs of current itemsets replaces the itemsets of itemsets
   * by the new ones
   */
  private void createNewItemsetsFromPreviousOnes() {
    // by construction, all existing itemsets have the same size
    int currentSizeOfItemsets = itemsets.get(0).length;
    Logger.getLogger(Apriori.class.getName()).log(Level.INFO, "Creating itemsets of size {0} based on {1} itemsets of size {2}", new Object[]{currentSizeOfItemsets + 1, itemsets.size(), currentSizeOfItemsets});

    HashMap<String, String[]> tempCandidates = new HashMap<>(); //temporary candidates

    // compare each pair of itemsets of size n-1
    for (int i = 0; i < itemsets.size(); i++) {
      for (int j = i + 1; j < itemsets.size(); j++) {
        String[] X = itemsets.get(i);
        String[] Y = itemsets.get(j);

        assert (X.length == Y.length);

        //make a string of the first n-2 tokens of the strings
        String[] newCand = new String[currentSizeOfItemsets + 1];
        for (int s = 0; s < newCand.length - 1; s++) {
          newCand[s] = X[s];
        }

        int ndifferent = 0;
        // then we find the missing value
        for (int s1 = 0; s1 < Y.length; s1++) {
          boolean found = false;
          // is Y[s1] in X?
          for (int s2 = 0; s2 < X.length; s2++) {
            if (X[s2].equals(Y[s1])) {
              found = true;
              break;
            }
          }
          if (!found) { // Y[s1] is not in X
            ndifferent++;
            // we put the missing value at the end of newCand
            newCand[newCand.length - 1] = Y[s1];
          }

        }

        // we have to find at least 1 different, otherwise it means that we have two times the same set in the existing candidates
        assert (ndifferent > 0);

        /*if (ndifferent==1) {
                	Arrays.sort(newCand);*/
        tempCandidates.put(Arrays.toString(newCand), newCand);
        //}
      }
    }

    //set the new itemsets
    itemsets = new ArrayList<>(tempCandidates.values());
    Logger.getLogger(Apriori.class.getName()).log(Level.INFO, "Created {0} unique itemsets of size {1}", new Object[]{itemsets.size(), currentSizeOfItemsets + 1});

  }

  /**
   * put "true" in trans[i] if the integer i is in line
   */
  private void line2booleanArray(String line, boolean[] trans) {
    Arrays.fill(trans, false);
    StringTokenizer stFile = new StringTokenizer(line, " "); //read a line from the file to the tokenizer
    //put the contents of that line into the transaction array
    while (stFile.hasMoreTokens()) {

      String parsedVal = stFile.nextToken();
//            Logger.getLogger(Apriori.class.getName()).log(Level.INFO, parsedVal);
      trans[words.indexOf(parsedVal)] = true; //if it is not a 0, assign the value to true
    }
  }

  /**
   * passes through the data to measure the frequency of sets in
   * {@link itemsets}, then filters thoses who are under the minimum support
   * (minimumSupport)
   */
  private void calculateFrequentItemsets() throws Exception {

    Logger.getLogger(Apriori.class.getName()).log(Level.INFO, "Passing through the data to compute the frequency of {0} itemsets of size {1}", new Object[]{itemsets.size(), itemsets.get(0).length});

    List<String[]> frequentCandidates = new ArrayList<>(); //the frequent candidates for the current itemset

    boolean match; //whether the transaction has all the items in an itemset
    int count[] = new int[itemsets.size()]; //the number of successful matches, initialized by zeros

    // load the transaction file
    String[] lines = getText().split("\n");

    boolean[] trans = new boolean[numItems];

    // for each transaction
    for (int i = 0; i < numTransactions; i++) {

      // boolean[] trans = extractEncoding1(data_in.readLine());
      String line = lines[i];
      line2booleanArray(line, trans);

      // check each candidate
      for (int c = 0; c < itemsets.size(); c++) {
        match = true; // reset match to false
        // tokenize the candidate so that we know what items need to be
        // present for a match
        String[] cand = itemsets.get(c);
        //int[] cand = candidatesOptimized[c];
        // check each item in the itemset to see if it is present in the
        // transaction
        for (String xx : cand) {
          if (trans[words.indexOf(xx)] == false) {
            match = false;
            break;
          }
        }
        if (match) { // if at this point it is a match, increase the count
          count[c]++;
          //Logger.getLogger(Apriori.class.getName()).log(Level.INFO, Arrays.toString(cand)+" is contained in trans "+i+" ("+line+")");
        }
      }

    }
    for (int i = 0; i < itemsets.size(); i++) {
      // if the count% is larger than the minimumSupport%, add to the candidate to
      // the frequent candidates
      if ((count[i] / (double) (numTransactions)) >= getMinimumSupport()) {
        foundFrequentItemSet(itemsets.get(i), count[i]);
        //System.out.println(Arrays.toString(itemsets.get(i)) + "  ("+ ((count[i] / (double) numTransactions))+" "+count[i]+")");
        frequentCandidates.add(itemsets.get(i));
      }
      //else Logger.getLogger(Apriori.class.getName()).log(Level.INFO, "-- Remove candidate: "+ Arrays.toString(candidates.get(i)) + "  is: "+ ((count[i] / (double) numTransactions)));
    }

    //new candidates are only the frequent candidates
    itemsets = frequentCandidates;
  }

  private void foundFrequentItemSet(String[] itemset, int support) {
    String line = "";
    for (int i = 0; i < itemset.length; i++) {
      line += itemset[i];
      if (i + 1 != itemset.length) {
        line += " ";
      }
    }
    line += "/" + (support / (double) numTransactions);

    listOfItemsets.add(line);
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * @return the minimumSupport
   */
  public double getMinimumSupport() {
    return minimumSupport;
  }

  /**
   * @param minSup the minimumSupport to set
   */
  public void setMinimumSupport(double minSup) {
    this.minimumSupport = minSup;
  }

  /**
   * @return the lenOfRule
   */
  public int getLenOfRule() {
    return lenOfRule;
  }

  /**
   * @param lenOfRule the lenOfRule to set
   */
  public void setMaxRuleLen(int lenOfRule) {
    this.lenOfRule = lenOfRule;
  }
}
