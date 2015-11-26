package com.google.appengine.demos.guestbook;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity public class Greeting {

    @Id public String id;

    public String email;
    public String text;
    public Long created;

    public Greeting() {

    }

    public void setId(){
        this.id = this.email + "." + this.created;
    }

}
