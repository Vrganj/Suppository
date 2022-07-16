package me.vrganj.suppository;

import net.freeutils.httpserver.HTTPServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Server extends HTTPServer {
    private final Suppository plugin;
    private final File repository;
    private final Authenticator authenticator;

    public Server(Suppository plugin, File repository, Authenticator authenticator, int port) {
        super(port);

        this.plugin = plugin;
        this.repository = repository.getAbsoluteFile();
        this.authenticator = authenticator;

        if (repository.mkdirs()) {
            plugin.getLogger().info("Created " + repository.getPath());
        }
    }

    @Override
    protected void handleMethod(Request req, Response res) throws IOException {
        plugin.getLogger().info(req.getMethod() + " " + req.getPath());

        var file = new File(repository, req.getPath()).getCanonicalFile();

        if (file.isHidden() || file.getName().startsWith(".")) {
            res.sendError(404);
            return;
        }

        if (!file.getPath().startsWith(repository.getPath())) {
            res.sendError(403);
            return;
        }

        var headers = req.getHeaders();

        if (!headers.contains("authorization") || !authenticator.authenticate(headers.get("authorization"))) {
            res.getHeaders().add("WWW-Authenticate", "Basic");
            res.sendError(401);
            return;
        }

        if (req.getMethod().equals("GET")) {
            if (!file.exists()) {
                res.sendError(404);
                return;
            }

            if (file.isDirectory()) {
                res.send(200, createIndex(file, req.getPath()));
                return;
            }

            serveFileContent(file, req, res);
        } else if (req.getMethod().equals("PUT")) {
            file.getParentFile().mkdirs();

            try (var stream = new FileOutputStream(file)) {
                req.getBody().transferTo(stream);
            }

            res.send(200, "written");
        }
    }
}
