package org.uam.eps.miso.pallete.mobile.main;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import dslHeuristicVisualization.HeuristicStrategy;
import dslHeuristicVisualization.RepreHeurSS;
import dslHeuristicVisualization.impl.DslHeuristicVisualizationFactoryImpl;
import graphic_representation.AffixedCompartmentElement;
import graphic_representation.AllElements;
import graphic_representation.DiagramElement;
import graphic_representation.Edge;
import graphic_representation.GraphicRepresentation;
import graphic_representation.Layer;
import graphic_representation.MMGraphic_Representation;
import graphic_representation.Node;
import graphic_representation.PaletteDescriptionLink;
import graphic_representation.Representation;
import graphic_representation.RepresentationDD;
import graphic_representation.impl.Graphic_representationFactoryImpl;
import splitterLibrary.EcoreEMF;
import splitterLibrary.impl.SplitterLibraryFactoryImpl;

public class PaletteMain {

	private static final String generate_pallete = "gp";
	private static final String generate_pallete_long = "generatepallete";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	final CommandLineParser cmdLineGnuParser = new GnuParser();
		
	final Options gnuOptions = constructGnuOptions(); 	
	
	CommandLine commandLine;
	
	try{
		
		commandLine = cmdLineGnuParser.parse(gnuOptions, args);
		
		if ( commandLine.hasOption(generate_pallete) )
        {
           String metamodel = commandLine.getOptionValue(generate_pallete);
                     
           EcoreEMF nemf = SplitterLibraryFactoryImpl.eINSTANCE.createEcoreEMF();
           nemf.setFileuri(URI.createFileURI(metamodel).toString());
           
           HeuristicStrategy heuristicStrategy = DslHeuristicVisualizationFactoryImpl.eINSTANCE.createHeuristicStrategy();
           GraphicRepresentation graph = Graphic_representationFactoryImpl.eINSTANCE.createGraphicRepresentation();
		   MMGraphic_Representation mmgraph = Graphic_representationFactoryImpl.eINSTANCE.createMMGraphic_Representation();
		   mmgraph.getListRepresentations().add(Graphic_representationFactoryImpl.eINSTANCE.createRepresentationDD());
		   graph.getAllGraphicRepresentation().add(mmgraph);
		   heuristicStrategy.setGraphic_representation(graph);
	       RepreHeurSS repre = DslHeuristicVisualizationFactoryImpl.eINSTANCE.createRepreHeurSS();
		   repre.getHeuristicStrategySettings().add(DslHeuristicVisualizationFactoryImpl.eINSTANCE.createHeuristicStrategySettings());
		   heuristicStrategy.getListRepresentation().add(repre);
		   heuristicStrategy.setNemf(nemf);
		   
           heuristicStrategy.ExecuteHeuristics();
		   
			//PerformRelationNodeElementsToAffixedCompartments
			PerformRelationNodeElementsToAffixedCompartments(heuristicStrategy.getGraphic_representation());
			PerformEReferenceContainer(heuristicStrategy.getGraphic_representation(),nemf, heuristicStrategy);
		   
			// Register the XMI resource factory for the .graphicR extension
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		    Map<String, Object> m = reg.getExtensionToFactoryMap();
		    m.put("graphicR", new XMIResourceFactoryImpl());
			   	      
		    //Create Resource
		    URI fUri = GetURI(nemf);
		    Resource rs = heuristicStrategy.getNemf().getRes().createResource(fUri);	   
		    rs.getContents().add(heuristicStrategy.getGraphic_representation());			
					
			try {
				rs.save(Collections.EMPTY_MAP);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
           System.out.println("Complete Generation of the Palette!" +  nemf.getList_classes().get(0).getName());
        }
		
	}
	catch(ParseException parseException){
		
		System.err.println(
	              "Encountered exception while parsing using GnuParser:\n"
	            + parseException.getMessage() );
		
		}
	
		
	}
	
	private static URI GetURI(EcoreEMF nemf) {
		// TODO Auto-generated method stub
		URI graphicURI = URI.createURI(nemf.getFileuri(),false);
		graphicURI = graphicURI.trimFileExtension().appendFileExtension("graphicR");
    	return graphicURI;		
	}

	public static Options constructGnuOptions()
	{
	      final Options gnuOptions = new Options();
	      gnuOptions.addOption(generate_pallete, generate_pallete_long, true, "Option for Generate the Palette Automatically");	                
	      return gnuOptions;
	}
	
	//Palette Description Link //Combine with elements in other layersss
		private static void PerformRelationNodeElementsToAffixedCompartments(GraphicRepresentation graphicR)
		{
			Iterator<MMGraphic_Representation> graph = graphicR.getAllGraphicRepresentation().iterator();
			while (graph.hasNext()) {
				MMGraphic_Representation mmRepresentation = (MMGraphic_Representation) graph.next();
				Iterator<Representation> itRepresentations = mmRepresentation.getListRepresentations().iterator();
				while (itRepresentations.hasNext()) {
					RepresentationDD representation = (RepresentationDD) itRepresentations.next();
					Iterator<Layer> layers = representation.getLayers().iterator();
					while (layers.hasNext()) {
						Layer lay = (Layer) layers.next();
						Iterator<AllElements> diagElements = lay.getElements().iterator();
						while (diagElements.hasNext()) {
							AllElements diag = (AllElements) diagElements.next();
							if(diag instanceof Node)
							{
								Iterator<AffixedCompartmentElement> itAffixedCompartment = ((Node) diag).getNode_elements().getAffixedCompartmentElements().iterator();
											
								while (itAffixedCompartment.hasNext()) {
									AffixedCompartmentElement affixedCompartment = (AffixedCompartmentElement) itAffixedCompartment.next();
									EReference ref = affixedCompartment.getAnEReference();
									EClass anRefEClass = (EClass) ref.getEType();
									
									EList<AllElements> DiagElements = new BasicEList<AllElements>();
									Iterator<Layer> itlayers = representation.getLayers().iterator();
									while (itlayers.hasNext()) {
										Layer layer = (Layer) itlayers.next();
										DiagElements.addAll(layer.getElements());
									}
									
									Iterator<AllElements> compareDiagElements = DiagElements.iterator();
									while (compareDiagElements.hasNext()) {
										AllElements typediagElement = (AllElements) compareDiagElements.next();
										if(typediagElement instanceof Node)
										{
											
												//Composition Relation-ship
												if(((Node)typediagElement).getAnEClass().equals(anRefEClass))
													affixedCompartment.getNodes().add((Node)typediagElement);
												else{
													//Inheritance Relation-ship
													EList<EClass> allClasses = ((Node)typediagElement).getAnEClass().getEAllSuperTypes();
													int index = allClasses.indexOf(anRefEClass);
													if(index!=-1)
													  affixedCompartment.getNodes().add((Node)typediagElement);
												}									
										}
									}							
								}
								
								Iterator<PaletteDescriptionLink> itPaletteDescrition = ((Node) diag).getNode_elements().getLinkPalette().iterator();
								while (itPaletteDescrition.hasNext()) {
									PaletteDescriptionLink paletteDescription = (PaletteDescriptionLink) itPaletteDescrition.next();
									EReference ref = paletteDescription.getAnEReference();
									EClass anRefEClass = (EClass) ref.getEType();
									
									EList<AllElements> DiagElements = new BasicEList<AllElements>();
									Iterator<Layer> itlayers = representation.getLayers().iterator();
									while (itlayers.hasNext()) {
										Layer layer = (Layer) itlayers.next();
										DiagElements.addAll(layer.getElements());
									}
									
									Iterator<AllElements> compareDiagElements = DiagElements.iterator();
									while (compareDiagElements.hasNext()) {
										DiagramElement typediagElement = (DiagramElement) compareDiagElements.next();
										if(typediagElement instanceof Node)
										{
											//Composition Relation-ship
											if(typediagElement.getAnEClass().equals(anRefEClass))
												paletteDescription.getAnDiagramElement().add((Node)typediagElement);
											else
											{
												//Inheritance Relation-ship
												EList<EClass> allClasses = typediagElement.getAnEClass().getEAllSuperTypes();
												int index = allClasses.indexOf(anRefEClass);
												if(index!=-1)
													paletteDescription.getAnDiagramElement().add((Node)typediagElement);
											}
										}
									}							
								}
							}
						}
					}				
				}
			}
		}	
		
		private static void PerformEReferenceContainer(GraphicRepresentation graph, EcoreEMF nemf, HeuristicStrategy strat) {
			// TODO Auto-generated method stub
			Iterator<MMGraphic_Representation> mm = graph.getAllGraphicRepresentation().iterator();
			while (mm.hasNext()) {
				MMGraphic_Representation mmGraphic_Representation = (MMGraphic_Representation) mm.next();
				Iterator<Representation> itRepresentations = mmGraphic_Representation.getListRepresentations().iterator();
				while (itRepresentations.hasNext()) {
					Representation representation = (Representation) itRepresentations.next();
					if(representation instanceof RepresentationDD)
					{
						RepresentationDD dd = (RepresentationDD)representation;
						//EClass root = dd.getRoot().getAnEClass();
						Iterator<Layer> layers = dd.getLayers().iterator();
						while (layers.hasNext()) {
							Layer layer = (Layer) layers.next();
							Iterator<AllElements> all = layer.getElements().iterator();
							while (all.hasNext()) {
								AllElements element = (AllElements) all.next();
								if(element instanceof Node)
								{
									if(((Node) element).getContainerReference() == null)
									{
										int index = nemf.getList_classes().indexOf(((Node) element).getAnEClass());
										int getParentIndex = strat.getEcoreContainment().GetParent(index);
										if(getParentIndex!=-1)
										{
										EReference ref = GetFeatureNameContainment(nemf.getList_classes().get(getParentIndex)
																					, ((Node) element).getAnEClass());
												if(ref!=null)
													((Node) element).setContainerReference(ref);	
										}
									}
								}
								else if(element instanceof Edge)
								{
									int index = nemf.getList_classes().indexOf(((Edge) element).getAnEClass());
									int getParentIndex = strat.getEcoreContainment().GetParent(index);
									if(getParentIndex!=-1)
									{
									EReference ref = GetFeatureNameContainment(nemf.getList_classes().get(getParentIndex)
																						, ((Edge) element).getAnEClass());
											if(ref!=null)
												((Edge) element).setContainerReference(ref);
									}
								}
							}
						}
					}
				}
				
			}
		}
		
		public static EReference GetFeatureNameContainment(EClass parentEClass, EClass childEClass) {
			// TODO: implement this method
			// Ensure that you remove @generated or mark it @generated NOT
			EList<EReference> listReferences = parentEClass.getEAllContainments();
			int listEReferences = listReferences.size();
			//Direct Containments
			for (int i = 0; i < listReferences.size(); i++) {
				if(listReferences.get(i).getEType().equals(childEClass)==true)
					return listReferences.get(i);
			}
			
			EList<EClass> childEClassEAllSuperTypes = childEClass.getEAllSuperTypes();
			int listAllSuperTypes = childEClassEAllSuperTypes.size();
			//Inheritance Containments
			for (int i = 0; i < listAllSuperTypes; i++) {
				for (int j = 0; j < listEReferences; j++) {
					if(listReferences.get(j).getEType().equals(childEClassEAllSuperTypes.get(i)))
						return listReferences.get(j);
				}
			}
			//Didn't find the containment reference
			return null;
		}	

}
