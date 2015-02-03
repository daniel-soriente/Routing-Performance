package ass1;

import java.util.ArrayList;
import java.util.List;

/**
 * Connects two routers and holds link information
 * Connected Routers, r1, r2
 * Prop = propagation delay
 * Cap = Link Capacity
 * currentConnections = a list of connections established between the 2 routers
 */
public class Edge {
	private Router r1;
	private Router r2;
	private int prop;
	private int cap;
	private List<Double[]> currentConnections;
	
	public Edge(Router a, Router b, int p, int c) {
		r1 = a;
		r2 = b;
		prop = p;
		cap = c;
		currentConnections = new ArrayList<Double[]>();
	}
	
	public void printEdge() {
		System.out.println("Nodes:" + r1.getRouterName() + " " + r2.getRouterName() + 
				           " Prop:" + prop + " Cap:" + cap);
	}
	
	/**
	 * Returns if this edge is the connecting edge
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean isConnected(Router a, Router b) {
		return isEdge(a) && isEdge(b);
	}
	
	/**
	 * Checks if r is a connected router on this edge
	 * @param a
	 * @return
	 */
	public boolean isEdge(Router a) {
		return a.equals(r1) || a.equals(r2);
	}
	
	/**
	 * Returns the other connected router
	 * @param r
	 * @return
	 */
	public Router getOtherRouter(Router r) {
		if (r1.equals(r)) {
			return r2;
		}
		if (r2.equals(r)) {
			return r1;
		}
		return null;
	}
	
	public int getProp() {
		return prop;
	}
	
	public int getCap() {
		return cap;
	}
	
	public void setConnection(double s, double e) {
		Double[] d = new Double[2];
		d[0] = s;
		d[1] = e;
		currentConnections.add(d);
	}
	
	public Double[] getConnection(double s, double e) {
		for (Double[] d: currentConnections) {
			if (d[0] == s && d[1] == e) {
				return d;
			}
		}
		return new Double[2];
	}
	
	/**
	 * returns the number of connections
	 * @return
	 */
	public int numConnections(double t) {
		int count = 0;
		for (Double[] d: currentConnections) {
			if (d[0] <= t && d[1] >= t) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * frees resources for time t
	 * if the end time of a connection is less than t
	 * that means connection has ended
	 * @param t
	 */
	public void connectionTimeout (double t) {
		ArrayList<Double[]> delete = new ArrayList<Double[]>();
		//Add all connections to be deleted in delete
		for (Double[] d: currentConnections) {
			//If end time of connection is < the new start time
			//then it will not affect the new connection
			if (d[1] < t) {
				delete.add(d);
			}
		}
		
		currentConnections.removeAll(delete);
	}
	
	/**
	 * returns if the link is at full capacity
	 * @return
	 */
	public boolean fullCapacity(double t) {
		return (getCap() == numConnections(t));
	}
	
	/**
	 * Calculates current load on edge for given time
	 * @param s
	 * @return
	 */
	public double getLoad(double s) {
		return (double) numConnections(s)/getCap();
	}
}
