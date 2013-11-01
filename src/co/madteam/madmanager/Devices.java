package co.madteam.madmanager;

public class Devices {

	public static final int GALAXY5 = 1;
	public static final int GALAXY_MINI = 2;
	public static final int OPTIMUS_CHIC = 3;
	public static final int GALAXY_FIT = 4;

	public static String galaxy5 = "galaxy5";
	public static String galaxy_mini = "galaxy_mini";
	public static String galaxy_fit = "galaxy_fit";
	public static String optimus_chic = "optimus_chic";

	public static int getID() {
		String Device = getDeviceName();

		if (Device.equals(galaxy5)) {
			return GALAXY5;
		}

		else if (Device.equals(optimus_chic)) {
			return OPTIMUS_CHIC;
		}

		else if (Device.equals(galaxy_mini)) {
			return GALAXY_MINI;
		}
		
		else if (Device.equals(galaxy_fit)) {
			return GALAXY_FIT;
		}

		else
			return 0;
	}

	public static String getDeviceName() {

		String Device = Command.b("getprop ro.product.device");

		if (Device.equals("GT-I5500")) {
			return galaxy5;
		} else if (Device.equals("GT-I5500L")) {
			return galaxy5;
		} else if (Device.equals("GT-I5500B")) {
			return galaxy5;
		} else if (Device.equals("GT-I5500M")) {
			return galaxy5;
		} else if (Device.equals("GT-I5503")) {
			return galaxy5;
		} else if (Device.equals("GT-I5503T")) {
			return galaxy5;
		} else if (Device.equals("GT-I5508")) {
			return galaxy5;
		}

		else if (Device.equals("e720")) {
			return optimus_chic;
		}

		else if (Device.equals("GT-S5570")) {
			return galaxy_mini;
		}
		
		else if (Device.equals("GT-S5670")) {
			return galaxy_fit;
		}
		else if (Device.equals("s5670")) {
			return galaxy_fit;
		}

		else {
			return Device;
		}
	}
}
