package ass1;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold all information regarding a router
 * Router Name
 * Distance from the start router - used in search algorithm
 * Path / stores the previous router on path - used in search algorithm
 * A list of neighbours which are edges
 */
public class Router {
	private String router;
	private double distance;
	private Edge path;
	private List<Edge> neighbours;
	
	public Router (String r) {
		router = r;
		distance = -1;
		neighbours = new ArrayList<Edge>();
	}
	
	public String getRouterName() {
		return router;
	}
	
	public void setDistance(double n) {
		distance = n;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setPath(Edge r) {
		path = r;
	}
	
	public Edge getPath() {
		return path;
	}
	
	public void addNeighbour(Edge e) {
		if (e.isEdge(this)) {
			neighbours.add(e);
		}
	}
	
	public List<Edge> getNeighbours() {
		return neighbours;
	}
	
	/**
	 * Returns connecting edge of this router and of r
	 * @param r
	 * @return
	 */
	public Edge getConnectedEdge(Router r) {
		for (Edge e: neighbours) {
			if (e.isConnected(this, r)) {
				return e;
			}
		}
		return null; //should never happen
	}
}
