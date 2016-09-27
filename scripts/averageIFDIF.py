#!/usr/bin/env python
import sys
import csv



with open( str(sys.argv[1]), 'rb') as csvfile:
  terms = csv.reader(csvfile, delimiter=',');
  ss = {};
  count ={};
  avg={};
  for row in terms:
    if row[0] in ss:
      c = count[row[0]];
      c+=1.0;
      s = float(ss[row[0]]) + float(row[1]);
      ss[row[0]] = s;
      avg[row[0]] = float(s) / float(c);
      count[row[0]] = c;
    else:
      s = float(row[1]);
      ss[row[0]] = s;
      c = 1;
      count[row[0]] = c;
      avg[row[0]] = float(s) / float(c);
      
      
for name, values in avg.iteritems():
    print name+","+str(values);
  
   

