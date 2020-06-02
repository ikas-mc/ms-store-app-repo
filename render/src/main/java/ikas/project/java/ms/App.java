package ikas.project.java.ms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author ikas
 * @since 2020/05/27 00:49
 */
public class App {

    public final static Logger L = LogManager.getLogger(App.class);

    private static String VERSION = "0.1";

    public static void main(String[] args) throws IOException {
        var workFolder = "..";

        var options = new Options();
        options.addOption("v", "version", false, "version");
        options.addOption("h", "help", false, "help");
        options.addOption("f", "folder", true, "work folder");

        var parser = new DefaultParser();
        try {
            var line = parser.parse(options, args);

            if (line.hasOption("v")) {
                L.info(VERSION);
                return;
            }

            if (line.hasOption("h")) {
                var formatter = new HelpFormatter();
                formatter.printHelp("App", options);
                return;
            }

            if (line.hasOption("f")) {
                workFolder = line.getOptionValue("f");
            }

        } catch (ParseException e) {
            L.error("参数错误", e);
            return;
        }

        //
        Objects.requireNonNull(workFolder);
        var projectPath = Paths.get(workFolder);
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
        }).filter(o -> null != o).collect(Collectors.groupingBy(f -> f.getOrDefault("category", "other")));

        L.debug(apps);

        //read template
        var data = Map.of("apps", (Object) apps);
        var engine = new PebbleEngine.Builder().cacheActive(true).loader(new FileLoader()).build();

        Files.walkFileTree(templateFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var template = engine.getTemplate(file.toAbsolutePath().toString());
                L.debug(() -> template.getName());

                var outFile = docsFolder.resolve(file.getFileName());
                Files.deleteIfExists(outFile);
                L.debug(() -> outFile.toAbsolutePath().toString());

                //evaluate
                try (var writer = Files.newBufferedWriter(outFile, StandardOpenOption.CREATE_NEW)) {
                    template.evaluate(writer, data);
                } catch (IOException e) {
                    L.error(file.toAbsolutePath().toString(), e);
                }
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
