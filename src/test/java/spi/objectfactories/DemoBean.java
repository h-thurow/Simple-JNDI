package spi.objectfactories;

/**
 * Not a bean in the proper sense because there is no argumentless constructor.
 */
public class DemoBean {

    private final String fullName;
    private final int size;

    public DemoBean(String fullName, int size) {
        this.fullName = fullName;
        this.size = size;
    }

    public String getFullName() {
        return fullName;
    }

    public int getSize() {
        return size;
    }
}
