package co.madteam.madmanager.utilities;

public class MadUtils {

	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		} else if (str.length() == 0) {
			return true;
		} else if (str.equals("")) {
			return true;
		} else if (str.equals("\n")) {
			return true;
		} else if (str.equalsIgnoreCase("null")) {
			return true;
		}
		return false;
	}

	public static boolean isEmpty(CharSequence str) {
		if (str == null) {
			return true;
		} else {
			return isEmpty(str.toString());
		}
	}
}