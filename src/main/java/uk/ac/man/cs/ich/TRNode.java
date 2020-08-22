package uk.ac.man.cs.ich;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.*; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;
import java.util.stream.*;

/**
 * Created by chris on 16/07/19.
 */ 

//NB: this is a lightweight representation of 
//a 'Node' as implemented by the OWL API.
//this is introduced to gain access
//to a representation of 'the' class hierarchy
//that does not require a reasoner
public class TRNode {

    private OWLClass representative;
    private Set<OWLClass> equivalenceClasses;
    private Set<TRNode> parents;//superclasses
    private Set<TRNode> children;//subclasses

    public TRNode(Set<OWLClass> classes){
        this.equivalenceClasses = new HashSet<>();
        this.equivalenceClasses.addAll(classes);
        this.parents = new HashSet<>();
        this.children = new HashSet<>(); 
        for(OWLClass c : classes){
            this.representative = c;//choose a representative
            break;
        }
    }

    public TRNode(OWLClass c){
        this.equivalenceClasses = new HashSet<>();
        this.equivalenceClasses.add(c); 
        this.parents = new HashSet<>();
        this.children = new HashSet<>(); 
        this.representative = c;
    }

    public OWLClass getRepresentative(){
        return this.representative;
    }

    public boolean isRoot(){
        return this.parents.isEmpty();
    }

    public boolean isLeaf(){
        return this.children.isEmpty();
    }

    public boolean isSingleton(){
        return this.equivalenceClasses.size() == 1;
    }

    public Set<TRNode> getChildren(){
        return this.children; 
    }

    public Set<TRNode> getTransitiveChildren(){
        //breath first search for transitive children
        Set<TRNode> transitive = new HashSet<>();
        Set<TRNode> queue = new HashSet<>(this.children);
        while(!queue.isEmpty()){
            transitive.addAll(queue);
            Set<TRNode> next = new HashSet<>();
            for(TRNode n : queue){
                next.addAll(n.getChildren());
            }
            queue.clear();
            queue.addAll(next);
        }
        return transitive; 
    }

    public Set<OWLClass> getClasses(){
        return this.equivalenceClasses;
    }

    public Set<OWLClass> getEquivalentClasses(){
        return this.equivalenceClasses; 
    }

    public void addChild(TRNode c){
        this.children.add(c); 
    }

    public Set<TRNode> getParents(){
        return this.parents; 
    }

    public void addParent(TRNode p){ 
        this.parents.add(p); 
    }

    public void setParentForChildren(){
        for(TRNode g : this.children){
            g.addParent(this); 
            g.setParentForChildren();
        }
    }

    public boolean equals(TRNode n){
        if(this == null && n == null)
            return true; 
        if(this != null && n == null)
            return false;
        if(this == null && n != null)
            return false;
        if(this == n)
            return true;
        return this.getRepresentative().equals(n.getRepresentative());
    }
}
