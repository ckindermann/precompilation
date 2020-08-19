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
 * check whether ontology is (logically) empty
 *
 * NOTE: tautologies will be checked during minimisation stage: 
 * if an onotlogy ONLY includes tautologies, then they can all be removed
 * and the resulting minmised ontology will be logically empty 
 */
public class EmptinessCheck {

    public static void main(String[] args) throws Exception {

        String ontFilePath = args[0];//ontology to be tested
        String outputPath = args[1];//results to be written to

        //set up output folder structure
        String emptinessCheckOutput = outputPath + "/emptinessCheck";
        Path emptinessCheckPath = Paths.get(emptinessCheckOutput); 
        if(!Files.exists(emptinessCheckPath)){
            File emptinessCheckDir = new File(emptinessCheckOutput);
            emptinessCheckDir.mkdirs(); 
        }

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        //perform check
        try{
            if(EmptinessCheck.isLogicallyEmpty(ontFilePath)){
                IOHelper.writeAppend(ontologyName, emptinessCheckOutput + "/notPassed");
            } else {
                IOHelper.writeAppend(ontologyName, emptinessCheckOutput + "/passed"); 
            }
        } catch (Exception e) {
            //compose error message
            String report = ontologyName + ","
                + e.toString().replace("\n"," <BR> ").replace("\r"," <BR> ") + ","
                + e.getMessage().replace("\n"," <BR> ").replace("\r"," <BR> "); 

            IOHelper.writeAppend(report, emptinessCheckOutput + "/error"); 
        }
    }

    public static boolean isLogicallyEmpty(String ontologyPath) throws Exception {
        OWLOntology ontology = OntologyLoader.load(ontologyPath); 
        return EmptinessCheck.isLogicallyEmpty(ontology);

    }

    public static boolean isLogicallyEmpty(OWLOntology ontology){
        Set<OWLLogicalAxiom> axioms = ontology.getLogicalAxioms(Imports.INCLUDED);
            if(axioms.isEmpty())
                return true;
            else
                return false;
    }
}

