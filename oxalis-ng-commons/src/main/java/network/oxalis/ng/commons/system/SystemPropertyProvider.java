package network.oxalis.ng.commons.system;

import com.google.inject.Singleton;

@Singleton
public class SystemPropertyProvider {

    public String getProperty(String key) {
        return System.getProperty(key);
    }
}
