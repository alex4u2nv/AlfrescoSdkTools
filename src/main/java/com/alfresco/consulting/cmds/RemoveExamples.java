package com.alfresco.consulting.cmds;

import com.alfresco.consulting.Main;
import com.alfresco.consulting.modelconf.ExampleFiles;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexmahabir on 8/28/15.
 */
@Component
@ComponentScan("com.alfresco.consulting")
@EnableAutoConfiguration
@EnableConfigurationProperties(ExampleFiles.class)
public class RemoveExamples extends  Main implements CommandLineRunner, ExitCodeGenerator {
    int exitCode=0;


    Logger log = Logger.getLogger(RemoveExamples.class);
    @Autowired
    ExampleFiles exampleFiles;

    @Value("${file.ignores}")
    String ignores;

    private String regex="";



    @Override
    public void run(String... args) throws Exception {
        if (!this.isCliTarget(args))
            return;

        System.out.println("removing example files " + ignores);
        for (String str : exampleFiles.getFiles()) {
            regex+=str+"|";
        }
        regex = regex.substring(0, regex.length() - 1);

        log.debug(regex);

        File currentDir=new File(".");

        recursiveDelete(currentDir, false);

        Console c = System.console();
        String cont;
        do
        {
             cont = c.readLine("Proceed to deleting the listed files (Y|N): ");
        } while (!cont.matches("Y|N"));
        if (cont.equals("Y")) {
            System.out.println("Deleting Files");
            recursiveDelete(currentDir, true);
            System.out.println("Files Deleted");
        } else {
            System.out.println("Exiting without deleting files");
        }

    }


    public void recursiveDelete(File file, boolean delete) {
        if (file.isDirectory()) {
            for (File fileItem : file.listFiles()) {
                if (fileItem.getAbsolutePath().matches(ignores))
                    continue;
                if( fileItem.getAbsolutePath().matches(regex)) {
                   deleteFile(fileItem,delete);

                }
                else if (fileItem.isDirectory())
                    recursiveDelete(fileItem,delete);
            }
        } else {

            if( file.getAbsolutePath().matches(regex)) {
                deleteFile(file, delete);
            }
        }
    }


    private void deleteFile(File file, boolean delete) {
        System.out.println(file.getAbsolutePath());

        if (delete) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error("Could not delete file: " + file.getAbsolutePath());
                log.error(e.getMessage());
                log.debug(e);
            }
        }

    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
