package uk.ac.man.cs.pre;

import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.*;

/**
 * Created by chris on 15/09/18.
 */
public class LoadCheck {

    public static void main(String[] args) throws Exception {

        String ontFilePath = args[0];//ontology to be tested
        String outputPath = args[1];//results to be written to

        //set up output folder structure
        String loadCheckOutput = outputPath + "/loadCheck"; 
        Path loadCheckPath = Paths.get(loadCheckOutput); 
        if(!Files.exists(loadCheckPath)){
            File loadCheckDir = new File(loadCheckOutput);
            loadCheckDir.mkdirs(); 
        }

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        //perform check
        try{
            if(LoadCheck.isLoadable(ontFilePath)){
                IOHelper.writeAppend(ontologyName, loadCheckOutput + "/passed");
            }
        } catch (Exception e) {
            //compose error message
            String report = ontologyName + ","
                + e.toString().replace("\n"," <BR> ").replace("\r"," <BR> ") + ","
                + e.getMessage().replace("\n"," <BR> ").replace("\r"," <BR> "); 

            IOHelper.writeAppend(report, loadCheckOutput + "/error"); 
        }
    }

    public static boolean isLoadable(String ontologyPath) throws Exception { 
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File(ontologyPath);

        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile); 
            return true;
        } catch (Exception e) {
            throw e;
        } 
    } 
}


