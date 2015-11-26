package com.google.appengine.demos.guestbook;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity
public class Greeting {

    @Id public String id;

    public String email;
    public String text;
    @Index public Date date;

    public Greeting(String email, String text, Date date) {
        this.id = email + "." + date.getTime();
        this.email = email;
        this.text = text;
        this.date = date;
    }


}
