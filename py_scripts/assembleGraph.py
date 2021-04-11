#!/usr/bin/env python

## RUN: completeGraph <number of nodes> <rand limiter>

from numpy.random import randint
from numpy import empty, vstack, array
from time import time
import sys
import os

def main():
    basename = make_txt_file()
    print("text file made")
    run_java_macros(basename)


def run_java_macros(basename):
    os.system("sort -k1n -k2n "+basename+".txt | uniq > "+basename+"_sorted.txt")
    print("sorted")
    os.system("java -cp \"../lib/*\" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy "+basename+" < "+basename+"_sorted.txt")
    print("Now generate graph")
    os.system("java -cp \"../bin\":\"../lib/*\" GenerateLabeledGraphFromTxt "+basename+" "+basename+"_sorted.txt")
    print("Print graph")
    os.system("java -cp \"../bin\":\"../lib/*\" PrintLabeledGraph "+basename)
    

def make_txt_file():
    nodes = int(sys.argv[1])
    lim = int(sys.argv[2])
    basename ="rand_graph_"+str(nodes)
    end = randint(nodes, 3*nodes+1)
    count = 0
    with open(basename+".txt", "w+") as f:
        '''
        while count < end:
            u = randint(nodes)
            v = randint(nodes)
            if u==v: continue
            f.write("%d\t%d\t%d\n"%( u, v, randint(1, 1000)))
            count+=1
        '''
        f.writelines("%d\t%d\t%d\n"%( u, v, randint(1, 1000)) for u in range(nodes) for v in range(nodes) if(u != v and randint(nodes) < lim) )
        '''
        for u in range(nodes):
            for v in range(nodes):
                if (u==v or randint(10)!=1): continue
                f.write("%d\t%d\t%d\n"%( u, v, randint(1, 1000) ) )
        '''
    return basename
        

if __name__=="__main__":
    t1 = time()
    main()
    print(time()-t1)
