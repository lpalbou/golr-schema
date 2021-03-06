package org.bbop.schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigManager {

  private static Logger LOG = Logger.getLogger(ConfigManager.class);
  // private GOlrConfig config = null;

  private ArrayList<GOlrField> fields = new ArrayList<GOlrField>();
  // unique_fields needs to be a LinkedHashMap, otherwise the order of the fields
  // retrieved from the configuration file will not be preserved, and the order of the fields
  // returned by getFields() will be random (originally, getFields was returning a List
  // by iterating the values of a HashMap, which are not returned as a List ...).
  private LinkedHashMap<String, GOlrField> unique_fields = new LinkedHashMap<String, GOlrField>();
  private HashMap<String, ArrayList<String>> collected_comments =
      new HashMap<String, ArrayList<String>>();

  // private String searchable_extension = null;

  /**
   * Constructor.
   */
  public ConfigManager() {
    // Nobody here.
  }

  private void addFieldToBook(String config_id, GOlrField field) {
    // Ensure presence of item; only take the first one.
    if (!unique_fields.containsKey(field.id)) {
      unique_fields.put(field.id, field);
    } else {
      // check if defined fields are equivalent
      if (!unique_fields.get(field.id).equals(field)) {
        throw new IllegalStateException(field.id + " is defined twice with different properties.\n"
            + unique_fields.get(field.id) + "\n" + field);
      }
    }
    // Ensure presence of comments (description) list.
    if (!collected_comments.containsKey(field.id)) {
      collected_comments.put(field.id, new ArrayList<String>());
    }
    // And add to it if there is an available description.
    if (field.description != null) {
      ArrayList<String> comments = collected_comments.get(field.id);
      comments.add(" " + config_id + ": " + field.description + " ");
      collected_comments.put(field.id, comments);
    }
  }

  /**
   * Work with a flexible document definition from a configuration file.
   *
   * @param location
   * @throws FileNotFoundException
   */
  public void add(String location) throws FileNotFoundException {

    // Find the file in question on the filesystem.
    InputStream input = new FileInputStream(new File(location));

    LOG.info("Found flex config: " + location);
    Yaml yaml = new Yaml(new Constructor(GOlrConfig.class));
    GOlrConfig config = (GOlrConfig) yaml.load(input);
    LOG.info("Dumping flex loader YAML: \n" + yaml.dump(config));

    // searchable_extension = config.searchable_extension;

    // Plonk them all in to our bookkeeping.
    for (GOlrField field : config.fields) {
      addFieldToBook(config.id, field);
      fields.add(field);
    }
    // for( GOlrDynamicField field : config.dynamic ){
    // addFieldToBook(field);
    // dynamic_fields.add(field);
    // }
  }

  /**
   * Get all the fields.
   * 
   * @return fields
   */
  public ArrayList<GOlrField> getFields() {

    ArrayList<GOlrField> collection = new ArrayList<GOlrField>();

    // Plonk them all in to our bookkeeping.
    for (GOlrField field : unique_fields.values()) {
      collection.add(field);
    }

    return collection;
  }

  /**
   * Return the comments associated with the GOlrCoreField id; empty list if there weren't any.
   * 
   * @param id
   * @return comments
   */
  public ArrayList<String> getFieldComments(String id) {

    ArrayList<String> collection = new ArrayList<String>();

    // Plonk them all in to our bookkeeping. private HashMap<String, ArrayList<String>>
    // collected_comments = new HashMap<String, ArrayList<String>>();
    if (collected_comments.containsKey(id)) {
      collection = collected_comments.get(id);
    }

    return collection;
  }

  // /**
  // * Get the by conf static "document_category".
  // *
  // * @returns String
  // */
  // public String getDocumentCategory() {
  // return fields;
  // }

  // /**
  // * Get the extension to be used with the searchable field generation.
  // *
  // * @returns ArrayList<GOlrField>
  // */
  // public String getSearchableExtension() {
  // return searchable_extension;
  // }
}
