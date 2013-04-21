package com.MobMonkey.Helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Media;
import com.MobMonkey.Models.RequestMedia;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Resources.AxisResource;
import com.MobMonkey.Resources.ResourceHelper;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Servlet implementation class AxisServlet
 */
@WebServlet("/CameraServlet")
public class CameraServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ResourceHelper rh;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CameraServlet() {
		super();
		rh = new ResourceHelper();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("dispatch.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String email = request.getParameter("email");
		String requestId = request.getParameter("requestId");
		String url = request.getParameter("url") + "\n";
		String resp = "";

		RequestMedia rm = (RequestMedia) rh.load(RequestMedia.class, email,
				requestId);

		if (rm == null) {
			resp = "The email and/or request ID not found in the database.";
			request.setAttribute("resp", resp); // It'll be available as ${sum}.
			request.getRequestDispatcher("dispatch.jsp").forward(request,
					response);
		} else {
			String videoUrl = "http://wowza-cloudfront.mobmonkey.com/live/"
					+ requestId + ".stream/playlist.m3u8";
		
			ObjectMetadata objmeta = new ObjectMetadata();
			objmeta.setContentLength(url.length());
			objmeta.setContentType("text/plain");

			InputStream bais = new ByteArrayInputStream(url.getBytes());

			PutObjectRequest putObjectRequest = new PutObjectRequest(
					"mobmonkeylive", requestId + ".stream", bais, objmeta);
			putObjectRequest.setRequestCredentials(rh.credentials());
			putObjectRequest.setCannedAcl(CannedAccessControlList.Private);

			rh.s3cli().putObject(putObjectRequest);
			
			Media m = new Media();
			m.seteMailAddress(email);
			m.setMediaType(3);
			m.setMediaURL(videoUrl);
			m.setMediaId(UUID.randomUUID().toString());
			m.setOriginalRequestor(email);
			m.setContentType("application/sdp");
			m.setRequestId(requestId);
			m.setUploadedDate(new Date());
			m.setAccepted(true);
			m.setRequestType("0");

			rh.save(m, requestId, m.getMediaId());

			

			request.setAttribute("resp", resp); // It'll be available as ${sum}.
			request.setAttribute("url", m.getMediaURL());
			request.getRequestDispatcher("dispatch.jsp").forward(request,
					response);
		}
	}

}
