package com.cyberneticscore.realityparser.proxytester;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Temporary main class to run this guy
 * Created by shadm01 on 08-Sep-16.
 */
@Slf4j
public class MainClass {
    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("Please choose mode");
            LOGGER.info("1: crawl public links <hostname:port>");
            LOGGER.info("2: validate proxy links <hostname:port>");
            return;
        }

        String inChar = args[0];
        try {

            if (args.length<2){
                LOGGER.info("Need one more argument with queue server host:port");
                return;
            }

            switch (inChar) {
                case "1": {
                    LOGGER.info("Crawling public links for proxy lists");
                    ProxyCrawler proxyclient = new ProxyCrawler(args[1]);
                    proxyclient.crawlPublicLinks();
                    break;
                }
                case "2": {
                    LOGGER.info("Validating that Proxy is working");
                    ProxyCrawler proxyclient = new ProxyCrawler(args[1]);
                    proxyclient.validateLinks();
                    break;
                }
                case "3": {
                    LOGGER.info("Crawl a url with text file: ");
                    ProxyCrawler proxyclient = new ProxyCrawler(args[1]);
                    proxyclient.crawlPublicLinks(args[2]);
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error {}", ex);
        }
    }

}
