# Simple-JNDI

Simple-JNDI is intended to solve two problems. The first is that of finding a container independent way of opening a 
database connection, the second is to access application configurations easily from anywhere in your application.

Unit tests or prototype code often need to emulate the environment within which the code is expected to run. A very 
common one is to get an object of type javax.sql.DataSource from JNDI so a java.sql.Connection to your database of 
choice may be opened.

This JNDI implementation is entirely memory based, so no server instances are started. The structure of a root directory serves as a model for the contexts structure. The contexts get populated with Objects defined by .properties files, XML files or Windows-style .ini files. Of course you can bind Objects programmatically to contexts too.

<h3>Download</h3>

<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.14.0.jar>simple-jndi-0.14.0.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.14.0-sources.jar>simple-jndi-0.14.0-sources.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.14.0-test-sources.jar>simple-jndi-0.14.0-test-sources.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.11.4.1-manual.zip>simple-jndi-0.11.4.1-manual.zip</a><br>
<pre>
&lt;dependency>
    &lt;groupId>com.github.h-thurow&lt;/groupId>
    &lt;artifactId>simple-jndi&lt;/artifactId>
    &lt;version>0.14.0&lt;/version>
&lt;/dependency>
</pre>
<h3>Installing Simple-JNDI</h3>

After download, installing Simple-JNDI is as simple as adding the simple-jndi jar to your classpath. Some of the features 
do however need additional dependencies. To get connection-pooling you will need commons-dbcp, which needs commons-pool 
and commons-collections.

<h3>Setting up Simple-JNDI</h3>

<p>
This is where all the work goes in a Simple-JNDI installation. Firstly you need a jndi.properties file, which somehow needs to go into your classpath. This jndi.properties needs two mandatory values:
</p>
<pre>
java.naming.factory.initial=org.osjava.sj.SimpleContextFactory
</pre>
<p>This property, <i>java.naming.factory.initial</i>, is a part of the jndi specification. </p>
<p>
The second required parameter, org.osjava.sj.root, is the location of your simple-jndi root, which is the location in which simple-jndi looks for values when code asks for them. The following code block details a few examples with explanatory comments.
</p>
<pre>
# absolute directory, using the default file protocol
org.osjava.sj.root=/home/hen/gj/simple-jndi/config/
</pre><pre>
# relative directory, using the default file protocol
org.osjava.sj.root=config/
</pre><pre>
# NEW in 0.13.0: Specify a list of files and/or directories. Separate them by the platform specific path separator.
org.osjava.sj.root=file1.cfg:directory1/file.properties:directory2
</pre>
<p>See also <a href="https://github.com/h-thurow/Simple-JNDI/wiki/Load-property-files-with-any-extension-from-any-location-(New-in-0.13.0)">Load property files with any extension from any location</a>.</p>

<h3>Declaratively create your contexts and context objects</h3>

<p>Simple-JNDI stores values in multiple .properties, xml or ini files. The files are located under a root directory as specified with the <i>org.osjava.sj.root</i> property. </p>
<p>Directory names and file names become part of the lookup key. Each delimited tree-node becomes a JNDI Context, while the leaves are implementations. The only exceptions are pseudo sub-values, which you will see with DataSources.</p>
<p>
The easiest way to understand is to consider an example. Imagine a file-structure looking like,
</p>
<pre>
    config/application1/users.properties
</pre>
<p>
in which the file looks like:</p>
<pre>
    admin=fred
    quantity=5
    enabled=true
</pre>
<p>The following pieces of Java are all legal ways in which to get values from Simple-JNDI. They assume that you set org.osjava.sj.root=config and that you instantiated ctxt by executing 'InitialContext ctxt = new InitialContext();'.</p>
<ul>
<li>String value = (String) ctxt.lookup("application1.users.admin")</li>
<li>String value = (String) ctxt.lookup("application1.users.quantity")</li>
<li>String value = (String) ctxt.lookup("application1.users.enabled")</li>
</ul>
</p>
<p><a href=https://github.com/h-thurow/Simple-JNDI/wiki/Declarative-creation-of-contexts-and-context-objects>Further information.</a>
</p>
<h3>Lookup typed properties, not only Strings</h3>
<p>
In the above example it would be favourable to lookup "quantity" as Integer and "enabled" as Boolean. To achieve this you can type your properties:
</p>
<pre>
    quantity=5
    quantity.type=java.lang.Integer
    enabled=true
    enabled.type=java.lang.Boolean
</pre>
<p>
Thereafter you can call typed properties:
</p>
<pre>
    Integer value = (Integer) ctxt.lookup("application1.users.quantity");
    Boolean value = (Boolean) ctxt.lookup("application1.users.enabled");
</pre>
<p>
The following types are supported: Byte, Short, Integer, Long, Float, Double, Character.
<p>
<p>
Note that you have to write "quantity/type=java.lang.Integer" and "enabled/type=java.lang.Boolean" when setting "org.osjava.sj.delimiter=/".
</p>
<p>
See also <a href=https://github.com/h-thurow/Simple-JNDI/wiki/A-more-elegant-way-to-lookup-typed-properties-(New-in-0.14.0)>A more elegant way to lookup typed properties (New in 0.14.0)</a>
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
This is what org.osjava.sj.delimiter is for. If not specified, then a '.' is chosen. To use "/" as separator in lookup pathes set
</p>
<pre>
org.osjava.sj.delimiter=/
</pre>
<p>
Note that you can not mix up different separators in property names and lookup pathes. When setting "org.osjava.sj.delimiter=/" and using namespaced property names you can not declare "a.b.c=123". You have to declare "a/b/c=123". See also <a href=https://github.com/h-thurow/Simple-JNDI/issues/1>ENC problem</a>.
<p>
See also <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Use-slash-separated-lookup-pathes-with-dot-separated-property-names-(New-in-0.14.0)>Use slash separated lookup pathes with dot separated property names (New in 0.14.0)</a>
<h3>DataSources</h3>
<p>
The most popular object to get from JNDI is a object of type <i>javax.sql.DataSource</i>, allowing the developer to obtain JDBC connections to databases. Simple-JNDI supports this out of the box.</p>

<p>There are five mandatory parameters for a DataSource in Simple-JNDI: <i>url, driver, user, password, type</i>. The following shows an example of a DataSource that will be available under the lookup key <i>application/ds/TestDS</i>. </p>
<pre>
application/ds/TestDS.properties
    type=javax.sql.DataSource
    driver=org.gjt.mm.mysql.Driver
    url=jdbc:mysql://localhost/testdb
    user=testuser
    password=testing
</pre>
<p>The code to obtain it would be:</p>
<pre>
      InitialContext ctxt = new InitialContext();
      DataSource ds = (DataSource) ctxt.lookup("application/ds/TestDS");
</pre>
<p>This example uses a delimiter of '/', which must be set with the <i>org.osjava.sj.delimiter</i> property.</p>
<p>See also <a href=https://github.com/h-thurow/Simple-JNDI/wiki/DBCP-2-and-Commons-Pool-2-support-(New-in-0.15.0)>DBCP 2 and Commons Pool 2 support (New in 0.15.0)</a></p>

<h3>Connection pooling</h3>

<p>Often when using a DataSource you will want to pool the Connections the DataSource is handing out. Simple-JNDI delegates to the Jakarta Commons DBCP project for this feature so you will need commons-dbcp, commons-pool and commons-collections jars in your classpath. </p>
<p>The feature is turned on by adding a sub-parameter of '<i>pool=&lt;pool-name&gt;</i>' in your datasource properties file. The above shown application1/ds/TestDS.properties file then looks like:
<pre>
    type=javax.sql.DataSource
    driver=org.gjt.mm.mysql.Driver
    url=jdbc:mysql://localhost/testdb
    user=testuser
    password=testing
    pool=apachePool
</pre>
<p>Note: The pool variable used to be a boolean '<i>true</i>' variable, but now a pool name is provided. This is fully backwards compatible. </p>
<p>See also <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Connection-pool-configuration-(commons-dbcp1)>Connection pool configuration (commons-dbcp1)</a> and <a href=https://github.com/h-thurow/Simple-JNDI/wiki/DBCP-2-and-Commons-Pool-2-support-(New-in-0.15.0)>DBCP 2 and Commons Pool 2 support (New in 0.15.0)</a></p>

<h3>Shared or unshared context?</h3>

<p>Setting <code>org.osjava.sj.jndi.shared=true</code> will put the in-memory JNDI implementation into a mode whereby all InitialContexts share the same memory. By default this is not set, so every new InitialContext() call will provide an independent InitialContext that does not share its memory with the other contexts. When binding an object to one of these contexts by calling Context.bind() this object is not visible in the other contexts. This could be not what you want when using a DataSource or a connection pool because everytime you call new InitialContext() in your application a new DataSource or a new connection pool is created.</p>

<h3>Dealing with "java:comp/env" (Environment Naming Context, ENC) while loading</h3>

<p>Set the <code>org.osjava.sj.space</code> property. Whatever the property is set to will be automatically prepended to <i>every</i> value loaded into the system. Thus <code>org.osjava.sj.space=java:comp/env</code> simulates the JNDI environment of Tomcat. The org.osjava.sj.space property is not subject to delimiter parsing, so even when org.osjava.sj.delimiter is set to ".", you have to lookup "java:comp/env", not "java:comp.env". See also <a href=https://github.com/h-thurow/Simple-JNDI/issues/1>ENC problem</a>.</p>

<p>Another way to achieve a similar result is putting a default.properties directly under your root. In this file declare all your context objects that should reside under "java:comp/env" by prefixing all properties with "java:comp/env", e. g. "java:comp/env/my/size=186". This way you can set some context objects in "java:comp/env" and other objects in a different name space.</p>

 <p>You could also put a file named "java:comp.properties" in your root directory or name a directory under your root directory "java:comp". But Windows does not like having a : in a filename, so to deal with the : you can use the <code>org.osjava.sj.colon.replace</code> property. If, for example, you choose to replace a <code>:</code> with <code>--</code> (ie <code>org.osjava.sj.colon.replace=--</code>), then you will need a file named <code>java--comp.properties</code>, or a directory named <code>java--comp</code> containing a file "env.properties".</p>

<h3>Context.close() and Context.destroySubcontext()</h3>

Either methods will recursively destroy every context and dereference all contained objects. So when writing JUnit tests, it is good practice to call close() in tearDown() and reinitialize the JNDI environment in setUp() by calling new InitialContext(). But do not forget to close your datasources by yourself.

<h3>Usage with Spring</h3>

<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Usage-with-Spring>See "Usage with Spring - Inject a DataSource into beans"</a>

<h3>See also</h3>

<a href=https://github.com/h-thurow/Simple-JNDI/wiki/Change-log>Change log</a>

<h3>References</h3>

<a href="http://www.mad-computer-scientist.com/blog/2017/01/06/creating-integration-tests-with-jndi">Creating Integration Tests with JNDI</a>

 <h3>Explanatory note</h3>
 
<p>This project is based on old https://github.com/hen/osjava/tree/master/simple-jndi .</p>
<ul>
<li>Several bugs fixed and many new tests added. <a href=https://github.com/h-thurow/Simple-JNDI/wiki/Failed-Tests-in-0.11.4.1>See Failed Tests in 0.11.4.1</a>
<li>Changed the way contexts are shared, because of ContextNotEmptyException with type=javax.sql.DataSource and Beans. In shared mode subcontexts and bound objects are now managed per context and not in a single static map for the same reason.
<li>Tests ported to JUnit 4. 
<li>Maven 2/3 support. 
<li>Support for additional basic datatypes (Byte, Short, Integer, Long, Float, Double, Character) in type declaration.
</ul>


