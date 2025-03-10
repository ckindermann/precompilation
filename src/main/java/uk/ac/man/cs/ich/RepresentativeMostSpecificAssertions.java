package uk.ac.man.cs.ich;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;

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

//NB: this does not(!) make use of a reasoner
//this has the advantage of using the output of any tool
//that can materialise the needed entailment sets
//TODO: the class name 'representative' is not particulary well chosen
public class RepresentativeMostSpecificAssertions {

    private OWLOntology realisation;
    private OWLOntology classification;
    private OWLDataFactory factory; 
    private Set<OWLAxiom> mostSpecificClassAssertions;//as given by ontology realisation

    private RepresentativeTransitiveReduct TR;
    private Map<OWLIndividual,Set<OWLClass>> individual2mostSpecificCasses; 

    private Set<OWLAxiom> tautologicalCompletion;
    private Set<OWLAxiom> nonDeterministicAxioms;
    private Set<OWLAxiom> deterministicAxioms;


    //expects two ontologies:
    //the first is an ontology containing most specific class assertions
    //(not necessarily a cardinality minimal representation thereof!)
    //the second is an ontology containing a (transitive reduct of a) class hierarchy
    public RepresentativeMostSpecificAssertions(OWLOntology realisation, OWLOntology classhierarchy) throws Exception {
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 

        this.realisation =  realisation;
        this.classification = classhierarchy;
        this.TR = new RepresentativeTransitiveReduct(this.classification);

        this.initialiseMostSpecificAssertions();
        this.initialiseMostSpecificClassesMap();
        this.initialseRepresentativeSystem(); 

    }
    //
    public RepresentativeMostSpecificAssertions(String realisationPath, String classificationPath) throws Exception{
        this(OntologyLoader.load(realisationPath), OntologyLoader.load(classificationPath));
    }

    public Set<OWLAxiom> getRepresentativeMSCA(){
        Set<OWLAxiom> rep = new HashSet<>();
        rep.addAll(this.tautologicalCompletion);
        rep.addAll(this.nonDeterministicAxioms);
        rep.addAll(this.deterministicAxioms);
        return rep; 
    }

    //get 'most specific class assertions' from realisation ontology
    private void initialiseMostSpecificAssertions(){
        this.mostSpecificClassAssertions = new HashSet<>();
        Set<OWLNamedIndividual> individuals = this.realisation.getIndividualsInSignature(Imports.INCLUDED);
        for(OWLNamedIndividual i : individuals){
            Set<OWLClassAssertionAxiom> realisations = this.realisation.getClassAssertionAxioms(i); 
                this.mostSpecificClassAssertions.addAll(realisations);
        }
    }

    //map all individuals to their (example) most specific classes
    private void initialiseMostSpecificClassesMap(){
        this.individual2mostSpecificCasses = new HashMap<>();
        for(OWLAxiom a : this.mostSpecificClassAssertions){
            if(a instanceof OWLClassAssertionAxiom){
                OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) a;
                OWLClassExpression c = axiom.getClassExpression();
                OWLIndividual i = axiom.getIndividual();
                this.individual2mostSpecificCasses.putIfAbsent(i,new HashSet<>());
                this.individual2mostSpecificCasses.get(i).add(c.asOWLClass()); 
            }
        }
    }

    public void initialseRepresentativeSystem(){
        this.tautologicalCompletion = new HashSet<>();
        this.deterministicAxioms = new HashSet<>();
        this.nonDeterministicAxioms = new HashSet<>();
        for(OWLAxiom a : this.mostSpecificClassAssertions){
            if(a instanceof OWLClassAssertionAxiom){
                OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) a;
                OWLClassExpression c = axiom.getClassExpression();
                OWLIndividual i = axiom.getIndividual();

                TRNode node = this.TR.getNode(c.asOWLClass());
                if(node.isRoot()){//we assume owl:Thing always to be the top node
                    this.tautologicalCompletion.add(this.getRepresentative(a)); 
                } else {
                    //check whether c is a singleton node
                    if(node.isSingleton()){
                        this.deterministicAxioms.add(this.getRepresentative(a)); 
                    } else {
                        this.nonDeterministicAxioms.add(this.getRepresentative(a));
                    }
                }
            }
        }
    }

    //public Set<OWLAxiom> getMaterialsedDeterministicClassAssertions(Set<OWLAxiom> axioms){
    public Set<OWLAxiom> getCoveredDeterminisitcClassAssertions(Set<OWLAxiom> axioms){
        Set<OWLAxiom> mapping = this.getRepresentatives(axioms);
        mapping.retainAll(this.deterministicAxioms);
        return mapping; 
    }

    public Set<OWLAxiom> getCoveredNonDeterministicClassAssertions(Set<OWLAxiom> axioms){
        Set<OWLAxiom> mapping = this.getRepresentatives(axioms);
        mapping.retainAll(this.nonDeterministicAxioms);
        return mapping; 
    }

    public Set<OWLAxiom> getMissingDeterministicClassAssertionsOf(Set<OWLAxiom> axioms){
        Set<OWLAxiom> det = new HashSet<>(this.deterministicAxioms);
        Set<OWLAxiom> mapping = this.getRepresentatives(axioms);
        det.removeAll(mapping);
        return det; 
    }

    public Set<OWLAxiom> getMissingNonDeterministicClassAssertionsOf(Set<OWLAxiom> axioms){
        Set<OWLAxiom> nonDet = new HashSet<>(this.nonDeterministicAxioms);
        Set<OWLAxiom> mapping = this.getRepresentatives(axioms);
        nonDet.removeAll(mapping);
        return nonDet; 
    }

    public Set<OWLAxiom> getRepresentatives(Set<OWLAxiom> axioms){
        Set<OWLAxiom> mapping = new HashSet<>();
        for(OWLAxiom a : axioms){
            mapping.add(this.getRepresentative(a));
        }
        return mapping; 
    }

    public OWLAxiom getRepresentative(OWLAxiom a){
        if(isAtomicClassAssertion(a)){
            OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) a;
            OWLClassExpression c = axiom.getClassExpression();
            OWLIndividual i = axiom.getIndividual();
            OWLClass rep = this.TR.getNode(c.asOWLClass()).getRepresentative();
            return this.factory.getOWLClassAssertionAxiom(rep,i); 
        } 
        return a;
    }

    private boolean isAtomicClassAssertion(OWLAxiom a){ 
            if(a instanceof OWLClassAssertionAxiom){
                OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) a;
                OWLClassExpression c = axiom.getClassExpression();
                return !c.isAnonymous(); 
            }
            return false;
    }

    public boolean isMostSpecificClassAssertion(OWLAxiom a){
        OWLAxiom rep = getRepresentative(a);
        if(this.deterministicAxioms.contains(a))
            return true;
        if(this.nonDeterministicAxioms.contains(a))
            return true;
        if(this.tautologicalCompletion.contains(a))
            return true;
        return false;
    }

    public boolean isMostSpecificTautology(OWLAxiom a){
        if(this.tautologicalCompletion.contains(a))
            return true;
        return false; 
    }

    public boolean hasMostSpecificClassAssertions(){
        if(!nonDeterministicAxioms.isEmpty()){
            return true;
        }
        if(!deterministicAxioms.isEmpty()){
            return true;
        }
        return false; 
    }
}
