package it.polito.tdp.artsmia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.artsmia.model.Adiacenza;
import it.polito.tdp.artsmia.model.ArtObject;

public class ArtsmiaDAO {

	public void listObjects(Map<Integer, ArtObject> idMap) {
		
		String sql = "SELECT * from objects";
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {
				if(!idMap.containsKey(res.getInt("object_id"))) { // Se questo metodo dovesse essere richiamato una seconda volta non entro più qui dentro
																  // perchè la mappa è già piena
					ArtObject artObj = new ArtObject(res.getInt("object_id"), res.getString("classification"), res.getString("continent"), 
							res.getString("country"), res.getInt("curator_approved"), res.getString("dated"), res.getString("department"), 
							res.getString("medium"), res.getString("nationality"), res.getString("object_name"), res.getInt("restricted"), 
							res.getString("rights_type"), res.getString("role"), res.getString("room"), res.getString("style"), res.getString("title"));
					
					idMap.put(artObj.getId(), artObj); // Creo l'identity map
				}
			}
			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* CON QUESTO METODO NON GIUNGE AL TERMINE
	public int getPeso(ArtObject a1, ArtObject a2) {
		
		String sql = "SELECT COUNT(*) AS peso "
				+ "FROM exhibition_objects e1, exhibition_objects e2 "
				+ "WHERE e1.exhibition_id = e2.exhibition_id "
				+ "AND e1.object_id = ? "
				+ "AND e2.object_id = ?";
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, a1.getId());
			st.setInt(2, a2.getId());
			ResultSet res = st.executeQuery();
			int peso = 0;
			if(res.next()) {
				// Essendo un conteggo ho solo un risultato, se c'è un risultato lo vado a prendere
				peso = res.getInt("peso");
			}
			
			conn.close();
			return peso;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	*/
	
	public List<Adiacenza> getAdiacenze(Map<Integer, ArtObject> idMap) {
		// Uguale alla query del lab come concetto perchè è un grafo non pesato.
		// La query mi restituisce tutti gli oggetti che sono stati nella stessa esibizione e quante volte ci sono stati nella stessa esibizione che è il
		// peso, ma prendendoli solo in un verso perchè facendo una query con se stessa la tabella mi genera gli stessi codici ma in verso opposto. 
		String sql = "SELECT e1.object_id AS e1, e2.object_id AS e2, COUNT(*) AS peso "
				+ "FROM exhibition_objects e1, exhibition_objects e2 "
				+ "WHERE e1.exhibition_id = e2.exhibition_id "
				+ "AND e2.object_id > e1.object_id "
				+ "GROUP BY e1.object_id, e2.object_id";
		
		Connection conn = DBConnect.getConnection();
		List<Adiacenza> resultAdiacenzas = new ArrayList<Adiacenza>();
		
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while(res.next()) {
				// Essendo un conteggio ho solo un risultato, se c'è un risultato lo vado a prendere. Uso la mappa perchè nella classe Adiacenza ho due 
				// oggetti di tipo ArtsObject, quindi a partire dal codice mi ritorno l'oggetto
				resultAdiacenzas.add(new Adiacenza(idMap.get(res.getInt("e1")), idMap.get(res.getInt("e2")), res.getInt("peso")));
			}
			
			conn.close();
			return resultAdiacenzas;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
