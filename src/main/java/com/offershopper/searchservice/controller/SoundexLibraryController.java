package com.offershopper.searchservice.controller;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

@RestController
public class SoundexLibraryController {

  @PostMapping("/add-code")
  public HashSet<String> insert(@RequestBody Document document) {
    // establishing connection
    MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    MongoDatabase database = mongoClient.getDatabase("OfferShopperDb");
    MongoCollection<Document> collection = database.getCollection("soundex");
    MongoCursor cursor ;
    
    HashSet<String> set = new HashSet<String>();
    String title = (String) document.get("offerTitle");
    String keywords = (String) document.get("keywords");
    String category = (String) document.get("category");
    set.add(category.toLowerCase().trim());
    //index
    //collection.createIndex(new Document("word", 1), new IndexOptions().unique(true));  
    String regx="[,\\s]+";
    String[] titleSplit = title.split(regx);
    for (String str : titleSplit) {
      set.add(str.toLowerCase().trim());
      
    }
    String[] keywordsSplit = keywords.split(regx);
    for (String str : keywordsSplit) {
      set.add(str.toLowerCase().trim());
    }
    
    
    try {
      for (String str : set) {
        System.out.println(Soundex.getGode(str));
        cursor = collection.find(new Document("word",str)).iterator();
       if(cursor.hasNext()) { 
         cursor.close();
         continue;
       }
          cursor.close();
          collection.insertOne(new Document("code", Soundex.getGode(str)).append("word", str));
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      collection.dropIndexes();
      mongoClient.close();
    }
    return set;
  }

}
