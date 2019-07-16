package it.polito.dp2.NFV.sol3.service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeFactory;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.dp2.NFV.ConnectionPerformanceReader;
import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.NfvReaderFactory;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;

public class NfvDB {
	private static Map<String, NffgType> nffgDB = new ConcurrentHashMap<>();
	private static Map<String, HostType> hostDB = new ConcurrentHashMap<>();
	private static Map<String, VnftType> vnftDB = new ConcurrentHashMap<>();
	private static Map<String, Map<String,String>> neo4jDB = new ConcurrentHashMap<>();
	private static Map<String, String> hostIdDB = new ConcurrentHashMap<>();
	private static Map<String, String> revHostIdDB = new ConcurrentHashMap<>();
	private static Map<String, String> nodeIdDB = new ConcurrentHashMap<>();
	private static Map<String, PerformanceType> perfDB = new ConcurrentHashMap<>();
	
	private static Logger logger= Logger.getLogger(NfvDB.class.getName());
	
	private static long lastNffg = 1;
	private static long lastNode = 0;
	private static long lastLink = 0;
	
	private static final String nffg_init_name ="Nffg0";
	
	private static NfvDB myObj;
	
	private NfvDB() throws Neo4jServiceException, NfvReaderException{
		NfvReaderFactory factory = NfvReaderFactory.newInstance();
		NfvReader nfv_reader;
		try {
			nfv_reader = factory.newNfvReader();
			Set<VNFTypeReader> vnfts = nfv_reader.getVNFCatalog();
			Set<HostReader> hosts = nfv_reader.getHosts();
			NffgReader nffg0 = nfv_reader.getNffg(nffg_init_name);
				
			//instantiate a new neo4jservice in order to save info on the neo4j DB
			Neo4jService neo4jservice = new Neo4jService();
			
			//store the vnfts
			for(VNFTypeReader vnft: vnfts){
				//convert the VNFTreader in vnfttype
				VnftType v = new VnftType();
				v.setDiskStorage(vnft.getRequiredStorage());
				v.setMemory(vnft.getRequiredMemory());
				v.setFuncType(FuncTypeType.fromValue(vnft.getFunctionalType().toString()));
				v.setName(vnft.getName());
				//save the vnfts
				vnftDB.put(vnft.getName(), v);
			}
			
			//add the hosts saved into the neo4jDB to the local DB
			Set<HostType> hosts_type = new HashSet<HostType>();
			for(HostReader host: hosts){
					//convert hostReader in hostType WITHOUT SETTING THE NODES ALLOCATED ON THE HOST
					HostType h = new HostType();
					h.setDiskStorage(host.getAvailableStorage());
					h.setMaxVnf(BigInteger.valueOf(host.getMaxVNFs()));
					h.setVnf(BigInteger.ZERO);
					h.setMemory(host.getAvailableMemory());
					h.setName(host.getName());
					h.setLeftDiskStorage(h.getDiskStorage());
					h.setLeftMemory(h.getMemory());

					hosts_type.add(h);		
			}
			//post the hosts in the remote DB neo4j 
			Map<String, String> hostId = neo4jservice.loadHosts(hosts_type);
			
			for(HostType host_type: hosts_type){
				if(hostId.get(host_type.getName())!=null){
					hostDB.put(host_type.getName(), host_type);
				}
			}
				
			//connection performances
			//for each couple of hosts, save in the DB the connection performances
			for(HostReader h1: hosts){
				for(HostReader h2: hosts){
					if(hostIdDB.get(h1.getName())!=null && hostIdDB.get(h2.getName())!=null){
						ConnectionPerformanceReader cp_r = nfv_reader.getConnectionPerformance(h1, h2);
						PerformanceType cp_t = new PerformanceType();
						cp_t.setAvgLatency(cp_r.getLatency());
						cp_t.setAvgThrough(cp_r.getThroughput());
						cp_t.setHost1(h1.getName());
						cp_t.setHost2(h2.getName());
						perfDB.put(h1.getName()+h2.getName(), cp_t);
					}
				}
			}
						
			//convert the NffgReader into an NffgType to be saved in the local DB
			NffgType nffg = new NffgType();
			nffg.setName(nffg0.getName());
			nffg.setDeployTime(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)nffg0.getDeployTime()));
			List<NodeType> nodeList = nffg.getNodeType();
			for(NodeReader nr: nffg0.getNodes()){
				NodeType nt = new NodeType();
				nt.setHost(nr.getHost().getName());
				nt.setName(nr.getName());
				nt.setNffgName(nr.getNffg().getName());
				nt.setVNFT(nr.getFuncType().getName());
				List<LinkType> nodeLinks = nt.getLinkType();
				for(LinkReader lr: nr.getLinks()){
					LinkType lt = new LinkType();
					lt.setName(lr.getName());
					lt.setDestNode(lr.getDestinationNode().getName());
					lt.setMaxLatency(lr.getLatency());
					lt.setMinThrough(lr.getThroughput());
					lt.setName(lr.getName());
					lt.setSourceNode(lr.getSourceNode().getName());
					nodeLinks.add(lt);
				}
				nodeList.add(nt);	
			}
			
			Map<String,String> nodeIdmapping = neo4jservice.loadNffg(nffg);
			nffgDB.put(nffg.getName(), nffg);
			//update host nodes 
			for(NodeType n: nffg.getNodeType()){
				VnftType v = vnftDB.get(n.getVNFT());
				HostType h = hostDB.get(n.getHost());
				List<String> allocated_nodes = h.getNode();
				allocated_nodes.add(n.getName());
				h.setLeftDiskStorage(h.getLeftDiskStorage()-v.getDiskStorage());
				h.setLeftMemory(h.getLeftMemory()-v.getMemory());
				h.setVnf(h.getVnf().add(BigInteger.valueOf(1)));
				hostDB.put(h.getName(), h);
			}
				
		}catch(Neo4jServiceException nse){
			logger.log(Level.SEVERE, "failure in the neo4j service");
			throw new Neo4jServiceException(nse.getMessage());
		}catch(NfvReaderException nfvre) {
			logger.log(Level.SEVERE, "failure in the NfvReader", nfvre);
			throw new NfvReaderException(nfvre.getMessage());
		}catch (Exception e) {
			logger.log(Level.SEVERE, "failure in init phase", e);
			e.printStackTrace();
		}
	}
	
	//synchronized to be thread-safe 
	public static synchronized long getNextNffg(){
		return ++lastNffg;
	}
	public static synchronized long getNextNode(){
		return ++lastNode;
	}
	public static synchronized long getNextLink() {
		return ++lastLink;
	}
	
	//retrieve the DBs - atomic operations, no need to be synchronized
	public static Map<String, NffgType> getNffgDB(){
		return nffgDB;
	}
	public static Map<String, PerformanceType> getPerfDB(){
		return perfDB;
	}
	
	public static Map<String, HostType> getHostDB(){
		return hostDB;
	}
	
	public static Map<String, VnftType> getVnftDB(){
		return vnftDB;
	}
	
	public static Map<String, Map<String,String>> getNeo4jDB(){
		return neo4jDB;
	}
	public static Map<String, String> getHostIdDB(){
		return hostIdDB;
	}
	public static Map<String, String> getRevHostIdDB(){
		return revHostIdDB;
	}
	public static Map<String, String> getNodeIdDB(){
		return nodeIdDB;
	}
	
	
	//retrieve the obj associated to the DB for locking
	public static Object getNffgSynch(){
		return nffgDB;
	}
	
	public static Object getHostSynch(){
		return hostDB;
	}
	
	public static Object getVnftSynch(){
		return vnftDB;
	}


	public static NfvDB getInstance() throws Neo4jServiceException, NfvReaderException {
		
		if(myObj==null){
			try{
				myObj = new NfvDB();
			}catch(Neo4jServiceException nse){
				logger.log(Level.SEVERE, "failure in the neo4j service", nse);
				throw new Neo4jServiceException(nse.getMessage());
			}catch(NfvReaderException nfvre) {
				logger.log(Level.SEVERE, "failure in the NfvReader", nfvre);
				throw new NfvReaderException(nfvre.getMessage());
			}
		}
		return myObj;
	}
}
