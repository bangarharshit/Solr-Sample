package Unit_testing;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 * 
 * Document class to be passed to solr. (currently not used)
 *
 */
public class CustomSolrInputDocument {
	
	// Storing the page hash as id.
	@Field
	String id;
	
	// See: schema.xml in solr config. _txt have text type text_en
	@Field("desc_en")
	List<String> body;
	
	// See: schema.xml in solr config. _txt have text type text_general 
	@Field("links_string_txt")
	List<String> linksString; // Indexed and Stored
	
	@Field("links_attribute_t")
	List<String> linksAttribute; // Stored but not indexed
	
}