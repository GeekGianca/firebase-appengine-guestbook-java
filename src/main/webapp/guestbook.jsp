<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<title>Firebase Guestbook</title>
<head>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css"/>
    <script src='https://cdn.firebase.com/js/client/2.3.2/firebase.js'></script>
</head>

<body>
<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
        pageContext.setAttribute("user", user);
%>
<p>Hello, ${fn:escapeXml(user.nickname)}! (You can
    <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>

<input id="text" maxlength="42" type="text" placeholder="Message"><br/>
<button id="post">Post</button>
<br/>

<div id="results">

</div>
<script>
var myFirebase = new Firebase('https://fb-gae-guestbook.firebaseio.com/');
var username = '${fn:escapeXml(user.email)}';

var textInput = document.querySelector('#text');
var postButton = document.querySelector('#post');

postButton.addEventListener("click", function() {
  var text = textInput.value;
  // basic and probably insufficient XSS protection
  // consider using document.createTextNode() instead
  username = username.replace(/\</g, "&lt;").replace(/\>/g, "&gt;");
  text = text.replace(/\</g, "&lt;").replace(/\>/g, "&gt;");

  myFirebase.push({email:username, text:text, created: new Date().getTime()});
  textInput.value = "";
});

myFirebase.on('child_added', function(snapshot) {
    var msg = snapshot.val();
    var html = '<div class="msg"><div class="name">' +
               '<b>' + msg.email + '</b>' +
               '<p>' + msg.text + '</p>' +
               '</div>';
    document.querySelector("#results").innerHTML += html;
});
</script>
<%
} else {
%>
<p>Hello!
    <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
    to post greetings.</p>
<%
    }
%>
</body>
</html>

