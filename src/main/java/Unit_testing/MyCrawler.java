package Unit_testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * 
 * Example for Connecting Crawler4j to Solr. Using Solr with 1 node and 1 shard.
 * Making a search engine for sample pages. 
 * Assumption - HTML element a is link (indexing both link text and attribute and storing only attribute). 
 * p is the main text and both indexed and stored. Ignoring all the other tags for while.
 * Separating out tags help us to perform different treatment on them. 
 */
public class MyCrawler extends WebCrawler {

        private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                          + "|png|tiff?|mid|mp2|mp3|mp4"
                                                          + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                          + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
        
        /*
         *  Copy on write list for testing across different threads.
         *  To do : replace with auto-commit  or Async-io operation since commits are expensive).
         */
        private List<SolrInputDocument> documentsIndexed = new CopyOnWriteArrayList<SolrInputDocument>();
        
        String url = "http://localhost:8983/solr/collection1";
        
        
        private int NO_OF_DOCUMENT_TO_COMMIT = 1;
        /**
         * You should implement this function to specify whether
         * the given url should be crawled or not (based on your
         * crawling logic).
         */
        @Override
        public boolean shouldVisit(WebURL url) {
                String href = url.getURL().toLowerCase();
                return !FILTERS.matcher(href).matches() && href.startsWith("http://www.ics.uci.edu/");
        }

        /**
         * This function is called when a page is fetched and ready 
         * to be processed by your program.
         */
        @Override
        public void visit(Page page) {          
                String url = page.getWebURL().getURL();
                System.out.println("URL: " + url);

                if (page.getParseData() instanceof HtmlParseData) {
                        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                        String text = htmlParseData.getText();
                        String html = htmlParseData.getHtml();
                        List<WebURL> links = htmlParseData.getOutgoingUrls();
                        // Parsing Tags out of Jsoup
                        Document doc = Jsoup.parse(html);
                        SolrInputDocument doSolrInputDocument = new SolrInputDocument();
                        doSolrInputDocument.setField("id", page.hashCode());
                        Elements linksList = doc.getElementsByTag("a");
                        String serverUrl = "http://localhost:8983/solr/collection1";
                        SolrServer solr = new HttpSolrServer(serverUrl);
                        
                        // To do : replace the logic with async-io for faster execution.
                        for (Element link : linksList) {
                          String linkHref = link.attr("href");
                          System.out.println(linkHref + "printed attribute \n");
                          String linkText = link.text();
                          System.out.println(linkText + "printed text \n");
                          doSolrInputDocument.setField("features", linkHref);;
                        }
                        
                        Elements paragraphList = doc.getElementsByTag("p");
                        for (Element parElement : paragraphList) {
                        	String paragraphText = parElement.text();
                        	System.out.println(paragraphText + "printed para text \n");
                        	doSolrInputDocument.setField("features", paragraphText);
                        }
                        
                        documentsIndexed.add(doSolrInputDocument);
                        
                        /*
                         * Reducing the number of commits. 
                         * To do : Replace commit with auto-commit on server side.
                         * http://stackoverflow.com/questions/17654266/solr-autocommit-vs-autosoftcommit
                         * To do : Replace add with async-io (Akka) since adds are blocking the thread.
                         */
                        if(documentsIndexed.size() > NO_OF_DOCUMENT_TO_COMMIT) {
	                        try {
	                        	solr.add(doSolrInputDocument);
	                        	
	                        	solr.commit(true, true);
	                        } catch(Exception e) {
	                        	System.out.println(e.getMessage());
	                        	e.printStackTrace();
	                        }
                        }
                        System.out.println("Text length: " + text.length());
                        System.out.println("Html length: " + html.length());
                        System.out.println("Number of outgoing links: " + links.size());
                }
        }
}