package lemon.halite3.util;

public class Networking {
	public static String readLine() {
		try {
			StringBuilder builder = new StringBuilder();
			int buffer;
			while ((buffer = System.in.read()) >= 0) {
				if (buffer == '\n') {
					break;
				}
				if (buffer == '\r') {
					// Ignore carriage return if on windows for manual testing.
					continue;
				}
				builder.append((char) buffer);
			}
			return builder.toString();
		} catch (Exception ex) {
			DebugLog.log(ex);
			throw new IllegalStateException("Exception reading line!");
		}
	}
}
