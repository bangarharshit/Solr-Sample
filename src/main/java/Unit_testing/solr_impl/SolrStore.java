package Unit_testing.solr_impl;

import Unit_testing.IIndexableContent;
import Unit_testing.IStore;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Avi Hayun on 12/10/2014.
 * Solr implementation
 */
public class SolrStore implements IStore {
  String serverUrl = "http://localhost:8983/solr/collection1";
  SolrServer solr = new HttpSolrServer(serverUrl);

  @Override
  public SolrIndexableContent parseIndexableContent(String html) {
    SolrIndexableContent sic = new SolrIndexableContent();

    Document doc = Jsoup.parse(html);
    SolrInputDocument doSolrInputDocument = new SolrInputDocument();
    doSolrInputDocument.setField("id", html.hashCode());
    Elements linksList = doc.getElementsByTag("a");

    // To do : replace the logic with async-io for faster execution.
    for (Element link : linksList) {
      String linkHref = link.attr("href");
      System.out.println("Link attribute:" + linkHref + " \n");
      String linkText = link.text();
      System.out.println("Link text: " + linkText + " \n");
      doSolrInputDocument.setField("features", linkHref);
    }

    Elements paragraphList = doc.getElementsByTag("p");
    for (Element parElement : paragraphList) {
      String paragraphText = parElement.text();
      System.out.println("Paragraph text: " + paragraphText + " \n");
      doSolrInputDocument.setField("features", paragraphText);
    }

    sic.setDoSolrInputDocument(doSolrInputDocument);
    return sic;
  }

  @Override
  public void add(IIndexableContent indexableContent) throws IOException, SolrServerException {
    SolrIndexableContent solrIndexableContent = (SolrIndexableContent) indexableContent;

    solr.add(solrIndexableContent.getDoSolrInputDocument());
    solr.commit(true, true);
  }
}