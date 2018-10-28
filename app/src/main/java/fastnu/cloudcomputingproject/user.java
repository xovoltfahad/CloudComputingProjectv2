package fastnu.cloudcomputingproject;

public class user {
    String name;
    String age;
    String city;
    String country;
    String phone;
    String profile;
    String email;

    public user(){}

    public user(String name, String age, String city, String country, String phone, String profile, String email) {
        this.name = name;
        this.age = age;
        this.city = city;
        this.country = country;
        this.phone = phone;
        this.profile = profile;
        this.email = email;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
