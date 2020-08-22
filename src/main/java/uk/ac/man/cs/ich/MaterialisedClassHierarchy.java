package uk.ac.man.cs.ich;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import java.util.*;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import java.io.FileWriter;
import org.semanticweb.owlapi.model.parameters.*;
import java.io.File;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports; 


/**
 * Created by Chris on 15/09/17.
 */
public class MaterialisedClassHierarchy {

    private OWLOntology ontology;

    private Set<OWLAxiom> axioms; 
    private Set<OWLSubClassOfAxiom> atomicSubsumptions;
    private Set<OWLEquivalentClassesAxiom> atomicEquivalences; 

    public MaterialisedClassHierarchy(OWLOntology o) throws Exception {

        this.ontology = o;

        //initialise asserted class hierarchy
        this.axioms = new HashSet<>();
        this.atomicSubsumptions = new HashSet<>();
        this.atomicEquivalences = new HashSet<>(); 
        this.initialise(); 
    }

    public MaterialisedClassHierarchy(String ontologyPath) throws Exception {
        this(OntologyLoader.load(ontologyPath)); 
    }

    public Set<OWLAxiom> getAxioms() {
        return this.axioms; 
    }

    public Set<OWLSubClassOfAxiom> getAtomicSubsumptions() {
        return this.atomicSubsumptions; 
    }

    public Set<OWLEquivalentClassesAxiom> getEquivalenceAxioms(){
        return this.atomicEquivalences; 
    }

    public OWLOntology getMaterialisation() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
        OWLOntology materialisation = manager.createOntology(IRI.generateDocumentIRI());
        ChangeApplied c1 = manager.addAxioms(materialisation, this.atomicSubsumptions);
        ChangeApplied c2 = manager.addAxioms(materialisation, this.atomicEquivalences); 
        return materialisation;
    }

    private void initialise() throws Exception {
        Set<OWLAxiom> axioms = this.ontology.getAxioms(Imports.INCLUDED); 

        for(OWLAxiom a : axioms){
            if(isAtomicSubsumption(a)){
                this.axioms.add(a);
                this.atomicSubsumptions.add((OWLSubClassOfAxiom) a);
            }
            if(isAtomicEquivalence(a)){
                this.axioms.add(a);
                this.atomicEquivalences.add((OWLEquivalentClassesAxiom) a); 
            }
        }
    } 

    private boolean isAtomicEquivalence(OWLAxiom a){
        if(a instanceof OWLEquivalentClassesAxiom){
            Set<OWLClassExpression> topLevelClasses = ((OWLEquivalentClassesAxiom) a).getClassExpressions(); //toplevel classes of the axiom
            Set<OWLClass> namedClassesHelper = ((OWLEquivalentClassesAxiom) a).getNamedClasses(); // all named classes in this equivalence axiom
            Set<OWLClassExpression> namedClasses = new HashSet<>();
            namedClasses.addAll(namedClassesHelper);

            for(OWLClassExpression c : topLevelClasses){
                if(!namedClasses.contains(c))
                    return false; 
            }

            return true;
        }
        return false;
    }

    private boolean isAtomicSubsumption(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(!subclass.isAnonymous() && !superclass.isAnonymous())
                return true; 
        }
        return false;
    } 
}
