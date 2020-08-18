package uk.ac.man.cs.ont;

import uk.ac.man.cs.util.*;

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

public class AxiomFilter { 

    public static Set<OWLAxiom> removeTautologies(Set<OWLAxiom> axioms){ 
        HashSet<OWLAxiom> toBeRemoved = new HashSet<>();
        for(OWLAxiom a : axioms){ 
            boolean tautology = false;
            if(subsumedByTop(a))
                tautology = true;
            if(bottomSubsumedBy(a))
                tautology = true; 
            if(tautology)
                toBeRemoved.add(a);
        }
        Set<OWLAxiom> res = new HashSet<>(axioms);
        res.removeAll(toBeRemoved); 

        return res;
    } 

    private static boolean subsumedByTop(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(superclass.isTopEntity())
                return true;
        } 
        return false; 
    }

    private static boolean bottomSubsumedBy(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            if(subclass.isBottomEntity())
                return true;
        } 
        return false; 
    }

    //private static boolean subsumedBySelf

    //TODO: self subsumption
}
