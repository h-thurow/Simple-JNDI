package spi.objectfactories;

/**
 * Not a bean in the proper sense because there is no argumentless constructor.
 */
public class DemoBean2 {

    private final String city;
    private final int inhabitants;

    public DemoBean2(String city, int inhabitants) {
        this.city = city;
        this.inhabitants = inhabitants;
    }

    public String getCity() {
        return city;
    }

    public int getInhabitants() {
        return inhabitants;
    }
}
