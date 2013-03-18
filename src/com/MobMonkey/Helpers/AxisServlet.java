package com.MobMonkey.Helpers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Status;
import com.MobMonkey.Resources.AxisResource;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Servlet implementation class AxisServlet
 */
@WebServlet("/AxisServlet")
public class AxisServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AxisServlet() {
		super();
		// TODO Auto-generated constructor stub
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

		String resp = "";
		String mac = request.getParameter("mac");
		String oak = request.getParameter("oak");
		if(!mac.matches("\\w{12}") && !oak.matches("\\w{4}-\\w{4}-\\w{4}")){
			resp = "Incorrect mac or oak provided, format for mac should be 12 (A-Z0-9). The format for oak should be XXXX-XXXX-XXXX";
		}else{
			ClientResponse dispatchResponse = new AxisResource().dispatch(mac, oak);
			resp = "Dispatch Server returned status: " + dispatchResponse.getStatus() + ".<br>" +
			"Result: " + dispatchResponse.getEntity(String.class);
		}
		request.setAttribute("resp", resp); // It'll be available as ${sum}.
		request.getRequestDispatcher("dispatch.jsp").forward(request,
				response);
	}

}
