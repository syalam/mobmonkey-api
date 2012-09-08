package com.MobMonkey.Filters;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class ResponseApiFilter implements ContainerResponseFilter {

	public ResponseApiFilter() {
		
	}


	@Override
	public ContainerResponse filter(ContainerRequest req,
			ContainerResponse response) {

		Object hi = response.getEntity();
		String test = hi.toString();
		return response;

	}

}
