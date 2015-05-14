UserManagement
=========================


1. Please refer to http://porterhead.blogspot.co.uk/2013/01/writing-rest-services-in-java-part-1.html  Only minor changes for this app. It is a jersey+spring web service.
2. This version has been tested briefly with mysql database. 
3. It is a practice for jersey, spring, jersey test framework, rolebased security auth in jersey, and also basic user authentification and authorization.

Json Body for create a new user:

<pre>
<code>
{
  "user" : {
    "id" : null,
    "firstName" : "dadas",
    "lastName" : "dadasdasd",
    "emailAddress" : "dadsad@gmail.com",
    "socialProfiles" : [ ],
    "verified" : false
  },
  "password" : {
    "password" : "131312313123131"
  }
}
</code>
</pre>

Json Body for update a requst:

<pre>
<code>
{
  "firstName" : "dadad",
  "lastName" : "dadsad",
  "emailAddress" : "dadadada@yahoo.com"
}
</code>
</pre>

 
