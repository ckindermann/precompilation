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
 * Parses an ontology that
 * contains a class hierarchy 
 * represented by some transitive reduct
 */ 

public class TRParser {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private Map<OWLClass,TRNode> class2node;//OWLClass to internal representation
    private Set<TRNode> nodes;//nodes of the transitive reduct

    public TRParser(OWLOntology o){
        this.ontology = o;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        this.class2node = new HashMap<>();
        this.nodes = new HashSet<>();

        this.initialiseEquivalenceNodes();
        this.initialiseSubClassOfNodes();
        this.initialiseTop();
        this.initialiseBottom();
    }

    public Set<TRNode> getNodes(){
        return this.nodes;
    }

    public Map<OWLClass,TRNode> getClassNodeMap(){
        return this.class2node;
    } 

    private void initialiseTop(){
        OWLClass top = this.factory.getOWLThing(); 
        if(this.class2node.get(top) == null){
            TRNode topNode  = new TRNode(top);
            this.class2node.put(top,topNode);
            //create top node
            for(TRNode n : this.nodes){
                if(n.isRoot()){
                    n.addParent(topNode);
                    topNode.addChild(n); 
                } 
            }
            this.nodes.add(topNode);
        }
    }

    private void initialiseBottom(){
        OWLClass bottom = this.factory.getOWLNothing(); 
        if(this.class2node.get(bottom) == null){
            //create bottom node
            TRNode bottomNode  = new TRNode(bottom);
            this.class2node.put(bottom,bottomNode);
            //create top node
            for(TRNode n : this.nodes){
                if(n.isLeaf()){
                    n.addChild(bottomNode);
                    bottomNode.addParent(n); 
                }
            }
            this.nodes.add(bottomNode);
        }
    }

    //parses equivalence axioms
    //(which will be represented by TRNode)
    private void initialiseEquivalenceNodes(){
        Set<OWLEquivalentClassesAxiom> equivalences = this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES,Imports.INCLUDED);
        for(OWLEquivalentClassesAxiom e : equivalences){
            Set<OWLClass> classes = e.getNamedClasses();
            TRNode node = new TRNode(classes);
            this.nodes.add(node);
            for(OWLClass c : classes){
                this.class2node.putIfAbsent(c,node); 
            }
        }
    }

    //parses subclassof axioms
    //which will be represented by links between TRNodes (representing OWLClasses)
    private void initialiseSubClassOfNodes(){
        Set<OWLSubClassOfAxiom> axioms = this.ontology.getAxioms(AxiomType.SUBCLASS_OF,Imports.INCLUDED);
        for(OWLSubClassOfAxiom axiom : axioms){

            OWLClass subclass = axiom.getSubClass().asOWLClass();
            OWLClass superclass = axiom.getSuperClass().asOWLClass();

            //create nodes (if necessary)
            if(!class2node.containsKey(subclass)){
                TRNode node = new TRNode(subclass);
                this.nodes.add(node);
                this.class2node.put(subclass,node);
            }
            if(!class2node.containsKey(superclass)){
                TRNode node = new TRNode(superclass);
                this.nodes.add(node);
                this.class2node.put(superclass,node); 
            }

            //set links
            TRNode subNode = this.class2node.get(subclass);
            this.class2node.putIfAbsent(subclass,subNode);
            TRNode superNode = this.class2node.get(superclass);
            this.class2node.putIfAbsent(superclass,superNode);
            subNode.addParent(superNode);
            superNode.addChild(subNode); 
        }
    }
}
