package com.cyberneticscore.realityparser.proxytester;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Checks if proxy server is available
 * Created by shadm01 on 04-Sep-16.
 */
@Slf4j
public class ProxyTester {
    public static boolean checkIfProxyServerIsWorking(String proxyHostAndPort) {
        String[] splits = proxyHostAndPort.split(":");
        return checkIfProxyServerIsWorking(splits[0], splits[1]);
    }

    public static boolean checkIfProxyServerIsWorking(String proxyHost, String proxyPort) {
        try {
            LOGGER.info("Testing proxy {}:{}", proxyHost, proxyPort);
            @Cleanup WebClient webClient = new WebClient(BrowserVersion.CHROME,
                    proxyHost, Integer.parseInt(proxyPort));
            webClient.getOptions().setJavaScriptEnabled(false);

            //TODO - start own local HTTPS server for test ?
            final HtmlPage page = webClient.getPage("https://ya.ru");
            int respCode = page.getWebResponse().getStatusCode();

            LOGGER.info("Response code: {}", respCode);
            if (respCode == 200 || respCode == 204) {
                return true;
            }
        } catch (NoClassDefFoundError ex) {
            //NoClassDefFoundError: org/apache/http/message/BasicHttpRequest
        }
        catch (FailingHttpStatusCodeException ex) {
            //403 Forbidden for https://google.com/
        } catch (IOException ex) {

        } catch (Exception ex){
            LOGGER.error("Unknown exception during proxy testing", ex);
        }

        return false;
    }

    public static ArrayList<String> readProxyList(String file_path) throws IOException {
        LOGGER.debug("Reading proxy files from {}", file_path);
        ArrayList<String> proxys = new ArrayList<String>();

        //List<String> proxys = Files.readAllLines(FileSystems.getDefault().getPath(file_path)); //JAVA 8

        @Cleanup FileInputStream fstream = new FileInputStream(file_path);
        @Cleanup BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

        while ((strLine = br.readLine()) != null) {
            proxys.add(strLine);
        }

        LOGGER.debug("Number of proxies in file {}", proxys.size());

        return proxys;
    }
}
