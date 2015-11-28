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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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

public class CleanServlet extends HttpServlet {

    Logger log = Logger.getLogger(this.getClass().getName());

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        log.info("Starting daily clean process...");
        //Create a new Firebase instance and subscribe on child events.
        final Firebase firebase = new Firebase("https://fb-gae-guestbook.firebaseio.com");

        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                log.info("Retrieved all the greetings...");

                // Build the email message contents using every field from Firebase.
                final StringBuilder guestBookSummaryMessage = new StringBuilder();
                guestBookSummaryMessage.append("Good Morning!\n\n"
                    + "You have the following new greetings in your guestbook:\n");
                for (DataSnapshot greetingItem : dataSnapshot.getChildren()) {
                    Greeting newGreeting = greetingItem.getValue(Greeting.class);
                    guestBookSummaryMessage.append(newGreeting.email).append(" wrote:\n")
                        .append(newGreeting.text).append("\n");
                    // Remove node
                    firebase.child(greetingItem.getKey()).removeValue();
                }
                guestBookSummaryMessage.append("\n\nThe Firebase Guestbook Team");
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                try {
                    Message msg = new MimeMessage(session);
                    //Make sure you substitute your project-id in the email From field
                    msg.setFrom(
                        new InternetAddress("guestbook@firebase-gae-guestbook.appspotmail.com",
                            "Firebase Guestbook"));
                    msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress("dcifuen@gmail.com")); //FIXME: Hardcoded email
                    msg.setSubject("[Firebase Guestbook] Guestbook Summary");
                    msg.setText(guestBookSummaryMessage.toString());
                    Transport.send(msg);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    log.warning(e.getMessage());
                }
            }

            @Override public void onCancelled(FirebaseError firebaseError) {
                log.info("Firebase error...");
            }

        });

        resp.setContentType("text/plain");
        resp.getWriter().println("Guestbook summary sent!");

    }
}
