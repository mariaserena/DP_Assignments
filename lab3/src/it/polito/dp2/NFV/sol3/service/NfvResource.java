package it.polito.dp2.NFV.sol3.service;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.NotFoundException;

import java.net.URI;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;
import java.util.logging.Level;
import java.util.logging.Logger;



@Path("/resources") //resource class hosted at the URI /resources
@Api(value="resources", description="a collection of service resources")
public class NfvResource {
	//create an instance of the object that can execute operations
	private NfvService service;
	private static Logger logger= Logger.getLogger(NfvResource.class.getName());

	public NfvResource(){
		//instantiate a new NfvService
		try {
			service = new NfvService();
		} catch(MyServerErrorException e){
			logger.log(Level.SEVERE, "server error occurred", e);
		} catch (Neo4jServiceException e) {
			logger.log(Level.SEVERE, "failure in the init phase. not able to reach the neo4j service", e);
			e.printStackTrace();
		}
	}

	
	
	@POST
	@Path("/nffgs")
	@ApiOperation(value = "Add a single NFFG to the NFFG list", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 403, message = "Forbidden - unable to alloc the nodes of the nffg"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 503, message = "Unavailable neo4j server")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response postNffg(NffgType nffg, @Context UriInfo uriInfo){
		NffgType deplNffg;
		try{
			deplNffg=service.postNffg(nffg);
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			URI u = builder.path(deplNffg.getName()).build();
			return Response.created(u).entity(deplNffg).build();
		}catch (MyForbiddenException nte){
			throw new ForbiddenException(); 
		}catch (MyBadRequestException bre ){
				throw new BadRequestException(); 
		}catch(Neo4jServiceException n4je){
			throw new ServiceUnavailableException();
		}
	}
	
	@GET
	@Path("/nffgs")
	@ApiOperation(value = "Get the list of NFFGs", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML,MediaType.TEXT_XML })
	public Response getNffgs() {
		try {
			NffgsType nffgs = service.getNffgs();
			return Response.ok(nffgs).build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Server error occurred while retrieving the nffgs", e);
			return Response.serverError().build();
		}
	}
	
	
	@GET
	@Path("/nffgs/{nffg_id}")
	@ApiOperation(value = "Get a singleNFFGs", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getNffg(@PathParam("nffg_id") String nffg_id) {
		NffgType nffg;
		try{
			nffg=service.getNffg(nffg_id);
			return Response.ok(nffg).build();
		}catch (MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(Exception e){
			logger.log(Level.SEVERE, "Server error occurred while retrieving a nffg", e);
			return Response.serverError().build();
		}
	}
	
	
	@GET
	@Path("/nffgs/{nffg_id}/nodes")
	@ApiOperation(value = "get the list of nodes belonging to a particular NFFG", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getNodes(@PathParam("nffg_id") String nffg_id){
		NodesType nodes;
		try{
			nodes = service.getNodes(nffg_id);
			return Response.ok(nodes).build();
		}catch (MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(Exception e){
			logger.log(Level.SEVERE, "server error occured while retrieving the nodes of an nffg", e);
			return Response.serverError().build();
		}
	}
	
	
	@POST
	@Path("/nffgs/{nffg_id}/nodes")
	@ApiOperation(value = "Add a single node to a node list of a NFFG", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 403, message = "Forbidden - unable to alloc the node on a host"),
			@ApiResponse(code = 404, message = "Not found. Invalid nffg id"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 503, message = "Unavailable neo4j server")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response postNode(@PathParam("nffg_id") String nffg_id,NodeType node, @Context UriInfo uriInfo){
		NodeType postednode;
		try{
			postednode = service.postNode(node, nffg_id);
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			URI u = builder.path(postednode.getName()).build();
			return Response.created(u).entity(postednode).build();
		}catch(MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(MyForbiddenException fbe){
			throw new ForbiddenException();
		}catch(MyBadRequestException bre){
			throw new BadRequestException();	
		}catch(Neo4jServiceException njs){
			throw new ServiceUnavailableException();
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal error occurred while trying to upload a node in a  already deployed nffg",e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/nffgs/{nffg_id}/nodes/{node_id}")
	@ApiOperation(value = "get a single node belonging to a particular NFFG", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getNode(@PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id){
		NodeType node;
		try{
			node = service.getNode(nffg_id, node_id);
			return Response.ok(node).build();
		}catch (MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error while retrieving a single node from an nffg", e);
			return Response.serverError().build();
		}
	}
	
	
	@GET
	@Path("/nffgs/{nffg_id}/nodes/{node_id}/reachable_hosts")
	@ApiOperation(value = "get the set of hosts reachable froma node", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getReachableHosts(@PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id){
		HostsType reachableHosts;
		try{
			reachableHosts = service.getReachableHosts(nffg_id, node_id);
			return Response.ok(reachableHosts).build();
		}catch(MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(Neo4jServiceException njs){
			throw new ServiceUnavailableException();
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal error occurred while trying to retrieve reachable hosts",e);
			return Response.serverError().build();
		}
		
	}

	
	@GET
	@Path("/nffgs/{nffg_id}/nodes/{node_id}/links")
	@ApiOperation(value = "get the set of links of a node", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getLinks(@PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id){
		LinksType links;
		try{
			links = service.getLinks(nffg_id, node_id);
			return Response.ok(links).build();
		}catch(MyNotFoundException nfe){
			throw new NotFoundException();
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error occurred while retrieving the links of a node in a deployed nffg", e);
			return Response.serverError().build();
		}	
	}

	@POST
	@Path("/nffgs/{nffg_id}/nodes/{node_id}/links")
	@ApiOperation(value = "Add a single link to a node", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 404, message = "Not found - invalid nffg_id or node_id"),
			@ApiResponse(code = 403, message = "Fobidden - already existing link, cannot be overwritten"),
			@ApiResponse(code = 503, message = "Unavailable neo4j server"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response postLink(@QueryParam("overwrite") boolean overwrite, @PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id, LinkType link, @Context UriInfo uriInfo){
		LinkType postedlink;
		try{
			postedlink = service.postLink(link, nffg_id, node_id, overwrite);
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			URI u = builder.path(postedlink.getName()).build();
			return Response.created(u).entity(postedlink).build();
		}catch(MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(MyForbiddenException fbe){
			throw new ForbiddenException();
		}catch(MyBadRequestException bre){
			throw new BadRequestException();
		}catch(Neo4jServiceException njs){
			throw new ServiceUnavailableException();
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error occurred while retrieving the links of a node in a deployed nffg", e);
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/hosts")
	@ApiOperation(value = "get the set of hosts in the IN", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getHosts(){
		HostsType host_list;
		try{
			host_list = service.getHosts();
			return Response.ok(host_list).build();
			
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error occurred while retrieving the list of hosts", e);
			return null;
		}	
	}
	
	@GET
	@Path("/hosts/{host_id}")
	@ApiOperation(value = "get a single host", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getHost(@PathParam("host_id") String host_id){
		HostType host;
		try{
			host = service.getHost(host_id);
			return Response.ok(host).build();
		}catch (MyNotFoundException nte){
			throw new NotFoundException(); 
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error occurred while retrieving a hosts", e);
			return Response.serverError().build();
		}	
		
	}

	@GET
	@Path("/performances")
	@ApiOperation(value = "get the a performance", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not Found"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getPerformance(){
		PerformancesType perf;
		try{
			perf = service.getPerformance();
			return Response.ok(perf).build();
		}catch(MyNotFoundException nfe){
			throw new NotFoundException();
		}catch(Exception e){
			logger.log(Level.SEVERE, "internal server error occurred while retrieving a performance connection", e);
			return Response.serverError().build();
		}
		
	}
	
	@GET
	@Path("/vnfts")
	@ApiOperation(value = "get the set of vnfts", notes = "xml format")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "Internal Server Error")})
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Response getVnfts(){
		VnftsType vnfts;
		try{
			vnfts = service.getVnfts();
			return Response.ok(vnfts).build(); 
		}catch(Exception e){
			logger.log(Level.SEVERE, "error while retrieving the list of vnfts", e);
			return Response.serverError().build();
		}
		
	}
	
	//not implemented methods
	@DELETE
	@Path("/nffgs/{nffg_id}")
	@ApiOperation(value="delete a nffg",notes="xml format")
	@ApiResponses(value={
			@ApiResponse(code= 501, message="Not Implemented")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response delNffg(@PathParam("nffg_id") String nffg_id ){
		return Response.status(501).build();
	}
	
	@DELETE
	@Path("/nffgs/{nffg_id}/nodes/{node_id}")
	@ApiOperation(value="delete a node",notes="xml format")
	@ApiResponses(value={
			@ApiResponse(code= 501, message="Not Implemented")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response delNode(@PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id ){
		return Response.status(501).build();
	}
	
	@DELETE
	@Path("/nffgs/{nffg_id}/nodes/{node_id}/links/{link_id}")
	@ApiOperation(value="delete a node",notes="xml format")
	@ApiResponses(value={
			@ApiResponse(code= 501, message="Not Implemented")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response delLink(@PathParam("nffg_id") String nffg_id, @PathParam("node_id") String node_id, @PathParam("link_id") String link_id ){
		return Response.status(501).build();
	}
	
}
