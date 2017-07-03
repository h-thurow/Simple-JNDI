package spi.objectfactories;

/**
 * Created by hot on 03.07.17.
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
