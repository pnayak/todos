package com.pragyasystems.knowledgeHub.resources.content;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.testng.annotations.Test;

import com.foobar.todos.db.EntityRepository;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;


/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test upload, download and delete of files
 */
@Test(groups = { "checkintest" })
public class ContentFileResourceTest {

	private final GridFsTemplate mockGridFsTemplate = mock(GridFsTemplate.class);
	private final EntityRepository mockRepo = mock(EntityRepository.class);
	private final SpaceRepository mockSpaceRepo = mock(SpaceRepository.class);
	
	
	private final ContentFileResource contentFileResource = new ContentFileResource(
			mockGridFsTemplate, mockRepo, mockSpaceRepo);

	@Test
	public void deleteFile() {
		
		Response response = contentFileResource.deleteFile("50c7517644aebfc7e1fef2d0");

		assertEquals(response.getStatus(), 204);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = contentFileResource.deleteFile(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

	}

	@Test
	public void downloadFile() {

		GridFSDBFile mockGridFSDBFile = mock(GridFSDBFile.class);
		when(mockGridFSDBFile.getFilename()).thenReturn("test.jpg");
		when(mockGridFSDBFile.getContentType()).thenReturn("image/jpg");
		ContentFileResource spy = spy(contentFileResource);
		doReturn("image/jpg").when(spy).getContentType("test.jpg");

		
		when(mockGridFsTemplate.findOne(any(Query.class))).thenReturn(
				mockGridFSDBFile);

		try {
			when(mockGridFSDBFile.writeTo(any(OutputStream.class))).thenReturn(
					1L);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Response response = spy.downloadFile("50c7517644aebfc7e1fef2d0");

		assertEquals(response.getStatus(), 200);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = spy.downloadFile(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		when(mockGridFsTemplate.findOne(any(Query.class))).thenReturn(null); 
		negResponse = spy.downloadFile("50c7517644aebfc7e1fef2d0");
		assertEquals(negResponse.getStatus(), 204);

	}

	@Test
	public void uploadFile() {

		InputStream mockUploadedInputStream = mock(InputStream.class);
		FormDataContentDisposition mockFileDetail = mock(FormDataContentDisposition.class);
		GridFSFile mockGridFSFile = mock(GridFSFile.class);

		when(
				mockGridFsTemplate.store(any(InputStream.class),
						any(String.class), any(DBObject.class))).thenReturn(
				mockGridFSFile);

		when(mockGridFSFile.getId()).thenReturn("511846484800016990f86233");

		Response response = contentFileResource.uploadFile(
				mockUploadedInputStream, mockFileDetail);

		assertEquals(response.getStatus(), 201);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = contentFileResource.uploadFile(null, mockFileDetail);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			negResponse = contentFileResource.uploadFile(
					mockUploadedInputStream, null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}
	@Test
	public void uploadContent() {

		InputStream mockUploadedInputStream = mock(InputStream.class);
		FormDataContentDisposition mockFileDetail = mock(FormDataContentDisposition.class);
		GridFSFile mockGridFSFile = mock(GridFSFile.class);

		when(
				mockGridFsTemplate.store(any(InputStream.class),
						any(String.class), any(DBObject.class))).thenReturn(
				mockGridFSFile);

		when(mockGridFSFile.getId()).thenReturn("511846484800016990f86233");

		Response response = contentFileResource.uploadContent(
				mockUploadedInputStream, mockFileDetail, "A-FAKE-LSID", "A-FAKE-NAME", "FAKE-FILE-NAME");

		assertEquals(response.getStatus(), 201);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");
		verify(mockRepo).save(any(LearningContent.class));

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = contentFileResource.uploadContent(null, mockFileDetail, "A-FAKE-LSID", "A-FAKE-NAME", "FAKE-FILE-NAME");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			negResponse = contentFileResource.uploadContent(
					mockUploadedInputStream, null, "A-FAKE-LSID", "A-FAKE-NAME", "FAKE-FILE-NAME");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

}
