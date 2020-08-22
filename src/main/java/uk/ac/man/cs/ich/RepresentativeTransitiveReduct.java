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
import java.io.*;
import java.util.stream.*;

/**
 * Created by chris on 16/07/19.
 */

public class RepresentativeTransitiveReduct {

    private OWLOntology ontology;
    private OWLDataFactory factory;

    private Set<TRNode> nodes;
    private Set<TRNode> nonSingletons;//nodes with more than one Class
    private Set<TRNode> singletons;
    private Set<TRNode> roots;
    private Set<TRNode> leaves;

    private Map<OWLClass,TRNode> class2node;

    //(some) representative system for the TR
    private Set<OWLAxiom> tautologicalCompletion;
    private Set<OWLAxiom> nonDeterministicAxioms;
    private Set<OWLAxiom> deterministicAxioms;

    //expects an ontology which is (transitive reduct of) a class hierarchy
    public RepresentativeTransitiveReduct(OWLOntology o) throws Exception {
        //TODO: make sure the given ontology is indeed an transitive reduct class hierarchy
        this.ontology = o;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 

        //parses a class hierarchy (stored in an ontology)
        //into an internal structure based on 'TRNodes'
        TRParser parser = new TRParser(this.ontology); 
        this.nodes = parser.getNodes();
        this.class2node = parser.getClassNodeMap();

        this.initialise(); 
    }

    //TODO: constructor that accepts a TransitiveReduct
    //public RepresentativeTransitiveReduct(TransitiveReduct tr)

    private void initialise(){
        this.roots = new HashSet<>();
        this.leaves = new HashSet<>();
        this.singletons = new HashSet<>();
        this.nonSingletons = new HashSet<>();

        for(TRNode n : this.nodes){
            if(n.isRoot()){
                this.roots.add(n);
            } 
            if(n.isLeaf()){
                this.leaves.add(n);
            }
            if(n.isSingleton()){
                this.singletons.add(n);
            }
            if(!n.isSingleton()){
                this.nonSingletons.add(n);
            }
        }

        this.tautologicalCompletion = this.getTautologicalCompletion();
        this.nonDeterministicAxioms = this.getNonDeterministicAxioms();
        this.deterministicAxioms = this.getDeterministicAxioms();
    }

    public boolean isEmpty(){
        if(!this.deterministicAxioms.isEmpty())
            return false;
        if(!this.nonDeterministicAxioms.isEmpty())
            return false;
        return true; 
    }

    //returned set of axioms are necessarily in a transitive reduct
    //(put differently: they are in all transitive reducts of the given class hierarchy)
    private Set<OWLAxiom> getDeterministicAxioms(){
        Set<OWLAxiom> deterministicAxioms = new HashSet<>();
        //all singletons to singletons
        for(TRNode node : this.nodes){
            if(node.isSingleton()){
                OWLClass a = node.getRepresentative();
                for(TRNode child : node.getChildren()){
                    if(child.isSingleton()){
                        OWLClass b = child.getRepresentative();
                        OWLSubClassOfAxiom axiom = this.factory.getOWLSubClassOfAxiom(b,a);
                        if(!this.tautologicalCompletion.contains(axiom)){
                            deterministicAxioms.add(axiom);
                        } 
                    } 
                } 
            } else {
                deterministicAxioms.add(this.factory.getOWLEquivalentClassesAxiom(node.getClasses()));
            }
        } 
        return deterministicAxioms;
    }

    //get axioms that are not in all(!) transitive reducts
    //(these are axioms that 'connect' TRNodes with multiple classes,
    //i.e, equivalence classses
    private Set<OWLAxiom> getNonDeterministicAxioms(){
        Set<OWLAxiom> nonDeterministicAxioms = new HashSet<>();

        //iterate ofer TRNodes with equivalent classes
        for(TRNode n : this.nonSingletons){
            OWLClass eqRep = n.getRepresentative();
            //trickle down
            for(TRNode c : n.getChildren()){
                OWLClass childRep = c.getRepresentative();
                OWLSubClassOfAxiom axiom = this.factory.getOWLSubClassOfAxiom(childRep,eqRep);
                if(!this.tautologicalCompletion.contains(axiom)){
                    nonDeterministicAxioms.add(axiom);
                }
            }
            //trickle up
            for(TRNode p : n.getParents()){
                OWLClass parentRep = p.getRepresentative();
                OWLSubClassOfAxiom axiom = this.factory.getOWLSubClassOfAxiom(eqRep,parentRep);
                if(!this.tautologicalCompletion.contains(axiom)){
                    nonDeterministicAxioms.add(axiom);
                }
            }
            //NB: this does not create duplicates
            //in case of links between two nonSingleton nodes;
            //tricklilng up and down creates the same axiom
            //because the same representatives are selected
        }

        return nonDeterministicAxioms;
    }

    private Set<OWLAxiom> getTautologicalCompletion(){
        Set<OWLAxiom> tautCompletionRepresentation = new HashSet<>();

        //handle root (top/thing) case
        OWLClass top = this.factory.getOWLThing();
        TRNode topNode = this.class2node.get(top);
        if(topNode != null){
            OWLClass topRep = topNode.getRepresentative();
            for(TRNode c : topNode.getChildren()){
                OWLClass childRep = c.getRepresentative();
                tautCompletionRepresentation.add(this.factory.getOWLSubClassOfAxiom(childRep,topRep)); 
            }
        } else { //this case should never occur due to the way the TRParser works
            for(TRNode r : this.roots){
                OWLClass rootRep = r.getRepresentative();
                tautCompletionRepresentation.add(this.factory.getOWLSubClassOfAxiom(rootRep,top));
            } 
        }

        //handle leaf case
        OWLClass bottom = this.factory.getOWLNothing();
        TRNode botNode = this.class2node.get(bottom);
        if(botNode != null){
            OWLClass botRep = botNode.getRepresentative();
            for(TRNode p : botNode.getParents()){
                OWLClass parentRep = p.getRepresentative();
                tautCompletionRepresentation.add(this.factory.getOWLSubClassOfAxiom(botRep,parentRep));
            }
        } else {//this case should never occur due to the way the TRParser works
            for(TRNode l : this.leaves){
                OWLClass leafRep = l.getRepresentative();
                tautCompletionRepresentation.add(this.factory.getOWLSubClassOfAxiom(bottom,leafRep));
            }
        }
        return tautCompletionRepresentation; 
    }


    //for a given a set of axioms
    //return (a representative set of) axioms in the tautological completion
    //that are not contained in the set of input axioms
    //public Set<OWLAxiom> getNonMaterialisedTautologicalCompletion(Set<OWLAxiom> axioms){
    public Set<OWLAxiom> getMissingTautologicalCompletionOf(Set<OWLAxiom> axioms){
        Set<OWLAxiom> axiomMapping = getRepresentatives(axioms); 
        //make a copy of tautological completion 
        Set<OWLAxiom> tautologicalCompletion = new HashSet<>(this.tautologicalCompletion);
        tautologicalCompletion.removeAll(axiomMapping);
        return tautologicalCompletion;
    }

    //public Set<OWLAxiom> getNonMaterialisedDeterministicAxioms(Set<OWLAxiom> axioms){
    public Set<OWLAxiom> getMissingDeterministicAxiomsOf(Set<OWLAxiom> axioms){
        //NOTE: since these axioms are NECESSARILY contained in
        //ALL transitive reducts, there is no need map them to the representative system
        Set<OWLAxiom> deterministic = new HashSet<>(this.deterministicAxioms);
        deterministic.removeAll(axioms);
        return deterministic; 
    }

    public Set<OWLAxiom> getMissingNonDeterministicAxioms(Set<OWLAxiom> axioms){
        //map given axioms to representative
        Set<OWLAxiom> axiomMapping = getRepresentatives(axioms); 
        //Set<OWLAxiom> links = this.getNonDeterministicAxioms(); //get representative system
        Set<OWLAxiom> links = new HashSet<>(this.nonDeterministicAxioms);
        links.removeAll(axiomMapping); //removed covered representatives
        return links;
    }

    //given a set of axioms
    //map them into the representative system of this.RepresentativeTransitiveReduct 
    public Set<OWLAxiom> getRepresentatives(Set<OWLAxiom> axioms){
        Set<OWLAxiom> axiomMapping = new HashSet<>();
        for(OWLAxiom a : axioms){
            axiomMapping.add(this.getRepresentative(a));
        }
        return axiomMapping; 
    }

    //mapps an axiom to the corresponding representative in this.RepresentativeTransitiveReduct
    //returns the axiom itself if there is no representative
    public OWLAxiom getRepresentative(OWLAxiom axiom){
            if(isAtomicSubsumptionAxiom(axiom)){//is an atomic axiom
                OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) axiom;
                OWLClass subclass = sca.getSubClass().asOWLClass();
                OWLClass superclass = sca.getSuperClass().asOWLClass();

                if(this.class2node.containsKey(subclass) &&
                    this.class2node.containsKey(superclass)){
                    OWLClass repSubclass = class2node.get(subclass).getRepresentative();
                    OWLClass repSuperclass = class2node.get(superclass).getRepresentative();
                    return this.factory.getOWLSubClassOfAxiom(repSubclass,repSuperclass); 
                }
            } 
            return axiom; //return original axiom otherwise
    }

    public OWLClass getRepresentative(OWLClass c){
        TRNode node = this.class2node.get(c);
        return node.getRepresentative();
    }

    public Set<OWLAxiom> getMaterialisedTautologicalCompletion(Set<OWLAxiom> axioms){
        Set<OWLAxiom> mapping = getRepresentatives(axioms);
        mapping.retainAll(this.tautologicalCompletion); 
        return mapping;
    }

    public Set<OWLAxiom> getMaterialisedDeterministicAxioms(Set<OWLAxiom> axioms){ 
        Set<OWLAxiom> mapping = getRepresentatives(axioms);
        mapping.retainAll(this.deterministicAxioms);
        return mapping;
    }

    public Set<OWLAxiom> getMaterialisedNonDeterministicAxioms(Set<OWLAxiom> axioms){
        Set<OWLAxiom> mapping = getRepresentatives(axioms);
        mapping.retainAll(this.nonDeterministicAxioms);
        return mapping;
    }

    public Set<OWLAxiom> getRepresentativeReduct(){
        Set<OWLAxiom> rep = new HashSet<>();
        rep.addAll(this.tautologicalCompletion);
        rep.addAll(this.nonDeterministicAxioms);
        rep.addAll(this.deterministicAxioms);
        return rep;
    }

    public Set<OWLClass> getProperSubClasses(OWLClass c){
        if(c.isBottomEntity()){
            return new HashSet<>();
        }
        Set<OWLClass> subclasses = new HashSet<>();
        TRNode classNode = this.class2node.get(c);

        Set<TRNode> subclassNodes = new HashSet<>();
        Set<TRNode> queue = new HashSet<>();
        queue.addAll(classNode.getChildren());
        while(!queue.isEmpty()){
            subclassNodes.addAll(queue);
            Set<TRNode> next = new HashSet<>();
            for(TRNode n : queue){
                next.addAll(n.getChildren());
            } 
            queue.clear();
            queue.addAll(next); 
        }

        for(TRNode n : subclassNodes){
            subclasses.add(n.getRepresentative());
        } 

        return subclasses;
    }

    public TRNode getNode(OWLClass c){
        return this.class2node.get(c);
    } 

    private boolean isAtomicSubsumptionAxiom(OWLAxiom a){
            if(a instanceof OWLSubClassOfAxiom){
                OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) a;
                OWLClassExpression subclass = sca.getSubClass();
                OWLClassExpression superclass = sca.getSuperClass();
                return (!subclass.isAnonymous() && !superclass.isAnonymous()); 
            }
            return false;
    } 
}
