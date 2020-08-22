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

public class ClassHierarchyPrecompiler {

    private OWLOntology ontology;
    private OWLOntology classification;

    private RepresentativeTransitiveReduct TR;
    private MaterialisedClassHierarchy MCH;
    private OWLOntologyManager manager;

    private OWLOntology precompiledOntology;
    private Set<OWLAxiom> precompiledAxioms;

    public ClassHierarchyPrecompiler(OWLOntology o, OWLOntology c) throws Exception {
        this.manager = OWLManager.createOWLOntologyManager(); 
        this.ontology = o;
        this.classification = c;
        this.TR = new RepresentativeTransitiveReduct(c);
        this.MCH = new MaterialisedClassHierarchy(o); 
        this.compile(); 
    }

    //TODO: constructor for precompiler using a reasoner

    private void compile() throws Exception {
        Set<OWLAxiom> materialsedAtomicAxioms = this.MCH.getAxioms();
        Set<OWLAxiom> nonMaterialisedTransitiveReduct = new HashSet<>();
        nonMaterialisedTransitiveReduct.addAll(this.TR.getMissingDeterministicAxiomsOf(materialsedAtomicAxioms));
        nonMaterialisedTransitiveReduct.addAll(this.TR.getMissingNonDeterministicAxioms(materialsedAtomicAxioms));
        this.precompiledAxioms = new HashSet<>(nonMaterialisedTransitiveReduct);

        this.precompiledOntology = this.manager.copyOntology(this.ontology, OntologyCopy.DEEP);
        this.manager.addAxioms(this.precompiledOntology, nonMaterialisedTransitiveReduct); 
    }

    public Set<OWLAxiom> getPrecompiledAxioms(){
        return this.precompiledAxioms;
    }

    public OWLOntology getPrecompilation(){
        return this.precompiledOntology;
    } 
} 
