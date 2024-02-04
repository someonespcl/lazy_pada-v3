package com.lazypanda.models;
import androidx.annotation.Keep;

@Keep
public class ModelUsers {

    String name, email, phoneN, image, search, uId;

    public ModelUsers() {}

    public ModelUsers(
        String name, String email, String phoneN, String image, String search, String uId) {
        this.name = name;
        this.email = email;
        this.phoneN = phoneN;
        this.image = image;
        this.search = search;
        this.uId = uId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneN() {
        return this.phoneN;
    }

    public void setPhoneN(String phoneN) {
        this.phoneN = phoneN;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getUId() {
        return this.uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }
}
