package com.alfresco.consulting;

import com.alfresco.consulting.help.UsageService;
import com.alfresco.consulting.modelconf.ExampleFiles;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableAutoConfiguration
public class Main  implements CommandLineRunner{

    static Logger logger = Logger.getLogger(Main.class);

    @Value("${usage.general}")
    private String usage;

    protected String short_name;

    public static void main(String[] args) throws Exception {
        if (logger.isDebugEnabled())
        {
            for(String arg:args) {
                logger.debug("Arg => " + arg);
            }
        }

            SpringApplication.run(Main.class, args);

    }

    public boolean isCliTarget(String ... args) {
        final String packagePath="com.alfresco.consulting.cmds.";
        if (args.length>0 && (this.getClass().getName().toLowerCase().equals((packagePath+args[0].toLowerCase())) || this.short_name.equals(args[0].toLowerCase())))
            return true;
        return false;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args == null || args.length==0) {
            UsageService.printUssage(usage);
        }
    }
}
