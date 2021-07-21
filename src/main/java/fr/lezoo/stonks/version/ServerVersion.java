package fr.lezoo.stonks.version;

/**
 * Used to find the version of the
 * server running the plugin
 *
 * @author Jules
 */
public class ServerVersion {
    private final String version;
    private final int[] integers;

    public ServerVersion(Class<?> clazz) {
        version = clazz.getPackage().getName().replace(".", ",").split(",")[3];
        String[] split = version.substring(1).split("\\_");
        integers = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
    }

    /**
     * @param version Only two integers, like { 1, 17 } for 1.17
     * @return If the current version is below or equal the given version
     */
    public boolean isBelowOrEqual(int... version) {
        return version[0] > integers[0] ? true : version[1] >= integers[1];
    }

    /**
     * @param version Only two integers, like { 1, 15 } for 1.15.x
     * @return If the current version is higher than given version
     */
    public boolean isStrictlyHigher(int... version) {
        return version[0] < integers[0] ? true : version[1] < integers[1];
        // return !isBelowOrEqual(version);
    }

    public int getRevisionNumber() {
        return Integer.parseInt(version.split("\\_")[2].replaceAll("[^0-9]", ""));
    }

    public int[] toNumbers() {
        return integers;
    }

    @Override
    public String toString() {
        return version;
    }
}
