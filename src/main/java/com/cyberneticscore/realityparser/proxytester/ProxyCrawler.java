package com.cyberneticscore.realityparser.proxytester;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ProxyCrawler {
    private final String dirtyProxyUrl;
    private final String cleanProxyUrl;

    public ProxyCrawler(String queueServerHostPort) {
        dirtyProxyUrl = queueServerHostPort + "/dirtyproxy";
        cleanProxyUrl = queueServerHostPort + "/proxy";
    }

    public void crawlPublicLinks(String proxyFile) throws IOException {
        List<String> nonFiltered = readProxyFromTxtPage(proxyFile);
        List<String> filtered = matchWholeList(nonFiltered);

        for (String z : filtered) {
            doPostWithProxy(dirtyProxyUrl, z); //break loop
        }


        LOGGER.info("DONE adding links");
    }

    public void crawlPublicLinks() throws IOException {
        String[] proxyUrls =
                {"http://rebro.weebly.com/uploads/2/7/3/7/27378307/rebroproxy-all-113326062014.txt",
                        "http://txt.proxyspy.net/proxy.txt",
                        "http://multiproxy.org/txt_all/proxy.txt"
                };

        for (String proxyUrl : proxyUrls) {
            List<String> nonFiltered = readProxyFromTxtPage(proxyUrl);
            List<String> filtered = matchWholeList(nonFiltered);

            for (String z : filtered) {
                doPostWithProxy(dirtyProxyUrl, z); //break loop
            }
        }

        LOGGER.info("DONE adding links");
    }

    public void validateLinks() throws IOException {
        LOGGER.info("Started validating links");
        while (true) {
            String proxyToCheck = doJsonGetWithProxy(dirtyProxyUrl);

            if ((proxyToCheck == null) || (proxyToCheck.isEmpty())) {
                LOGGER.info("No more links left, exiting");
                break;
            }

            boolean work = ProxyTester.checkIfProxyServerIsWorking(proxyToCheck);

            if (work) {
                doPostWithProxy(cleanProxyUrl, proxyToCheck);
                LOGGER.info("Working proxy: {}", proxyToCheck);
            }
        }

        LOGGER.info("Finished validating links");

    }

    List<String> readProxyFromTxtPage(String link) throws IOException {
        String scanned = new Scanner(new URL(link)
                .openStream(), "UTF-8").useDelimiter("\\A").next();

        return Arrays.asList(scanned.split("\n"));
    }

    ArrayList<String> matchWholeList(List<String> proxysList) {
        ArrayList<String> res = new ArrayList<>();

        for (String line : proxysList) {
            String ip = matchNumericProxy(line);
            if (ip.isEmpty()) {
                continue;
            }

            res.add(ip);
            LOGGER.info("matched IP {}", ip);
        }

        return res;
    }

    public String matchNumericProxy(String line) {
        final String ipPattern = "(\\d{1,3}+\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{2,4})"; //TODO - too primitive
        final Pattern regex = Pattern.compile(ipPattern);
        Matcher m = regex.matcher(line);

        try {
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception ex) {

        }
        return "";
    }

    String doJsonGetWithProxy(String connectionUrl) {
        try {
            URL url = new URL(connectionUrl);

            @Cleanup(value = "disconnect")
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            @Cleanup BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String link = br.lines().collect(Collectors.joining(""));

            LOGGER.info("Pulled link from queue {}", link);

            return link;
        } catch (MalformedURLException ex) {
            LOGGER.error("Malformed URL: {}", connectionUrl, ex);
        } catch (IOException ex) {
            LOGGER.error("IO Exception.", ex);
        }

        return "";
    }

    void doPostWithProxy(String connectionUrl, String proxy) {
        LOGGER.info("Posting to queue {} proxy {}", connectionUrl, proxy);
        try {
            URL url = new URL(connectionUrl);

            @Cleanup(value = "disconnect")
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("proxyUrl", proxy); //TODO not sure that it's the best way to pass params

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Obtained error response with code {}", conn.getResponseCode());
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            LOGGER.info("proxy was added");
            return;
        } catch (MalformedURLException ex) {
            LOGGER.error("Malformed URL: {}", connectionUrl, ex);
        } catch (ConnectException ex) {
            LOGGER.error("Conection issues at : {}", connectionUrl, ex); //TODO disconnect here
        } catch (IOException ex) {
            LOGGER.error("IO Exception.", ex);
        }

    }

}