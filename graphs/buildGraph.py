#!/usr/bin/env python
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
    os.system("java -cp \"../bin\":\"../lib/*\" GenerateLabeledGraphFromTxt "+basename+" "+basename+"_sorted.txt")
    os.system("java -cp \"../bin\":\"../lib/*\" PrintLabeledGraph "+basename)
    

def make_txt_file():
    nodes = int(sys.argv[1])
    edges = int(sys.argv[2])
    basename ="graph_"+str(nodes)+"_"+str(edges) 
    f = open(basename+".txt", "w+")
    e_count = 0
    for u in range(nodes):
        v = randint(nodes)
        vstack((added, array([u, v])))
        weight = randint(1, 1000) # range [1, 1000)
        string = "%d\t%d\t%d\n"%(u,v,weight)
        f.write(string)
        e_count+=1
        #add_edge(f, u, e_count, nodes, added)
    while e_count<edges:
        u = randint(nodes)
        v = randint(nodes)
        vstack((added, array([u, v])))
        weight = randint(1, 1000) # range [1, 1000)
        string = "%d\t%d\t%d\n"%(u,v,weight)
        f.write(string)
        e_count+=1
        #add_edge(f, u, e_count, nodes, added)
    f.close()
    return basename


if __name__=="__main__":
    t1 = time()
    main()
    print(time()-t1)




'''     
        for e in range(edges):
        u = randint(nodes)
        v = randint(nodes)
        ## no self loops or repeat edges
        while (u == v or (u, v) in added):
            u = randint(nodes)
            v = randint(nodes)
        vstack((added, array([u, v])))
        #added.append((u, v))
        #print(u, v)
        weight = randint(1, 1000) # range [1, 1000)
        string = "%d\t%d\t%d\n"%(u,v,weight)
'''
