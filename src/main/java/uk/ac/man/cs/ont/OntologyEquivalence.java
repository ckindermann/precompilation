package uk.ac.man.cs.ont;

import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.precompilation.*;
import uk.ac.man.cs.ont.*;

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
import java.nio.file.*;


/**
 * Created by slava on 15/09/17.
 */

//given two minimisations, test whether they are equal
public class OntologyEquivalence {

    public static void main(String[] args) throws  Exception {

        String ontFile1 = args[0];//ontology to be tested
        String ontFile2 = args[1];//ontology to be tested
        String reasonerName = args[2];
        String outputPath = args[3];//results to be written to

        String ontologyName = Paths.get(ontFile1).getFileName().toString();

        OWLOntology o1 = OntologyLoader.load(ontFile1);
        OWLOntology o2 = OntologyLoader.load(ontFile2);

        Set<OWLLogicalAxiom> o1Axioms = o1.getLogicalAxioms(Imports.INCLUDED);
        Set<OWLLogicalAxiom> o2Axioms = o2.getLogicalAxioms(Imports.INCLUDED);

        OWLReasoner reasoner1 = ReasonerLoader.initReasoner(ReasonerName.get(reasonerName),o1); 
        reasoner1.precomputeInferences(InferenceType.CLASS_HIERARCHY); 
        OWLReasoner reasoner2 = ReasonerLoader.initReasoner(ReasonerName.get(reasonerName),o2); 
        reasoner2.precomputeInferences(InferenceType.CLASS_HIERARCHY); 

        boolean equiv = true;
        for(OWLLogicalAxiom a : o1Axioms){
            if(!reasoner2.isEntailed(a)){
                equiv = false;
                break;
            }
        }

        if(equiv){
            for(OWLLogicalAxiom a : o2Axioms){
                if(!reasoner1.isEntailed(a)){
                    equiv = false;
                    break;
                }
            } 
        }

        if(equiv){
            IOHelper.writeAppend(ontologyName, outputPath + "/equivalent"); 
        } else {
            IOHelper.writeAppend(ontologyName, outputPath + "/notEquivalent"); 
        } 
    }
}

