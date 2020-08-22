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
import java.nio.file.*;

/**
 * Created by chris on 16/07/19.
 */ 

//this one uses a reasoner to create a transitive reduct
public class MostSpecificAssertions {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<OWLAxiom> axioms;


    public MostSpecificAssertions(OWLOntology o, String reasonerName) throws Exception {
        this.ontology = o;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 

        this.reasoner = ReasonerLoader.initReasoner(ReasonerName.get(reasonerName),this.ontology);
        this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY); 
        this.reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS); 

        this.axioms = new HashSet<>();
        this.build();
    }

    public Set<OWLAxiom> getAxioms(){
        return axioms;
    }

    //build set of most specific class assertions
    private void build(){
        Set<OWLNamedIndividual> individuals = this.ontology.getIndividualsInSignature(Imports.INCLUDED);
        for(OWLNamedIndividual i : individuals){
            //get (true=direct) types of individuals
            Set<Node<OWLClass>> types = this.reasoner.getTypes(i,true).getNodes();
            for(Node<OWLClass> type : types){
                //NOTE: this selects a representative system
                OWLClass typeRep = type.getRepresentativeElement();
                this.axioms.add(this.factory.getOWLClassAssertionAxiom(typeRep,i));
            }
        } 
    } 
}
