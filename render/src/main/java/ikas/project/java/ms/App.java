package ikas.project.java.ms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import org.apache.commons.cli.*;
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
        CommandLine commandLine = null;
        Exception exception = null;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            exception = e;
            L.error("参数错误", e);
        }

        if (null != exception || commandLine.hasOption("h")) {
            var formatter = new HelpFormatter();
            formatter.printHelp("App", options);
            return;
        }

        if (commandLine.hasOption("v")) {
            L.info(VERSION);
            return;
        }


        if (commandLine.hasOption("f")) {
            workFolder = commandLine.getOptionValue("f");
        }

        //
        Objects.requireNonNull(workFolder);
        var projectFolder = Paths.get(workFolder);
        L.debug(() -> projectFolder.toAbsolutePath().toString());

        var repoFolder = projectFolder.resolve("repo");
        var templateFolder = projectFolder.resolve("template");
        var docsFolder = projectFolder.resolve("docs");

        //read repo json
        var objectMapper = new ObjectMapper();

        var apps = Files.walk(repoFolder, 1).filter(Files::isRegularFile).map(f -> {
            try (var reader = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                var app= objectMapper.readValue(reader, HashMap.class);
                return app;
            } catch (IOException e) {
                L.error("文件结构错误:" + f.toAbsolutePath().toString(), e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.groupingBy(f -> f.getOrDefault("category", "other")));

        L.debug(apps);

        //template
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

        L.info("ok");

    }


}
