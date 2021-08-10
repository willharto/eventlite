package uk.ac.man.cs.eventlite.api.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterInterface {

  private Twitter twitter = null;
  
  public TwitterInterface() {
      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setDebugEnabled(true)
        .setOAuthConsumerKey("SwYLNpaUOcyGH85lOJoU12410")
        .setOAuthConsumerSecret("sTJe0mCZQVidkUIakqLmvEgVdtJ6DeiNNBSNZ0W2XQuDgfNUoB")
        .setOAuthAccessToken("1248574676584009728-GIIQ8R22ISGAXkxmmJ7gXSteMr5YzO")
        .setOAuthAccessTokenSecret("WwG09d8DqEhfBQUMWH5m6wAzp8KZNkRqx782UNLo9ZGUx");
      
      TwitterFactory factory = new TwitterFactory(cb.build());
      twitter = factory.getInstance();
  }
  
  public Twitter getTwitterInstance() {
    return this.twitter;
  }
}
