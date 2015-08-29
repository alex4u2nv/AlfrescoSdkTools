package com.alfresco.consulting.help;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Created by alexmahabir on 8/28/15.
 */
@Service
public class UsageService {

    private static Logger log = Logger.getLogger(UsageService.class);
    @Autowired
    ApplicationContext applicationContext;
    public static void printUssage(String usageHelp) {
        System.out.println(usageHelp);
    }
}
