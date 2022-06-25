package me.vrganj.suppository;

import fi.iki.elonen.NanoHTTPD;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class Suppository extends JavaPlugin {
    private NanoHTTPD server;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var port = getConfig().getInt("port");

        var authenticator = new Authenticator(getConfig().getStringList("users"));
        var repository = new File(getDataFolder(), "repository");
        server = new Server(this, repository, authenticator, port);

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("Started HTTPD server on port " + port);
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
