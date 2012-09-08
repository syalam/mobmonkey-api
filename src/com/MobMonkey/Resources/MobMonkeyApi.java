package com.MobMonkey.Resources;

import java.util.*;
import javax.ws.rs.core.Application;



public class MobMonkeyApi extends Application {

	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(SignUpResource.class);
		s.add(VODResource.class);
		s.add(MediaResource.class);
		s.add(PartnerResource.class);
		s.add(Error.class);
		s.add(VerifyResource.class);
		s.add(RequestMediaResource.class);
		return s;
	}
}
