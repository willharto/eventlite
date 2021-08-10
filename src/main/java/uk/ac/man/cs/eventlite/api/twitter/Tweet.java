package uk.ac.man.cs.eventlite.api.twitter;

public class Tweet {

  private String date;
  private String tweet;
  private String tweetURL;
  
  public Tweet() {
    
  }
  
  public void setDate(String date) {
    this.date = date;
  }
  
  public void setTweet(String tweet) {
    this.tweet = tweet;
  }
  
  public void setTweetURL(String tweetID) {
    this.tweetURL = "https://www.twitter.com/EventliteF12/status/" + tweetID;
  }
  
  public String getDate() {
    return this.date;
  }
  
  public String getTweet() {
    return this.tweet;
  }
  
  public String getTweetURL() {
    return this.tweetURL;
  }
}
