package uk.ac.man.cs.mat;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.ich.*;
import uk.ac.man.cs.comp.*;

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
import java.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.nio.file.*;

import org.semanticweb.owlapi.change.AxiomChangeData;
import org.semanticweb.owlapi.util.*;



/**
 * Created by chris on 15/09/17.
 */

//get ontology (minimised)
//get classification (materialised)
//

public class MSCAPrecompilationMaterialiser {

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0];
        String mostSpecificRepresentative = args[1];
        String classificationFile = args[2];
        String outputPath = args[3];

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        MostSpecificClassAssertionPrecompiler precompiler =
            new MostSpecificClassAssertionPrecompiler(OntologyLoader.load(ontFilePath),
                                                      OntologyLoader.load(classificationFile),
                                                      OntologyLoader.load(mostSpecificRepresentative));

        OWLOntology precompiledOntology = precompiler.getPrecompilation();//ontology
        Set<OWLAxiom> precompilation = precompiler.getPrecompiledAxioms();//injection
        OntologySaver.saveAxioms(precompilation, outputPath + "/injection");
        OntologySaver.saveOntology(precompiledOntology, outputPath + "/precompilation.owl");
    }

}
