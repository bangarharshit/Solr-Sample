package Unit_testing.solr_impl;

import Unit_testing.IIndexableContent;
import org.apache.solr.common.SolrInputDocument;

/**
 * Created by Avi Hayun on 12/10/2014.
 *
 */
public class SolrIndexableContent implements IIndexableContent {
  private SolrInputDocument doSolrInputDocument = new SolrInputDocument();


  public SolrInputDocument getDoSolrInputDocument() {
    return doSolrInputDocument;
  }

  public void setDoSolrInputDocument(SolrInputDocument doSolrInputDocument) {
    this.doSolrInputDocument = doSolrInputDocument;
  }
}