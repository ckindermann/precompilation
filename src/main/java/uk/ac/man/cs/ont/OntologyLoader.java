package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.File;
import java.nio.file.Paths; 
import java.nio.file.Files;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;


/**
 * Created by chris on 22/09/19.
 */
public class OntologyLoader {

    public static OWLOntology load(String pathToOntology) throws Exception { 
        if(!Files.exists(Paths.get(pathToOntology))){
            throw new FileNotFoundException(pathToOntology + " does not exist!");
        }

        File ontFile = new File(pathToOntology); 
        return OntologyLoader.load(ontFile); 
    }

    public static OWLOntology load(File ontFile) throws Exception { 
        if(!ontFile.exists()){
            throw new FileNotFoundException(ontFile.getAbsolutePath() + " does not exist!");
        } 
        if(ontFile.isDirectory()){
            throw new FileNotFoundException(ontFile.getAbsolutePath() + " is a directory!");
        }
        if(!ontFile.canRead()){
            throw new AccessDeniedException(ontFile.getAbsolutePath() + " cannot read file!");
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager.loadOntologyFromOntologyDocument(ontFile); 
    }

    //create same for file argument
    public static OWLOntology loadWithImports(String pathToOntology) throws Exception { 
        if(!Files.exists(Paths.get(pathToOntology))){
            throw new FileNotFoundException(pathToOntology + " does not exist!");
        } 

        File ontFile = new File(pathToOntology); 
        return OntologyLoader.loadWithImports(ontFile);
    } 

    public static OWLOntology loadWithImports(File ontFile) throws Exception {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        AutoIRIMapper mapper = new AutoIRIMapper(ontFile.getParentFile(), true);
        OWLOntologyManager tempManager = OWLManager.createOWLOntologyManager();
        tempManager.addIRIMapper(mapper);

        OWLOntology o = tempManager.loadOntologyFromOntologyDocument(ontFile);
        return manager.createOntology(o.getAxioms(Imports.INCLUDED)); 
    }
}
