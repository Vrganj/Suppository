package me.vrganj.suppository;

import java.util.Base64;
import java.util.List;

public class Authenticator {
    private final List<String> users;

    public Authenticator(List<String> users) {
        this.users = users;
    }

    boolean authenticate(String encoded) {
        if (!encoded.startsWith("Basic ")) {
            return false;
        }

        try {
            var decoded = new String(Base64.getDecoder().decode(encoded.substring(6)));
            return users.contains(decoded);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
