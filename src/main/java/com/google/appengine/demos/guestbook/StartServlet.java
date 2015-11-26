/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.guestbook;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.googlecode.objectify.ObjectifyService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

public class StartServlet extends HttpServlet {

    Logger log = Logger.getLogger(this.getClass().getName());

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        log.info("Starting application...");
        //Create a new Firebase instance and subscribe on child events.
        Firebase firebase = new Firebase("https://fb-gae-guestbook.firebaseio.com");

        firebase.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                log.info("Child added...");
                Greeting newGreeting = snapshot.getValue(Greeting.class);
                log.info(newGreeting.toString());
                //Check if already stored
                ObjectifyService.begin();
                Greeting existing = ObjectifyService.ofy().load().type(Greeting.class)
                    .id(newGreeting.email + "." + newGreeting.created).now();
                if (existing == null) {
                    log.info("New greeting, storing and emailing it");
                    newGreeting.setId();
                    ObjectifyService.ofy().save().entity(newGreeting).now();
                    //Now Send the email
                    final StringBuilder greetingMessage = new StringBuilder();
                    greetingMessage.append(
                        "Hello!\n\nYou have signed the guestbook, here is a copy for your records:\n\n");
                    greetingMessage.append(newGreeting.text);
                    greetingMessage.append("\n\nThe Firebase Guestbook Team");
                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props, null);
                    try {
                        Message msg = new MimeMessage(session);
                        //Make sure you substitute your project-id in the email From field
                        msg.setFrom(
                            new InternetAddress("guestbook@firebase-gae-guestbook.appspotmail.com",
                                "Firebase Guestbook"));
                        msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(newGreeting.email));
                        msg.setSubject("[Firebase Guestbook] You have signed the guestbook");
                        msg.setText(greetingMessage.toString());
                        Transport.send(msg);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        log.warning(e.getMessage());
                    }
                } else {
                    log.info("Existing greeting, nothing else to do here");
                }

            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                log.info("Child changed...");
            }

            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
                log.info("Child removed...");
            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                log.info("Child moved...");
            }

            @Override public void onCancelled(FirebaseError firebaseError) {
                log.info("Firebase error...");
            }


        });

    }
}
