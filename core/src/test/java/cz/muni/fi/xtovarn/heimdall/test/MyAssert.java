package cz.muni.fi.xtovarn.heimdall.test;

import org.junit.Assert;

public class MyAssert {
	
	private static AssertionError assertionError = null;
	
	public static void assertEquals(Object expected, Object actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			assertionError = e;
		}
	}
	
	public static void assertNotNull(Object object) {
		try {
			Assert.assertNotNull(object);
		} catch (AssertionError e) {
			assertionError = e;
		}
	}
	
	public static void clearAssertionError() {
		assertionError = null;
	}
	
	public static void throwAssertionErrorIfAny() {
		if (assertionError != null)
			throw assertionError;
	}

}
