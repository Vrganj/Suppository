package me.vrganj.suppository;

import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Server extends NanoHTTPD {
    private final Suppository plugin;
    private final File repository;
    private final Authenticator authenticator;

    public Server(Suppository plugin, File repository, Authenticator authenticator, int port) {
        super(port);

        this.plugin = plugin;
        this.repository = repository;
        this.authenticator = authenticator;

        if (repository.mkdirs()) {
            plugin.getLogger().info("Created " + repository.getPath());
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        plugin.getLogger().info(session.getMethod() + " " + session.getUri());

        var file = new File(repository, session.getUri());

        if (!file.getAbsolutePath().startsWith(repository.getAbsolutePath())) {
            plugin.getLogger().warning("wtf: " + session.getUri());
            return null;
        }

        try {
            if (session.getMethod() == Method.GET) {
                if (!file.exists()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "File not found");
                } else if (file.isDirectory()) {
                    var result = new StringBuilder();

                    result.append("<table><tbody>");

                    result.append("<tr>");
                    result.append("<th>Name</th>");
                    result.append("<th>Last modified</th>");
                    result.append("<th>Size</th>");
                    result.append("</tr>");

                    if (!file.toPath().equals(repository.toPath())) {
                        result.append("<tr><td><a href=\"/" + repository.toPath().relativize(file.getParentFile().toPath()) + "\">Parent Directory</a><td><td></td><td>-</td></tr>");
                    }

                    for (var child : Objects.requireNonNull(file.listFiles())) {
                        var attributes = Files.readAttributes(child.toPath(), BasicFileAttributes.class);

                        result.append("<tr>");

                        result.append("<td>");
                        var path = "/" + repository.toPath().relativize(child.toPath());
                        result.append("<a href=\"").append(path).append("\">").append(child.getName()).append("</a>");
                        result.append("</td>");

                        var format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        result.append("<td>").append(format.format(new Date(attributes.lastModifiedTime().toMillis()))).append("</td>");

                        if (attributes.isDirectory()) {
                            result.append("<td>-</td>");
                        } else {
                            result.append("<td>").append(attributes.size() / 1024).append(" KiB</td>");
                        }

                        result.append("</tr>");
                    }

                    result.append("</tbody></table>");

                    return newFixedLengthResponse(Response.Status.OK, "text/html", result.toString());
                } else {
                    return newFixedLengthResponse(Files.readString(file.toPath()));
                }
            } else if (session.getMethod() == Method.PUT) {
                var headers = session.getHeaders();

                if (!headers.containsKey("authorization") || !authenticator.authenticate(headers.get("authorization"))) {
                    return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html", "Unauthorized");
                }

                file.getParentFile().mkdirs();

                int length = Integer.parseInt(session.getHeaders().get("content-length"));

                try (var stream = new FileOutputStream(file)) {
                    stream.write(session.getInputStream().readNBytes(length));
                }

                return newFixedLengthResponse(Response.Status.OK, "text/html", "<h1>created</h1>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", "shit hit the fan");
        }

        return null;
    }
}
