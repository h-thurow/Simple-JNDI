# Simple-JNDI

Simple-JNDI is intended to solve two problems. 

The first is to test or use classes that depend on JNDI environment objects (most known a DataSource) provided by a Java EE container outside of such a container. So it is recommended by Spring to replace its own deprecated JNDI Mock implementation (See [Spring Framework > 
Testing > Unit Testing](https://docs.spring.io/spring-framework/reference/6.1/testing/unit.html#mock-objects-jndi)). You can also read about it here: [Test a Mock JNDI Datasource with Spring](https://www.baeldung.com/spring-mock-jndi-datasource#mock-and-test-a-jndi-datasource-using-simple-jndi).

The second problem Simple-JNDI is intended to solve is to access application configurations easily from anywhere in your application.

If your only intention is to test or use classes that depend on Tomcat's JNDI environment outside of Tomcat or you are only in need of a JNDI based DataSource give [TomcatJNDI](https://github.com/h-thurow/TomcatJNDI) (not to be confused with Simple-JNDI) a try.

Simple-JNDI's JNDI implementation is entirely memory based. No server instance is started. A java.util.Properties object, the structure of a root directory or a list of .property files serves as a model for the contexts structure. The contexts get populated with objects defined programmatically or declared in .properties, .xml or .ini files.

<h3>Download</h3>

<pre>
&lt;dependency>
    &lt;groupId>com.github.h-thurow&lt;/groupId>
    &lt;artifactId>simple-jndi&lt;/artifactId>
    &lt;version>0.25.0&lt;/version>
&lt;/dependency>
</pre>
or <a href=http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.h-thurow%22%20AND%20a%3A%22simple-jndi%22>download from here</a>.
<h3>Installing Simple-JNDI</h3>

After download, installing Simple-JNDI is as simple as adding the simple-jndi jar to your classpath. Some of the features 
do however need additional dependencies. To get connection-pooling you will need commons-dbcp, commons-dbcp2 or HikariCP.

<h3>Setting up Simple-JNDI</h3>

<p>
This is where all the work goes in a Simple-JNDI installation. Firstly you need a jndi.properties file, which somehow needs to go into your classpath. This jndi.properties needs two mandatory values:
</p>
<pre>
java.naming.factory.initial = org.osjava.sj.SimpleContextFactory
</pre>
<p>This property, <code>java.naming.factory.initial</code>, is a part of the JNDI specification. </p>
<p>
    The second required parameter, when not creating the context objects programmatically, is <code>org.osjava.sj.root</code>, where you store the files, that define the context objects you want Simple-JNDI to create. The following code block details a few examples with explanatory comments.
</p>
<pre>
# absolute directory
org.osjava.sj.root = /home/hen/gj/simple-jndi/config/
</pre><pre>
# relative directory
org.osjava.sj.root = config/
</pre>
<p>Not required, but highly recommended is setting <code><a href=#shared-or-unshared-context>org.osjava.sj.jndi.shared = true</a></code> too.</p><p>
    <b>NEW in 0.13.0:</b> Specify a list of files and/or directories. Separate them by the platform specific path separator.
</p><pre>
org.osjava.sj.root = file1.cfg:directory1/file.properties:directory2
</pre>
<p>From 0.17.2 on you should also set <code>org.osjava.sj.pathSeparator</code> to the separator used in <code>org.osjava.sj.root</code> to ensure platform independency of your jndi.properties file. See also <a href="https://github.com/h-thurow/Simple-JNDI/wiki/Load-property-files-with-any-extension-from-any-location-(New-in-0.13.0)">Load property files with any extension from any location</a>.
</p><p>
<b>NEW in 0.18.0:</b> You can load files or directories from JARs on classpath<br><p>
<pre>org.osjava.sj.root = jarMarkerClass=any.class.in.Jar,root=/root/in/jar</pre>
<p>The jarMarkerClass is the Name of a class unique over all JARs on classpath to identify the JAR containing the root directory. The JAR must be found in the file system. Very probably JARs encapsulated in WARs or uber jars will not work.
</p><p>
    <b>NEW in 0.18.2:</b> You can declare all these parameters as system properties and dispense with jndi.properties file. See <a href="https://github.com/h-thurow/Simple-JNDI/issues/16">Enhancement request: make org.osjava.sj.root not mandatory in jndi.properties</a>.
</p>

<h3>Create your contexts and context objects (file system based)</h3>

<p>Simple-JNDI stores values in multiple .properties, .xml or .ini files. The files are located under a root directory as specified with the <code>org.osjava.sj.root</code> property. </p>
<p>Directory names and file names become part of the lookup key. Each delimited tree-node becomes a JNDI Context, while the leaves are implementations. The only exceptions are pseudo sub-values, which you will see with DataSources.</p>
<p>
The easiest way to understand is to consider an example. Imagine a file-structure looking like <code>config/application1/users.properties</code> in which the file looks like:
</p><pre>
    admin = fred
    quantity = 5
    enabled = true
</pre>
<p>Now you can access these properties from anywhere in your application via JNDI:
</p><pre>
InitialContext ctx = new InitialContext();
String admin = (String) ctx.lookup("application1.users.admin");
String quantity = (String) ctx.lookup("application1.users.quantity");
String enabled = (String) ctx.lookup("application1.users.enabled");
</pre>
<p>The example assumes that you set <code>org.osjava.sj.root = config</code>.
</p>
<p>There are some reserved words you can not use as property names: <code>type, converter, javaxNamingSpiObjectFactory</code>.
</p>
<p><a href=https://github.com/h-thurow/Simple-JNDI/wiki/Declarative-creation-of-contexts-and-context-objects>Further information.</a>
</p><p>
    See also<br>
    <a href="https://github.com/h-thurow/Simple-JNDI/issues/17">System property substitution in resource files (New in 0.19.0)</a><br>
</p>

<h3>Create your contexts and context objects (programmatically)</h3>

<p>When only some objects are needed, e. g. just a DataSource, it might be more convenient, to do it programmatically. See <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Programmatically-create-your-contexts-and-context-objects-(no-resource-files-needed)>Programmatically create your contexts and context objects (no .properties, .xml or .ini files needed)</a></p>

<h3>Lookup typed properties, not only Strings</h3>

<p>
In the above example it would be favourable to lookup "quantity" as Integer and "enabled" as Boolean. To achieve this you can type your properties:
</p>
<pre>
    quantity = 5
    quantity.type = java.lang.Integer
    enabled = true
    enabled.type = java.lang.Boolean
</pre>
<p>
Thereafter you can call typed properties:
</p>
<pre>
    Integer quantity = (Integer) ctxt.lookup("application1.users.quantity");
    Boolean enabled = (Boolean) ctxt.lookup("application1.users.enabled");
</pre>
<p>
The following types are supported: Byte, Short, Integer, Long, Float, Double, Character.
<p>
Also supported are Maps (0.16.0):
</p>
<pre>
    city.type = java.util.Map
    city.citizens = 3.520.031
    city.name = Berlin
</pre>
<p>
Now you can lookup a Map:
<pre>
    Map city = (Map) ctx.lookup("city");
    assertEquals("3.520.031", city.get("citizens"));
</pre>
<p>
For further map examples <a href=https://github.com/h-thurow/Simple-JNDI/tree/master/src/test/resources/roots/maps>see here</a>.
<p>
Note that you have to write <code>quantity/type = java.lang.Integer</code> and <code>enabled/type = java.lang.Boolean</code> when setting <code>org.osjava.sj.delimiter = /</code> unless you <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Use-slash-separated-lookup-pathes-with-dot-separated-property-names-(New-in-0.14.0)>follow this description</a>. And as you might anticipate already: <code>type</code> is a reserved word with Simple-JNDI.
</p>
<p>
See also<br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/A-more-elegant-way-to-lookup-typed-properties-(New-in-0.14.0)>A more elegant way to lookup typed properties (New in 0.14.0)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Load-self-defined-types-as-Beans-or-how-to-hook-into-the-object-creation-process-(New-in-0.15.0)>Load self defined types as Beans or how to hook into the object creation process (New in 0.15.0)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Instantiate-beans-and-set-their-properties-(New-in-0.17.0)>Instantiate beans and set their properties (New in 0.17.0)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Usage-of-3rd-party-SPI-ObjectFactory-implementations-(New-in-0.21.0)>Usage of 3rd party SPI ObjectFactory implementations (New in 0.21.0)</a>
</p>

<h3>Lookup pathes with "/" as context separator instead of "."</h3>

<p>
So far we used "." as context separator in lookup pathes like in
</p>
<pre>
ctxt.lookup("application1.users.enabled");
</pre>
<p>
But more usual in JNDI world are lookup pathes like
</p>
<pre>
ctxt.lookup("application1/users/enabled");
</pre>
<p>
This is what <code>org.osjava.sj.delimiter</code> is for. If not specified, then a '.' is chosen. To use "/" as separator in lookup pathes set
</p>
<pre>
org.osjava.sj.delimiter = /
</pre>
<p>
Note that you can not mix up different separators in property names and lookup pathes. When setting <code>org.osjava.sj.delimiter = /</code> and using namespaced property names you can not declare <code>a.b.c = 123</code>. You have to declare <code>a/b/c = 123</code>. See also <a href=https://github.com/h-thurow/Simple-JNDI/issues/1>ENC problem</a>.
<p>
See also <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Use-slash-separated-lookup-pathes-with-dot-separated-property-names-(New-in-0.14.0)>Use slash separated lookup pathes with dot separated property names (New in 0.14.0)</a>
</p>

<h3>DataSources</h3>

<p>
The most popular object to get from JNDI is an object of type <i>javax.sql.DataSource</i>, allowing the developer to obtain JDBC connections to databases. Simple-JNDI supports this out of the box. See</p>
<p>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/DataSource-Configuration-DBCP-2-and-Commons-Pool-2-(New-in-0.15.0)>DataSource Configuration DBCP 2 and Commons Pool 2 (New in 0.15.0)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/DataSource-Configuration-HikariCP-(New-in-0.15.0)>DataSource Configuration HikariCP (New in 0.15.0)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/DataSource-Configuration-(commons-dbcp-1)>DataSource Configuration (commons dbcp 1)</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Usage-with-Spring>Usage with Spring - Inject a DataSource into beans</a>
</p><p>
    See also <a href=https://github.com/h-thurow/TomcatJNDI#only-interested-in-a-datasource>TomcatJNDI: Only interested in a DataSource?</a>
</p>

<h3>Shared or unshared context?</h3>

<p>Setting <code>org.osjava.sj.jndi.shared = true</code> will put the in-memory JNDI implementation into a mode whereby all InitialContexts share the same memory. By default this is not set, so every new InitialContext() call will provide an independent InitialContext that does not share its memory with the other contexts. This could be not what you want when using a DataSource or a connection pool because everytime you call new InitialContext() in your application a new DataSource or a new connection pool is created. Also when binding an object to a specific context by calling Context.bind() this object will be not visible in the context provided by a subsequent "new InitialContext()" call.</p>

<h3>Dealing with "java:comp/env" (Enterprise Naming Context, ENC) while loading</h3>

<p>Set the <code>org.osjava.sj.space</code> property. Whatever the property is set to will be automatically prepended to <i>every</i> value loaded into the system. Thus <code>org.osjava.sj.space = java:comp/env</code> simulates the JNDI environment of Tomcat. The <code>org.osjava.sj.space</code> property is not subject to delimiter parsing, so even when <code>org.osjava.sj.delimiter</code> is set to ".", you have to lookup "java:comp/env", not "java:comp.env". See also <a href=https://github.com/h-thurow/Simple-JNDI/issues/1>ENC problem</a>.</p>

<p>Another way to achieve a similar result is putting a default.properties directly under your root. In this file declare all your context objects that should reside under "java:comp/env" by prefixing all properties with "java:comp/env", e. g. <code>java:comp/env/my/size = 186</code>. This way you can set some context objects in "java:comp/env" and other objects in a different name space.</p>

 <p>You could also put a file named "java:comp.properties" in your root directory or name a directory under your root directory "java:comp". But Windows does not like having a ":" in a filename, so to deal with the ":" you can use the <code>org.osjava.sj.colon.replace</code> property. If, for example, you choose to replace a ":" with "--" (ie <code>org.osjava.sj.colon.replace = --</code>), then you will need a file named <code>java--comp.properties</code>, or a directory named <code>java--comp</code> containing a file "env.properties".</p>

<h3>Context.close() and Context.destroySubcontext()</h3>

Either methods will recursively destroy every context and dereference all contained objects. So when writing JUnit tests, it is good practice to call close() in tearDown() and reinitialize the JNDI environment in setUp() by calling new InitialContext(). But do not forget to close your datasources by yourself.

New in 0.16.0: There are situations where you want prevent SimpleJNDI from closing the contexts this way when close() is called. See issue <a href=https://github.com/h-thurow/Simple-JNDI/issues/5>Multiple datasources created when using Spring JNDI template</a> and the Hibernate related issue <a href="https://github.com/h-thurow/Simple-JNDI/issues/13">How to setup a transaction manager?</a>. To do so set
<pre>
org.osjava.sj.jndi.ignoreClose = true
</pre>
Really closing those contexts is a little bit tricky now:
<pre>
Hashtable env = new InitialContext().getEnvironment();
env.remove("org.osjava.sj.jndi.ignoreClose");
env.put("java.naming.factory.initial", "org.osjava.sj.SimpleJndiContextFactory");
new InitialContext(env).close();
</pre>

<h3>Thread considerations</h3>
<p>
Any object manually bound to a context after SimpleJNDI's initialization will be visible in any thread looking up the object. But to guarantee the visibility of modifications to an object in all threads after it was bound you have to use the set-after-write trick:</p>
<pre>
InitialContext ic = new InitialContext();
List&lt;City> cities = (List&lt;City>) ic.lookup("Cities");
cities.add(new City("Berlin"));
ic.rebind("Cities", cities); // rebind guarantees visibility in all threads
</pre>

<h3>See also</h3>

<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Change-log>Change log</a>

 <h3>Explanatory note</h3>
 
<p>This project is based on old https://github.com/hen/osjava/tree/master/simple-jndi .</p>
<ul>
<li>Several bugs fixed and many new tests added. <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Failed-Tests-in-0.11.4.1>See Failed Tests in 0.11.4.1</a>
<li>Changed the way contexts are shared, because of ContextNotEmptyException with type=javax.sql.DataSource and Beans. In shared mode subcontexts and bound objects are now managed per context and not in a single static map for the same reason.
<li>Tests ported to JUnit 4. 
<li>Maven 2/3 support. 
<li>Support for additional basic datatypes (Byte, Short, Integer, Long, Float, Double, Character) in type declaration.
</ul>


