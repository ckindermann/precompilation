package uk.ac.man.cs.mat;

import uk.ac.man.cs.util.*;
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
 * Created by chris on 15/09/17.
 */
public class AboxRemover {

    public static void main(String[] args) throws  Exception {

        String ontFilePath = args[0];//ontology to be tested
        String outputPath = args[1];//results to be written to

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        if(!Files.exists(Paths.get(outputPath))){
            File outputDir = new File(outputPath);
            outputDir.mkdirs(); 
        }

        try{ 
            OWLOntology ontologyWithoutAbox = AboxRemover.getOntology(ontFilePath);
            OntologySaver.saveOntology(ontologyWithoutAbox, outputPath + "/" + ontologyName); 

        } catch (Exception e){
            IOHelper.writeAppend(ontologyName + "," + e.toString() + "," + e.getMessage(), outputPath + "/notPassed"); 

        } 
    } 

    public static OWLOntology getOntology(String ontologyPath) throws Exception {

        File ontologyFile = new File(ontologyPath);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile); 

        Set<OWLAxiom> aBoxAxioms = ontology.getABoxAxioms(Imports.INCLUDED);
        ChangeApplied c = manager.removeAxioms(ontology, aBoxAxioms); 

        return ontology;
    }
}
