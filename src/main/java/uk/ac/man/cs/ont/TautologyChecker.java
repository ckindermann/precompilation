package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.*; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;
import java.util.stream.*;

public final class TautologyChecker { 


    public static boolean isTautology(OWLAxiom axiom) throws Exception {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology(IRI.generateDocumentIRI());
        OWLReasoner reasoner = ReasonerLoader.initReasoner(ontology);

        return false;
    }

    public static boolean isAtomicTautology(OWLAxiom axiom){

        if(axiom instanceof OWLSubClassOfAxiom){
            OWLSubClassOfAxiom a = ((OWLSubClassOfAxiom) axiom);
            if(bottomSubsumedBy(a))
                return true;
            if(subsumedByTop(a))
                return true;
            if(subsumedBySelf(a))
                return true;
        }
        if(axiom instanceof OWLEquivalentClassesAxiom){ 
            OWLEquivalentClassesAxiom a = ((OWLEquivalentClassesAxiom) axiom);
            if(equivalentToSelf(a))
                return true;
        } 

        if(axiom instanceof OWLClassAssertionAxiom){
            OWLClassAssertionAxiom a = (OWLClassAssertionAxiom) axiom;
            OWLClassExpression c = a.getClassExpression();
            if(c.isTopEntity())
                return true;
        } 
        return false; 
    }

    //atomic equivalence axioms
    private static boolean equivalentToSelf(OWLEquivalentClassesAxiom a){
            Set<OWLClassExpression> topLevelClasses = ((OWLEquivalentClassesAxiom) a).getClassExpressions(); //toplevel classes of the axiom
            Set<OWLClass> namedClassesHelper = ((OWLEquivalentClassesAxiom) a).getNamedClasses(); // all named classes in this equivalence axiom
            //if the set is just 1 class, then this means that the equivalent classes axiom
            //is of the form A equiv A
            if(namedClassesHelper.size() < 2)
                return true; 
        return false; 
    }

    //atomic subsumption axioms
    private static boolean subsumedBySelf(OWLSubClassOfAxiom a){
        OWLClassExpression subclass = a.getSubClass();
        OWLClassExpression superclass = a.getSuperClass();
        if(subclass.compareTo(superclass) == 0)
            return true;
        return false; 
    }

    public static boolean subsumedByTop(OWLSubClassOfAxiom a){
        OWLClassExpression superclass = a.getSuperClass();
        if(superclass.isTopEntity())
            return true;
        return false; 
    }

    public static boolean bottomSubsumedBy(OWLSubClassOfAxiom a){
        OWLClassExpression subclass = a.getSubClass();
        if(subclass.isBottomEntity())
            return true;
        return false; 
    }

}
