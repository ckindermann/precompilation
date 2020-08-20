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
 */
public class BruteForceHierarchyMinimiser { 

    public static void main(String[] args) throws  Exception {

        String ontPath = args[0];//ontology to be tested
        String reasonername = args[1];
        long timeout = Long.parseLong(args[2]);//ontology to be tested
        String output = args[3];

        OWLOntology ontology = OntologyLoader.load(ontPath);
        ExplicitClassHierarchy ech = new ExplicitClassHierarchy(ontology);

        Set<OWLAxiom> atomicAxioms = ech.getAxioms();
        Minimiser minimiser = new Minimiser(ontology,reasonername,timeout);
        minimiser.identifyRedundancies(atomicAxioms);
        Set<OWLAxiom> redundant = minimiser.getRedundant();
        Set<OWLAxiom> uncertainRedundant = minimiser.getUncertainRedundant();

        minimiser.minimise(redundant);
        Set<OWLAxiom> removed = minimiser.getRemovable();
        Set<OWLAxiom> uncertainRemovable = minimiser.getUncertainRemovable();

        OWLOntology minimisation = minimiser.getMinimisation();

        OntologySaver.saveAxioms(redundant, output + "/redundant");
        OntologySaver.saveAxioms(uncertainRedundant, output + "/uncertainRedundant");
        OntologySaver.saveAxioms(removed, output + "/removed");
        OntologySaver.saveAxioms(uncertainRemovable, output + "/uncertainRemovable");
        OntologySaver.saveOntology(minimisation, output + "/minimisation"); 
    } 
}
