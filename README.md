# Simple-JNDI

Simple-JNDI is intended to solve two problems. The first is that of finding a container independent way of opening a 
database connection, the second is to find a good way of specifying application configurations.

Unit tests or prototype code often need to emulate the environment within which the code is expected to run. A very 
common one is to get an object of type javax.sql.DataSource from JNDI so a java.sql.Connection to your database of 
choice may be opened.<br>

<h3>Installing Simple-JNDI</h3>

After download, installing Simple-JNDI is as simple as adding the simple-jndi jar to your classpath. Some of the features 
do however need additional dependencies. To get connection-pooling you will need commons-dbcp, which needs commons-pool 
and commons-collections.

<h3>Setting up Simple-JNDI</h3>

<p>
This is where all the work goes in a Simple-JNDI installation. Firstly you need a jndi.properties file, which somehow needs to go into your classpath. This jndi.properties needs one mandatory value:
</p>
<pre>
java.naming.factory.initial=org.osjava.sj.SimpleContextFactory
</pre>
<p>This property, <i>java.naming.factory.initial</i>, is a part of the jndi specification. </p>
<p>
There are two simple-jndi specific parameters. <br></br>
The first (required) parameter, org.osjava.sj.root, is the location of your simple-jndi root, which is the location in which simple-jndi looks for values when code asks for them. The following code block details a few examples with explanatory comments.
</p>
<pre>
# absolute directory, using the default file protocol
org.osjava.sj.root=/home/hen/gj/simple-jndi/config/

# relative directory, using the default file protocol
org.osjava.sj.root=config/

# specified file protocol with an absolute directory
org.osjava.sj.root=file:///home/hen/gj/simple-jndi/config/

# specified file protocol with a relative directory
org.osjava.sj.root=file://config/

</pre>
<p>
If no org.osjava.sj.root is specified, an Exception is thrown. When classpath support is re-implemented, then a classpath root will be chosen, with no package.
</p>
<p>
The second (optional) parameter is the delimiter used to separate elements in a lookup value. This allows code to get closer to pretending to be another JNDI implementation, such as DNS or LDAP.</p>
<pre>
# DNS/Java like delimiters
org.osjava.sj.delimiter=.

# LDAP/XPath like delimiters
org.osjava.sj.delimiter=/
</pre>
<p>
If no org.osjava.sj.delimiter is specified, then a '.' (dot) is chosen. 
</p>

<h3>Memory implementation configuration</h3>

<p>Setting <code>org.osjava.sj.jndi.shared=true</code> will put the in-memory JNDI implementation into a mode whereby all InitialContext's share the same memory. By default this is not set, so two separate InitialContext's do not share the same memory and what is bound to one will not be viewable in the other. </p>

<h3>Dealing with java: while loading</h3>

 <p>Windows does not like having a : in a filename, so to deal with the : you can use the <code>org.osjava.sj.colon.replace</code> property. If, for example, you choose to replace a <code>:</code> with <code>--</code> (ie <code>org.osjava.sj.colon.replace=--</code>), then you will need a file named <code>java--.properties</code>, or a directory named <code>java--</code>. Alternatively, the next section provides a different way of handling things. </p>

<h3>Dealing with ENCs while loading</h3>

<p>To simulate an environment naming context (ENC), the <code>org.osjava.sj.space</code> property may be used. Whatever the property is set to will be automatically prepended to every value loaded into the system. Thus <code>org.osjava.sj.space=java:/comp/env</code> simulates the JNDI environment of Tomcat. </p>
 <p>As <code>:</code> is usually found in an ENC, using this property to handle ENCs is a simpler way to handle the colon than using the colon-replace property. </p>
