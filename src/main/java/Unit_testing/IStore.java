package Unit_testing;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

/**
 * Created by Avi Hayun on 12/10/2014.
 * Interface representing the store type one wants to index one's content in
 */
public interface IStore {
  /**
   * Parses the HTML in order to extract only the wanted content
   * */
  IIndexableContent parseIndexableContent(String html);

  /**
   * Add the indexable content into the store
   * */
  void add(IIndexableContent indexableContent) throws Exception;
}