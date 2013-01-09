package irc;

public class Tools {

	  /**
	   * <p>Case insensitive check if a String starts with a specified prefix.</p>
	   *
	   * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
	   * references are considered to be equal. The comparison is case insensitive.</p>
	   *
	   * <pre>
	   * StringUtils.startsWithIgnoreCase(null, null)      = true
	   * StringUtils.startsWithIgnoreCase(null, "abcdef")  = false
	   * StringUtils.startsWithIgnoreCase("abc", null)     = false
	   * StringUtils.startsWithIgnoreCase("abc", "abcdef") = true
	   * StringUtils.startsWithIgnoreCase("abc", "ABCDEF") = true
	   * </pre>
	   *
	   * @see java.lang.String#startsWith(String)
	   * @param str  the String to check, may be null
	   * @param prefix the prefix to find, may be null
	   * @return <code>true</code> if the String starts with the prefix, case insensitive, or
	   *  both <code>null</code>
	   * @since 2.4
	   */
	  public static boolean startsWithIgnoreCase(String str, String prefix) {
	      return startsWith(str, prefix, true);
	  }

	  /**
	   * <p>Check if a String starts with a specified prefix (optionally case insensitive).</p>
	   *
	   * @see java.lang.String#startsWith(String)
	   * @param str  the String to check, may be null
	   * @param prefix the prefix to find, may be null
	   * @param ignoreCase indicates whether the compare should ignore case
	   *  (case insensitive) or not.
	   * @return <code>true</code> if the String starts with the prefix or
	   *  both <code>null</code>
	   */
	  private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
	      if (str == null || prefix == null) {
	          return (str == null && prefix == null);
	      }
	      if (prefix.length() > str.length()) {
	          return false;
	      }
	      return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
	  }

	  public static boolean isInteger(String s) {
		  try {
			  Integer.parseInt(s);
			  return true;
		  } catch (Exception e) {
			  return false;
		  }
	  }

}
