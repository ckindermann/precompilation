package uk.ac.man.cs.pre;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;
//import uk.ac.man.cs.precompilation.*;

import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.*;
import java.io.FileWriter;
import java.util.*;
import java.io.File;
import java.nio.file.*;

/**
 * Created by chris on 15/09/18.
 *
 * check whether ontology has a non empty tbox 
 * 
 *
 */
public class TboxEmptinessCheck {

    public static void main(String[] args) throws Exception {

        String ontFilePath = args[0];//ontology to be tested
        String outputPath = args[1];//results to be written to

        String emptinessCheckOutput = outputPath + "/tboxCheck";
        Path emptinessCheckPath = Paths.get(emptinessCheckOutput); 
        if(!Files.exists(emptinessCheckPath)){
            File emptinessCheckDir = new File(emptinessCheckOutput);
            emptinessCheckDir.mkdirs(); 
        }

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        //perform check
        try{
            if(TboxEmptinessCheck.hasTbox(OntologyLoader.load(ontFilePath))){
                IOHelper.writeAppend(ontologyName, emptinessCheckOutput + "/passed");
            } else {
                IOHelper.writeAppend(ontologyName, emptinessCheckOutput + "/notPassed"); 
            }
        } catch (Exception e) {
            //compose error message
            String report = ontologyName + ","
                + e.toString().replace("\n"," <BR> ").replace("\r"," <BR> ") + ","
                + e.getMessage().replace("\n"," <BR> ").replace("\r"," <BR> "); 

            IOHelper.writeAppend(report, emptinessCheckOutput + "/error"); 
        }
    }

    //this check does not check for tautologies
    public static boolean hasTbox(OWLOntology ontology){
        Set<OWLAxiom> axioms = ontology.getTBoxAxioms(Imports.INCLUDED);
            if(axioms.isEmpty())
                return false;
            else
                return true;
    }
}
