package uk.ac.man.cs.comp;

import uk.ac.man.cs.util.*;
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
import java.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.nio.file.*;

import org.semanticweb.owlapi.change.AxiomChangeData;
import org.semanticweb.owlapi.util.*;


/**
 * Created by chris on 15/09/18.
 */

public class MostSpecificClassAssertionPrecompiler {

    private OWLOntology ontology;
    private OWLOntology classification;
    private RepresentativeMostSpecificAssertions MSCA;
    private OWLOntologyManager manager;

    private OWLOntology precompiledOntology;//precompiled ontology
    private Set<OWLAxiom> precompiledAxioms;//injection

    public MostSpecificClassAssertionPrecompiler(OWLOntology o, OWLOntology c, OWLOntology r) throws Exception {
        this.manager = OWLManager.createOWLOntologyManager(); 
        this.ontology = o;
        this.classification = c;
        this.MSCA = new RepresentativeMostSpecificAssertions(r,c);
        this.compile();
    }

    private void compile() throws Exception {
        Set<OWLAxiom> classAssertions = new HashSet<>(this.ontology.getAxioms(AxiomType.CLASS_ASSERTION, Imports.INCLUDED));

        Set<OWLAxiom> nonMaterialisedMSCA = new HashSet();
        nonMaterialisedMSCA.addAll(this.MSCA.getMissingDeterministicClassAssertionsOf(classAssertions));
        nonMaterialisedMSCA.addAll(this.MSCA.getMissingNonDeterministicClassAssertionsOf(classAssertions)); 
        this.precompiledAxioms = new HashSet<>(nonMaterialisedMSCA);

        this.precompiledOntology = this.manager.copyOntology(this.ontology, OntologyCopy.DEEP);
        this.manager.addAxioms(this.precompiledOntology, nonMaterialisedMSCA); 
        this.manager.addAxioms(this.precompiledOntology, this.ontology.getAxioms());

    }

    public Set<OWLAxiom> getPrecompiledAxioms(){
        return this.precompiledAxioms;
    }

    public OWLOntology getPrecompilation(){
        return this.precompiledOntology;
    } 
}
