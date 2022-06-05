package it.polito.tdp.artsmia.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model {
	private Graph<ArtObject, DefaultWeightedEdge> grafo;
	private ArtsmiaDAO dao;
	// Creiamo una identity map che permette di creare una corrispondenza tra l'identificativo dell'oggetto e l'oggetto stesso
	private Map<Integer, ArtObject> idMap;
	
	public Model() {
		dao = new ArtsmiaDAO(); // Ce ne serve solo uno e lo costruiamo quindi solo una volta
		idMap = new HashMap<Integer, ArtObject>(); // La costruisco solo una volta
	}
	
	public void creaGrafo() {
		// In questo modo ogni volta il grafo viene distrutto e ricreato così siamo sicuri che sia pulito
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	
		// Ricevo dall'esterno una mappa da riempire
		dao.listObjects(idMap);
		
		// Aggiungo i vertici e in questo caso devo aggiungere tutti i vertici
		Graphs.addAllVertices(grafo, idMap.values()); // idMap.values() mi prende tutti i valori, cioè gli ArtObject, e li mette come vertici del grafo
		
		// Aggiungo gli archi
		/* CON QUESTO METODO NON GIUNGO AL TERMINE
		for (ArtObject a1 : grafo.vertexSet()) {
			for (ArtObject a2 : grafo.vertexSet()) {
				// Per ogni coppia di vertici chiedo al database se sono collegati
				if(!a1.equals(a2) && grafo.containsEdge(a1, a2)) {
					// Essendo un grafo non orientato devo controllare che non sia già presente l'arco che va da a1 ad a2 che è lo stesso che va da a2 ad a1
					int peso = dao.getPeso(a1, a2);
					if(peso > 0) {
						Graphs.addEdgeWithVertices(grafo, a1, a2, peso);
					}
				}
			}
		
			System.out.println("Grafo creato");
			System.out.println("Numero vertici: " + grafo.vertexSet().size());
			System.out.println("Numero archi: " + grafo.edgeSet().size());
		}
		
		*/
		
		// APPROCCIO 2 --> efficiente in ogni caso
		for (Adiacenza adiacenza : dao.getAdiacenze(idMap)) {
			// Aggiungo l'arco con il rispettivo peso tra due vertici sulla base del risultato della query
			Graphs.addEdgeWithVertices(grafo, adiacenza.getA1(), adiacenza.getA2(), adiacenza.getPeso());			
		}
	}
	
	public int nVertici() {
		return grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return grafo.edgeSet().size();
	}

	public ArtObject getObject(int objectId) {
		return idMap.get(objectId);
	}

	public int getComponenteConnessa(ArtObject vertice) {
		Set<ArtObject> visitati = new HashSet<>(); // Sono i vertici visitati
		DepthFirstIterator<ArtObject, DefaultWeightedEdge> it = new DepthFirstIterator<ArtObject, DefaultWeightedEdge>(grafo, vertice);
		
		while (it.hasNext()) {
			visitati.add(it.next());
		}
		
		return visitati.size();
	}
}
