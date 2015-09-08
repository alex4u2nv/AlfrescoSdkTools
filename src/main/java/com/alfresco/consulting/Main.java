package com.alfresco.consulting;

import com.alfresco.consulting.help.UsageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@EnableAutoConfiguration
public class Main implements CommandLineRunner {

    static Logger logger = Logger.getLogger(Main.class);

    @Value("${usage.general}")
    private String usage;

    protected String short_name = "";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);

    }

    public boolean isCliTarget(String... args) {
        final String packagePath = "com.alfresco.consulting.cmds.";
        if (logger.isDebugEnabled()) {
            for (String arg : args) {
                logger.debug("arg: " + arg);
            }
            logger.debug("Shortname: " + this.getShort_name());
        }
        if (args != null && args.length > 0 &&
                (this.getClass().getName().toLowerCase().equals((packagePath + args[0].toLowerCase()))
                        || this.getShort_name().equals(args[0].toLowerCase())))
            return true;
        return false;
    }

    public String getShort_name() {
        return short_name;
    }


    @Override
    public void run(String... args) throws Exception {
        if (args == null || args.length == 0) {
            UsageService.printUssage(usage);
        }
    }




}
