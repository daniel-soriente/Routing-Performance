package ass1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoutingPerformance {
	private String network;
	private String routing;
	private String topology;
	private String workload;
	private double packetRate;
	private List<Router> routers;
	private List<Edge> edges;
	private long requests;
	private long tPackets;
	private double sPackets;
	private double bPackets;
	private double tHop;
	private double tProp;
	
	public static void main (String[] args) {
		if (args.length == 5 && checkArgs(args)) {
			new RoutingPerformance(args);
		} else {
			System.err.println("Please enter 5 arguments in correct format:");
			System.err.println("[CIRCUIT/PACKET][SHP,SDP,LLP][FILE][FILE][NATURAL NUMBER]");
		}
	}
	
	public RoutingPerformance(String[] a) {
		network = a[0];
		routing = a[1];
		topology = a[2];
		workload = a[3];
		packetRate = Double.parseDouble(a[4]);
		requests = 0;
		tPackets = 0;
		sPackets = 0;
		bPackets = 0;
		tHop = 0;
		tProp = 0;
		
		//create network
		createNetwork();
		
		//create Connections
		processWorkload();
		
		//print performance
		System.out.println("total number of virtual circuit requests: " + requests);
		System.out.println("total number of packets: " + tPackets);
		System.out.println("number of successfully routed packets: " + String.format("%.0f", sPackets));
		System.out.println("percentage of successfully routed packets: " + String.format("%.2f", (double) (sPackets/tPackets) * 100));
		System.out.println("number of blocked packets: " + String.format("%.0f", bPackets));
		System.out.println("percentage of blocked packets: " + String.format("%.2f", (double) (bPackets/tPackets * 100)));
		System.out.println("average number of hops per circuit: " + String.format("%.2f", tHop/requests));
		System.out.println("average cumulative propagation delay per circuit: " + String.format("%.2f", tProp/requests));
	}

	/**
	 * Checks program arguments are correct
	 * @param a
	 * @return
	 */
	private static boolean checkArgs(String[] a) {
		if (!(a[0].equals("CIRCUIT") || a[0].equals("PACKET"))) {
			return false;
		}
		
		if (!(a[1].equals("SHP") || a[1].equals("SDP") || a[1].equals("LLP"))) {
			return false;
		}
		
		if (!(new File(a[2]).isFile())) {
			return false;
		}
		
		if (!(new File(a[3]).isFile())) {
			return false;
		}
		
		if (!(Integer.parseInt(a[4]) > 0)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates the routers and connections between routers
	 * Each router has a list of edges
	 * This class keeps track of all routers and edges
	 */
	public void createNetwork() {
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(topology));
			routers = new ArrayList<Router>();
			edges = new ArrayList<Edge>();
			//Read each line of topology file and create an edge
		    for(String line; (line = br.readLine()) != null; ) {
		    	String[] l = line.split(" ");
		    	Router r1;
		    	Router r2;
		    	
		    	if (getRouter(l[0]) != null) {
		    		r1 = getRouter(l[0]);
		    	} else {
		    		r1 = new Router(l[0]);
		    		routers.add(r1);
		    	}
		    	
		    	if (getRouter(l[1]) != null) {
		    		r2 = getRouter(l[1]);
		    	} else {
		    		r2 = new Router(l[1]);
		    		routers.add(r2);
		    	}
		    	
		    	Edge edge = new Edge(r1, r2, Integer.parseInt(l[2]), Integer.parseInt(l[3]));
		    	edges.add(edge);
		    	r1.addNeighbour(edge);
		    	r2.addNeighbour(edge);
		    }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void processWorkload() {
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(workload));
			//Read each line of topology file and create an edge
		    for(String line; (line = br.readLine()) != null; ) {
		    	String[] l = line.split(" ");
		    	Router r1 = getRouter(l[1]);
	    		Router r2 = getRouter(l[2]);
	    		double startT = Double.parseDouble(l[0]);
	    		double duration = Double.parseDouble(l[3]);
	    		int packets = (int) Math.ceil(Double.parseDouble(l[3]) * packetRate);
	    		
	    		if (network.equals("CIRCUIT")) {
	    			//Process request once, and apply for all packets
	    			processRequest(r1, r2, startT, startT + duration, packets);
	    		} else {
	    			double i = 1/(double) packetRate;
	    			//For every packet, process request with a new start and end time
	    			for (int j = 0; j < packets; j++) {
	    				if (j+1 != packets) {
	    					processRequest(r1, r2, startT + (i * j), startT + (i * (j+1)), 1);
	    				} else {
	    					processRequest(r1, r2, startT + (i * j), startT + duration, 1);
	    				}
	    			}
	    		}
	    		
	    		tPackets += packets;
	    		
	    		//Free resources
	    		for (Edge e : edges) {
	    			e.connectionTimeout(startT);
	    		}
		    }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Given s, we return the router where the strings match
	 * @param s
	 * @return
	 */
	public Router getRouter (String s) {
		for (Router r: routers) {
			if (r.getRouterName().equals(s)) {
				return r;
			}
		}
		return null; //should never happen
	}
	
	/**
	 * Process the request with given start and end time for n packets
	 * Calls routing algorithm. If path is empty, then packet is blocked
	 * else it was successful
	 * Each routing algorithm will find a path and check if the connection can be made
	 * @param r1
	 * @param r2
	 * @param s
	 * @param e
	 * @param packets
	 */
	public void processRequest(Router r1, Router r2, Double s, Double e, int packets) {
		List<Edge> path = new ArrayList<Edge>();
		
		if (routing.equals("SHP")) {
			path = shp(r1, r2, s, e);
		} else if (routing.equals("SDP")) {
			path = sdp(r1, r2, s, e);
		} else {
			path = llp(r1, r2, s, e);
		}
		
		if (!path.isEmpty() && canMakeConnection(path, s, e)) {
			sPackets += packets;
			tHop += path.size();
		} else {
			bPackets += packets;
		}
		
		requests++;
		resetNetwork();
	}
	
	/**
	 * Need to set the values at each router back to
	 * -1 and reset its path
	 */
	public void resetNetwork() {
		for (Router r: routers) {
			r.setDistance(-1);
			r.setPath(null);
		}
	}
	
	/**
	 * Shortest Hop Path
	 * @param start
	 * @param end
	 * @param e2 
	 * @param st 
	 * @return
	 */
	public List<Edge> shp(Router start, Router end, Double st, Double et) {
		QueueInterface<Router> toVisit = new QueueInterface<Router>();
		start.setDistance(0);
		toVisit.add(start);
		
		while (!toVisit.isEmpty()) {
			Router current = toVisit.remove();
			
			if (current.equals(end)) {
				return getPath(start,end);
			}
			
			List<Edge> neighbours = current.getNeighbours();
			for (Edge edge: neighbours) {
				Router n = edge.getOtherRouter(current);
				if (n.getDistance() == -1 || current.getDistance() + 1 < n.getDistance()) {
					n.setDistance(current.getDistance() + 1);
					n.setPath(edge);
					toVisit.add(n);
				}				
			}
		}
		
		return new ArrayList<Edge>();
	}
	
	/**
	 * Shortest Delay Path
	 * @param start
	 * @param end
	 * @param e2 
	 * @param s 
	 * @return
	 */
	public List<Edge> sdp(Router start, Router end, Double st, Double et) {
		QueueInterface<Router> toVisit = new QueueInterface<Router>();
		start.setDistance(0);
		toVisit.add(start);
		
		while (!toVisit.isEmpty()) {
			Router current = toVisit.remove();
			
			if (current.equals(end)) {
				return getPath(start,end);
			}
			
			List<Edge> neighbours = current.getNeighbours();
			for (Edge edge: neighbours) {
				Router n = edge.getOtherRouter(current);
				if (n.getDistance() == -1 || current.getDistance() + edge.getProp() < n.getDistance()) {
					n.setDistance(current.getDistance() + edge.getProp());
					n.setPath(edge);
					toVisit.add(n);
				}				
			}
		}
		
		return new ArrayList<Edge>();
	}
	
	/**
	 * Least Loaded Path
	 * @param start
	 * @param end
	 * @param st
	 * @param et
	 * @return
	 */
	public List<Edge> llp(Router start, Router end, Double st, Double et) {
		QueueInterface<Router> toVisit = new QueueInterface<Router>();
		start.setDistance(0);
		toVisit.add(start);
		
		while (!toVisit.isEmpty()) {
			Router current = toVisit.remove();
			
			if (current.equals(end)) {
				return getPath(start,end);
			}
			
			List<Edge> neighbours = current.getNeighbours();
			for (Edge edge: neighbours) {
				Router n = edge.getOtherRouter(current);
				if (n.getDistance() == -1 || current.getDistance() + edge.getLoad(st) < n.getDistance()) {
					n.setDistance(current.getDistance() + edge.getLoad(st));
					n.setPath(edge);
					toVisit.add(n);
				}				
			}
		}
		
		return new ArrayList<Edge>();
	}
	
	/**
	 * Every algorithm should call this function
	 * Traverses the path backwards from the end to start
	 * and add the edges onto the path
	 * @param start
	 * @param end
	 * @return
	 */
	public List<Edge> getPath(Router start, Router end) {
		Router r = end;
		List<Edge> path = new ArrayList<Edge>();

		while (r != null && !r.equals(start)) {
			path.add(r.getPath());
			r = r.getPath().getOtherRouter(r);
		}
		return path;
	}
	
	/**
	 * Checks each edge, if we can make a connection at the given start time
	 * then we allocate the resources for the duration of that connection
	 * @param path
	 * @param st
	 * @param et
	 * @return
	 */
	public boolean canMakeConnection(List<Edge> path, Double st, Double et) {
		double cProp = 0;
		for (Edge edge : path) {
			if (!edge.fullCapacity(st)) {
				edge.setConnection(st, et);
				cProp += edge.getProp();
			} else {
				return false;
			}
		}
		tProp += cProp;
		return true;
	}
}
