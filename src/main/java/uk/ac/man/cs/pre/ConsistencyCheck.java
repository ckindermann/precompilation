package uk.ac.man.cs.pre;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;


import java.util.ConcurrentModificationException;
import java.util.Timer;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import java.util.*;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import java.io.FileWriter;
import org.semanticweb.owlapi.model.parameters.*;
import java.io.File;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.nio.file.*;



/**
 * Created by chris on 15/09/18.
 */
public class ConsistencyCheck {

    public static void main(String[] args) throws Exception {

        String ontFilePath = args[0];//ontology to be tested
        String reasoner = args[1];//reasoner to be tested
        long timeout = Long.parseLong(args[2]);
        String outputPath = args[3];//results to be written to

        String consistencyOutput = outputPath + "/consistencyCheck/" + reasoner;
        Path consistencyCheckPath = Paths.get(consistencyOutput); 
        if(!Files.exists(consistencyCheckPath)){
            File consistencyCheckDir = new File(consistencyOutput);
            consistencyCheckDir.mkdirs(); 
        } 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        boolean consistent = false;
        try{
            if(isConsistent(ontFilePath, reasoner, timeout)){
                System.out.println("\t" + reasoner + "\t" + "Consistency Check: \t\t Passed");
                IOHelper.writeAppend(ontologyName, consistencyOutput + "/passed");
                consistent = true;
            } else {
                System.out.println("\t" + reasoner + "\t" + "Consistency Check: \t\t Not Passed - abort"); 
                IOHelper.writeAppend(ontologyName + ",inconsistent", consistencyOutput + "/notPassed");
            }
        } catch (Exception e) {
            //compose error message
            String report = ontologyName + ","
                + e.toString().replace("\n"," <BR> ").replace("\r"," <BR> ");

            IOHelper.writeAppend(report, consistencyOutput + "/error"); 
        } 
    } 

    public static boolean isConsistent(String ontologyPath, String reasonerName, long timeout) throws Exception { 

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
        File ontologyFile = new File(ontologyPath);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile); 

        try {
            OWLReasoner reasoner = ReasonerLoader.initReasoner(ReasonerName.get(reasonerName), ontology); 

            Timer timer = new Timer(true);
            timer.schedule(new InterruptReasonerTask(reasoner), timeout);//TODO: this is rather cumbersome

            boolean res = reasoner.isConsistent();
            return res;

        } catch (ReasonerInterruptedException e){
            throw e;
        } catch (Exception e) {
            throw e;
        }
    } 
}
