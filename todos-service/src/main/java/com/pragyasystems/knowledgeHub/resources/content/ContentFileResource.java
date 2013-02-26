/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.foobar.todos.db.EntityRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.content.LearningContent.ContentType;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.auth.Auth;


/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
@Component
@Path("/file")
public class ContentFileResource {

	private static final Logger LOG = LoggerFactory.getLogger(ContentFileResource.class);
	private GridFsTemplate gridFSTemplate;
	private EntityRepository contentRepository;
	private SpaceRepository spaceRepository;
	@Context ServletContext context;
	// This resource should also use base but need some design first.
//	@Autowired protected AuthUtil authZ;

	/**
	 * @param gridFSTemplate
	 */
	@Autowired
	public ContentFileResource(GridFsTemplate gridFSTemplate, EntityRepository contentRepository,
			SpaceRepository spaceRepository) {
		super();
		this.gridFSTemplate = gridFSTemplate;
		this.contentRepository = contentRepository;
		this.spaceRepository = spaceRepository;
	}

	
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		
		GridFSFile file = storeFile(uploadedInputStream, fileDetail, null);
		return Response
				.created(UriBuilder.fromPath("/{uuid}").build(file.getId()))
				.build();
	}

	@POST
	@Path("/learningcontent")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadContent(//@Auth User user,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("learningSpaceId") String learningSpaceId,
			@FormDataParam("contentName") String contentName,
			@FormDataParam("fileName") String fileName) {
		
//		LearningSpace parentSpace = spaceRepository.findOne(learningSpaceId);
//		authZ.assertCanWrite(user, parentSpace);
		GridFSFile file = storeFile(uploadedInputStream, fileDetail, fileName);
		createLearningContent(file, learningSpaceId, contentName, fileName);
		
		return Response
				.created(UriBuilder.fromPath("/{uuid}").build(file.getId()))
				.build();
	}
	
	/*We duplicate this function because ajax file upload has some issue
	in return type other than string because response
	is coming inside headers and was unable to fetch value from header*/
	@POST
	@Path("/logoupload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String uploadToReturnUuidAsString(@Auth User user,
			@FormDataParam("logoUpload") InputStream uploadedInputStream,
			@FormDataParam("logoUpload") FormDataContentDisposition fileDetail, 
			@FormDataParam("learningSpaceId") String learningSpaceId,
			@FormDataParam("contentName") String contentName,
			@FormDataParam("fileName") String fileName) {		

//		LearningSpace parentSpace = spaceRepository.findOne(learningSpaceId);
//		authZ.assertCanWrite(user, parentSpace);
		GridFSFile file = storeFile(uploadedInputStream, fileDetail, fileName);		
		// Why we create Learning Content for the Logo ??????
		createLearningContent(file, learningSpaceId, contentName, fileName);
		
		return new StringBuilder().append("returnId: "+file.getId()).toString();
				
	}

	@GET
	@Path("/{uuid}")
	public Response downloadFile(@PathParam("uuid") String uuid) {
		
		if (uuid == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}

		// The query below did not work so resorting to using a JSON query
		// Query query = new Query(GridFsCriteria.where("_id").is(uuid));
		Query query = new BasicQuery(String.format(
				"{'_id' : { '$oid' : '%s' }}", uuid));

		final GridFSDBFile file = this.gridFSTemplate.findOne(query);

		if (file != null) {
			StreamingOutput streamingOutput = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException,
						WebApplicationException {
					file.writeTo(output);
				}
			};
			return Response.ok(streamingOutput).header("Content-Type", getContentType(file.getFilename())).build();
		} else {
			return Response.noContent().build();
		}
	}

	@DELETE
	@Path("/{uuid}")
	public Response deleteFile(@PathParam("uuid") String uuid) {
		
		if (uuid == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}

		// The query below did not work so resorting to using a JSON query
		// Query query = new Query(GridFsCriteria.where("_id").is(uuid));
		Query query = new BasicQuery(String.format(
				"{'_id' : { '$oid' : '%s' }}", uuid));

		this.gridFSTemplate.delete(query);

		// HTTP 204 No Content: The server successfully processed the request,
		// but is not returning any content
		return Response.noContent().build();
	}
	
	private GridFSFile storeFile(InputStream uploadedInputStream,FormDataContentDisposition fileDetail, String fileName){
		if (uploadedInputStream == null || fileDetail == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}
		DBObject metadata = new BasicDBObject();

		if(fileName!=null){
			fileName = getFileNameOnly(fileName);
		}

		if (fileDetail != null) {
			if(fileName==null){
				fileName = fileDetail.getName(); // TODO convert/escape?
			}
			metadata.put("name", fileName);
			metadata.put("type", fileDetail.getType());
			metadata.put("size", fileDetail.getSize());
		}
		GridFSFile file = this.gridFSTemplate.store(uploadedInputStream,fileName, metadata);
		// New version of gridFSTemplate allow to set the contentType, so this code will go once its release.
		if(file.getFilename()!=null){
			file.put("contentType", getContentType(file.getFilename()));
			file.save();
		}
		return  file;
	}
	
	private void createLearningContent(GridFSFile file, String parentSpaceId, String contentName, String fileName){
		LearningContent providedContent = new LearningContent();
		LearningSpace parentSpace = spaceRepository.findOne(parentSpaceId);
		providedContent.setAncestor(parentSpace);
		providedContent.setTitle(contentName);
		providedContent.setUri("/file/"+file.getId());
		providedContent.setType(ContentType.LOCAL);
		providedContent.setContentId(file.getId().toString());
		contentRepository.save(providedContent);
	}

	private String getFileNameOnly(String fullPath){
		int lastSeparatorIndex = fullPath.lastIndexOf("/") > 0 ?fullPath.lastIndexOf("/"):fullPath.lastIndexOf("\\");
		return lastSeparatorIndex>0?fullPath.substring(lastSeparatorIndex+1):fullPath;
				
	}
	
	protected String getContentType(String fileName){
		String contentType = context.getMimeType(fileName);
		if(contentType==null)
			contentType = new MimetypesFileTypeMap().getContentType(fileName);
		return contentType;
	}
}
