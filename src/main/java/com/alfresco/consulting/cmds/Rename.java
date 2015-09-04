package com.alfresco.consulting.cmds;

import com.alfresco.consulting.Main;
import com.alfresco.consulting.help.UsageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alexmahabir on 8/28/15.
 */
@EnableAutoConfiguration
@Component
public class Rename extends Main implements CommandLineRunner, ExitCodeGenerator {

    Logger log = Logger.getLogger(Rename.class);

    @Value("${usage.rename}")
    protected String usage;

    @Value("${rename.from:repo-amp}")
    private File from;
    @Value("${rename.to:}")
    private File to;



    @Value("${short_name.rename}")
    protected  String short_name;

    @Value("${file.ignores:.|..|overlays|.git|.svn|.hg|alf_data_dev}")
    private String ignores;
    @Value("${rename.info}")
    private String rninfo;

    int exitCode = 0;

    private final String REL_CONTEXT_DIR="src/main/amp/config/alfresco/module/";

    @Override
    public void run(String... args) throws Exception {
        //grr spring runs all CommandLineRunner implementations for now

        if (!this.isCliTarget(args))
            return;

        if (log.isDebugEnabled()) {
            if (to == null) log.debug("to is null");
            else log.debug("to: " + to.getName());
            if (from == null) log.debug("from is null");
            else log.debug("from: " + from.getName());

            for (String arg : args) {
                log.debug("args " + arg);
            }
        }

        if (to == null || to.getName().isEmpty()) {
            UsageService.printUssage(usage);
            exitCode = 1;
            return;
        }

        rninfo = rninfo.replaceFirst("_from_",
                from.getName()).replace("_to_",
                to.getName());


        System.out.println(rninfo);
        try {
            renameAmpCheck();
            updateXmls(new File("."));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e);
        }

    }

    private void renameAmpCheck() throws Exception {
        if (to.exists()) {
            log.fatal(to.getAbsolutePath() + " Already Exists!");
            exitCode=2;
            throw new Exception(to.getName() + " already exists!");
        }

        if (!from.exists()) {
            exitCode=3;
            log.fatal(from + " does not exist!");
            throw new Exception(from.getName() + " does not exist!");
        }
    }

    private File renameFile(File file) {
        Path path = Paths.get(file.getAbsolutePath());
        try {
            log.trace("Renaming file: " + file.getAbsolutePath());
            return Files.move(path,
                    path.resolveSibling(
                            file.getName().replace(from.getName(),to.getName())
                    )).toFile();
        } catch (IOException e) {
            log.error("Could not rename file: " + file.getAbsolutePath());
            log.debug(e.getStackTrace());


        }
        return file;
    }


    @Override
    public int getExitCode() {
        return exitCode;
    }

    private void updateXmls(File file) throws IOException {
        if (file.isDirectory()) {
            for (File fileItem : file.listFiles()) {
                if (fileItem.getName().matches(ignores))
                    continue;
                //rename file if needed; before updating xml
                if (fileItem.getName().matches(from.getName()+".*")) {
                    fileItem=renameFile(fileItem);
                }
                if (fileItem.isDirectory() || fileItem.getName().matches(".*xml") ) {
                    updateXmls(fileItem);
                }
            }
        } else {
            if (log.isTraceEnabled())
                log.trace("Updating File: " + file.getAbsolutePath());
            Path path = Paths.get(file.getAbsolutePath());
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            String newContent = content.replaceAll(from.getName(), to.getName());
            //only write new files if new content to avoid messing with timeModified
            if (!content.equals(newContent))
                Files.write(path, newContent.getBytes(charset));
        }

    }

    @Override
    public String getShort_name() {
        return short_name;
    }
}
