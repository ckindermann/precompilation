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

public class ClassHierarchyPrecompilationMaterialiser {

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0];
        String matClassHierarchy = args[1];
        String outputPath = args[2];

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        ClassHierarchyPrecompiler precompiler =
            new ClassHierarchyPrecompiler(OntologyLoader.load(ontFilePath),
                                          OntologyLoader.load(matClassHierarchy));

        OWLOntology precompiledOntology = precompiler.getPrecompilation();
        Set<OWLAxiom> precompilation = precompiler.getPrecompiledAxioms();
        OntologySaver.saveAxioms(precompilation, outputPath + "/injection");
        OntologySaver.saveOntology(precompiledOntology, outputPath + "/precompilation.owl"); 
    }
}
