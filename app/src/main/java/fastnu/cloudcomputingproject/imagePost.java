package fastnu.cloudcomputingproject;

public class imagePost {

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public imagePost(String username, String description, String picUrl, String city, String country) {
        this.username = username;
        this.description = description;
        this.picUrl = picUrl;
        this.city = city;
        this.country = country;
    }

    public imagePost(){
        username="";
        description="";
        picUrl="";
        city="";
        country="";
    }

    String username,description,picUrl,city,country;

}
