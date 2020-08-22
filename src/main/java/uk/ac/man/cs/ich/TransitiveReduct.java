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
public class TransitiveReduct {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<OWLAxiom> axioms;

    public TransitiveReduct(OWLOntology o, String reasonerName) throws Exception {
        this.ontology = o;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 

        this.reasoner = ReasonerLoader.initReasoner(ReasonerName.get(reasonerName),this.ontology);
        this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY); 
        this.axioms = new HashSet<>();
        this.build();
    }

    public Set<OWLAxiom> getAxioms(){
        return axioms;
    }

    //build hierarchy bottom up
    private void build(){

        Node<OWLClass> bottomNode = this.reasoner.getBottomClassNode();

        Set<Node<OWLClass>> queue = new HashSet<>();
        queue.add(bottomNode);
        Set<Node<OWLClass>> level = new HashSet<>();

        while(!queue.isEmpty()){
            level.addAll(queue);
            queue.clear();
            for(Node<OWLClass> n : level){
                this.handleNode(n);
                queue.addAll(this.reasoner.getSuperClasses(n.getRepresentativeElement(),true).getNodes());
            } 
            level.clear();
        }
    }

    private void handleNode(Node<OWLClass> n){
        if(!n.isSingleton()){ 
            this.axioms.add(this.factory.getOWLEquivalentClassesAxiom(n.getEntities())); 
        }
        //get representative
        OWLClass rep = n.getRepresentativeElement();
        //get (true=direct) superclass nodes
        Set<Node<OWLClass>> superclassNodes = this.reasoner.getSuperClasses(rep,true).getNodes();
        //for each node get an axiom
        for(Node<OWLClass> sup : superclassNodes) {
            OWLClass supRep = sup.getRepresentativeElement();
            this.axioms.add(this.factory.getOWLSubClassOfAxiom(rep, supRep)); 
        }
    }
}
