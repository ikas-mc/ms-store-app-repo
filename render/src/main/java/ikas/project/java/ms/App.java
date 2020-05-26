package ikas.project.java.ms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ikas
 * @since 2020/05/27 00:49
 */
public class App {

    public static Logger L = LogManager.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        L.info("start");

        //
        var projectPath = Paths.get("..\\");
        L.debug(() -> projectPath.toAbsolutePath().toString());

        var repoFolder = projectPath.resolve("repo");
        var templateFolder = projectPath.resolve("template");
        var docsFolder = projectPath.resolve("docs");

        //read repo json
        var objectMapper = new ObjectMapper();

        var apps = Files.walk(repoFolder, 1).filter(f -> Files.isRegularFile(f)).map(f -> {
            try (var reader = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                return objectMapper.readValue(reader, HashMap.class);
            } catch (IOException e) {
                L.error(f.toAbsolutePath().toString(), e);
            }
            return null;
        }).filter(o -> null != o).collect(Collectors.toList());

        L.debug(apps);

        //read template
        var data = Map.of("apps", (Object) apps);
        var engine = new PebbleEngine.Builder().cacheActive(true).loader(new FileLoader()).build();

        Files.walkFileTree(templateFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var outFile = docsFolder.resolve(file.getFileName());
                L.debug(() -> outFile.toAbsolutePath().toString());

                //evaluate
                var writer = Files.newBufferedWriter(outFile, StandardOpenOption.CREATE);
                var template = engine.getTemplate(file.toAbsolutePath().toString());
                L.debug(() -> template.getName());

                template.evaluate(writer, data);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        L.info("end");

    }


}
