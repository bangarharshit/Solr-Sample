package Unit_testing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import Unit_testing.solr_impl.SolrStore;
import org.apache.solr.common.SolrInputDocument;

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
  private static AtomicInteger numOfPagesIndexed = new AtomicInteger(0);
  IStore store = new SolrStore();

  private int NO_OF_DOCUMENT_TO_COMMIT = 3;

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
      String html = htmlParseData.getHtml();

      // Parsing Tags out of Jsoup
      IIndexableContent indexableContent = store.parseIndexableContent(html);

      int currentIndex = numOfPagesIndexed.incrementAndGet();
//                        documentsIndexed.add(doSolrInputDocument);

      /*
       * Reducing the number of commits.
       * To do : Replace commit with auto-commit on server side.
       * http://stackoverflow.com/questions/17654266/solr-autocommit-vs-autosoftcommit
       * To do : Replace add with async-io (Akka) since adds are blocking the thread.
       */
      if(currentIndex <= NO_OF_DOCUMENT_TO_COMMIT) {
        try {
          store.add(indexableContent);
        } catch(Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }
}