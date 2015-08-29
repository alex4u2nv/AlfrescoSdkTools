package com.alfresco.consulting.cmds;

import com.alfresco.consulting.Main;
import com.alfresco.consulting.help.UsageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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


    @Value("${file.ignores:.|..|overlays|.git|.svn|.hg|alf_data_dev}")
    private String ignores;

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
            log.debug("Renaming File with args: ");
            for (String arg : args) {
                log.debug("args " + arg);
            }
        }

        if (to == null || to.getName().isEmpty()) {
            UsageService.printUssage(usage);
            exitCode = 1;
            return;
        }

        try {
            renameAmp();
            updateXmls(new File("."));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e);
        }

    }

    private void renameAmp() throws Exception {
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

        //rename context dir
        File contextDir = new File(from.getAbsolutePath()+ File.separator+  REL_CONTEXT_DIR+ from.getName());
        if (contextDir.exists() && contextDir.isDirectory()) {
            File toContextDir = new File(from.getAbsolutePath() + File.separator + REL_CONTEXT_DIR + to.getName());
            if (toContextDir.exists()) {
                exitCode=4;
                log.fatal(toContextDir.getAbsolutePath() + " Already Exists!");
                throw new Exception("Context Dir Already Exists! " + toContextDir.getAbsolutePath());
            }
            contextDir.renameTo(toContextDir);
        }

        //rename amp:
        from.renameTo(to);
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
                if (fileItem.isDirectory() || fileItem.getName().matches(".*xml") )
                    updateXmls(fileItem);
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
}
