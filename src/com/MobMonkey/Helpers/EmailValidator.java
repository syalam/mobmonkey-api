package com.MobMonkey.Helpers;
import java.util.regex.Pattern;
 
public class EmailValidator {
 
	static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	static Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
 
	public static boolean validate(final String email) {
		return EMAIL_REGEX.matcher(email).matches();
	}
}