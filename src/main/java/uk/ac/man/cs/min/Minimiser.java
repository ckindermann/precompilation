package uk.ac.man.cs.min;

import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.precompilation.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.ich.*;

import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import java.util.*;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import java.io.FileWriter;
import org.semanticweb.owlapi.model.parameters.*;
import java.io.File;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;



/**
 * Created by chris on 15/09/17.
 * General purpose minimiser
 */
public class Minimiser {

    private OWLOntology ontology;
    private OWLOntology minimisation;
    private long timeout;
    private String reasonerName;
    private OWLOntologyManager manager;

    private Set<OWLAxiom> redundant;//not all individual redundant axioms can be removed
    private Set<OWLAxiom> removable;//maximal set can could be removed
    private Set<OWLAxiom> uncertainRedundant;
    private Set<OWLAxiom> uncertainRemovable;

    public Minimiser(OWLOntology o, String rName, long t){
        this.ontology = o;
        this.reasonerName = rName;
        this.timeout = t;
        this.manager = OWLManager.createOWLOntologyManager(); 
    } 

    public Set<OWLAxiom> getRedundant(){
        return this.redundant;
    }
    public Set<OWLAxiom> getRemovable(){
        return this.removable;
    }
    public Set<OWLAxiom> getUncertainRedundant(){
        return this.uncertainRedundant;
    }
    public Set<OWLAxiom> getUncertainRemovable(){
        return this.uncertainRemovable;
    }
    public OWLOntology getMinimisation(){
        return this.minimisation;
    }

    public void minimise(Set<OWLAxiom> toMinimise) throws Exception { 
        this.removable = new HashSet<>();
        this.uncertainRemovable = new HashSet<>();

        this.minimisation = this.manager.copyOntology(this.ontology, OntologyCopy.DEEP);

        for(OWLAxiom a : toMinimise){
            manager.removeAxiom(minimisation, a);
            try{
                if(isRedundant(minimisation, a)){
                    this.removable.add(a);
                } else {
                    manager.addAxiom(minimisation, a); //not removable, so put the axiom back in
                }
            } catch (Exception e){
                manager.addAxiom(minimisation, a); //eception occurred, so put the axiom back in
                this.uncertainRemovable.add(a);
            } 
        }
    }

    public void identifyRedundancies(Set<OWLAxiom> toTest){

        this.redundant = new HashSet<>();
        this.uncertainRedundant = new HashSet<>();

        for(OWLAxiom a : toTest){ 
            manager.removeAxiom(this.ontology, a); 
            try{
                if(isRedundant(this.ontology, a)){
                    this.redundant.add(a); 
                } 
            } catch (Exception e){
                this.uncertainRedundant.add(a); 
            }
            manager.addAxiom(this.ontology, a); 
        } 
    }

    private boolean isRedundant(OWLOntology o, OWLAxiom a) throws Exception { 
        OWLReasoner reasoner = ReasonerLoader.initReasoner(ReasonerName.get(this.reasonerName), o, this.timeout); 
        boolean res = reasoner.isEntailed(a); 
        reasoner.dispose();
        return res;
    } 

}
