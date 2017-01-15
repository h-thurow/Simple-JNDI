# Simple-JNDI

Simple-JNDI is intended to solve two problems. The first is that of finding a container independent way of opening a 
database connection, the second is to find a good way of specifying application configurations.

Unit tests or prototype code often need to emulate the environment within which the code is expected to run. A very 
common one is to get an object of type javax.sql.DataSource from JNDI so a java.sql.Connection to your database of 
choice may be opened.

The JNDI implementation is entirely memory based, so no server instances are started. The structure of a root directory serves as a model for the contexts structure. The contexts get populated with Objects defined by .properties files, XML files or Windows-style .ini files. The files may be either on the file system or in the classpath. Of course you can bind Objects programmatically to contexts too.

<h3>Download</h3>

<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.13.0.jar>simple-jndi-0.13.0.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.13.0-sources.jar>simple-jndi-0.13.0-sources.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.13.0-test-sources.jar>simple-jndi-0.13.0-test-sources.jar</a><br>
<a href=https://github.com/h-thurow/Simple-JNDI/raw/master/dist/simple-jndi-0.11.4.1-manual.zip>simple-jndi-0.11.4.1-manual.zip</a><br>
<pre>
&lt;dependency>
    &lt;groupId>com.github.h-thurow&lt;/groupId>
    &lt;artifactId>simple-jndi&lt;/artifactId>
    &lt;version>0.13.0&lt;/version>
&lt;/dependency>
</pre>
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
There are two simple-jndi specific parameters. <br>
The first (required) parameter, org.osjava.sj.root, is the location of your simple-jndi root, which is the location in which simple-jndi looks for values when code asks for them. The following code block details a few examples with explanatory comments.
</p>
<pre>
# absolute directory, using the default file protocol
org.osjava.sj.root=/home/hen/gj/simple-jndi/config/

# relative directory, using the default file protocol
org.osjava.sj.root=config/

# NEW in 0.13.0: Specify a list of files and/or directories. Separate them by the platform specific path separator
org.osjava.sj.root=file1.cfg;directory1/file.properties;directory2

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

<h3>Creating your data files</h3>

<p>Simple-JNDI stores values in multiple .properties, xml or ini files and are looked up using a specified name convention, such as dot or slash delimited. It is also possible to set the type of object a property represents. As already mentioned, the files are located under a root directory as specified with the <i>org.osjava.sj.root</i> property. </p>
<p>In addition to the delimited lookup key structure, directory names and file names become part of the lookup key. Each delimited tree-node becomes a JNDI Context, while the leaves are implementations. The only exceptions are pseudo sub-values, which you will see with DataSource and other converters. </p>

<h4>Examples</h4>

<p>
The easiest way to understand is to consider a few examples. Imagine a file-structure looking like,
</p>
<pre>
config/
config/debug.properties
config/ProductionDS.properties
config/application1/default.properties
config/application1/ds.properties
config/application1/users.properties
</pre>
<p>
in which the files look like;
<dl>
<dt>default.properties</dt>
<dd>
name=Prototype<br>
url=http://www.generationjava.com/
</dd>
<dt>debug.properties</dt>
<dd>
state=ERROR
</dd>
<dt>ProductionDS.properties</dt>
<dd>
type=javax.sql.DataSource
driver=org.gjt.mm.mysql.Driver
url=jdbc:mysql://localhost/testdb
user=testuser
password=testing
</dd>
<dt>application1/default.properties</dt>
<dd>
name=My Application<br>
version=v3.4
</dd>
<dt>application1/ds.properties</dt>
<dd>
TestDS.type=javax.sql.DataSource<br>
TestDS.driver=org.gjt.mm.mysql.Driver<br>
TestDS.url=jdbc:mysql://localhost/testdb<br>
TestDS.user=testuser<br>
TestDS.password=testing
</dd>
<dt>application1/users.properties</dt>
<dd>
admin=fred<br>
customer=jim<br>
quantity=5<br>
quantity.type=java.lang.Integer<br>
enabled=true<br>
enabled.type=java.lang.Boolean
</dd>
</dl>
<p>The following pieces of Java are all legal ways in which to get values from Simple-JNDI. They assume they are preceded with a line of 'InitialContext ctxt = new InitialContext();'.</p>
<ul>
<li>Object value = ctxt.lookup("debug.state")</li>
<li>Object value = ctxt.lookup("name")</li>
<li>Object value = ctxt.lookup("url")</li>
<li>Object value = ctxt.lookup("ProductionDS")</li>
<li>Object value = ctxt.lookup("application1.name")</li>
<li>Object value = ctxt.lookup("application1.TestDS")</li>
<li>Object value = ctxt.lookup("application1.users.admin")</li>
<li>Object value = ctxt.lookup("application1.users.quantity")</li>
<li>Object value = ctxt.lookup("application1.users.enabled")</li>
</ul>
Note that the ProductionDS and TestDS return types are objects of type javax.sql.DataSource, while application1.users.quantity is an Integer and application1.users.enabled is the Boolean true value. 
</p>

<h3>DataSources</h3>

<p>The most popular object to get from JNDI is a object of type <i>javax.sql.DataSource</i>, allowing the developer to obtain JDBC connections to databases. Simple-JNDI supports this out of the box.</p>

<p>There are four mandatory parameters for a DataSource in Simple-JNDI, and four optional parameters (see next section). The mandatory parameters are <i>url, driver, user, password</i>. The following shows an example of a DataSource that will be available under the lookup key <i>application1/ds/TestDS</i>. </p>
<pre>
application1/ds.properties
    TestDS.type=javax.sql.DataSource
    TestDS.driver=org.gjt.mm.mysql.Driver
    TestDS.url=jdbc:mysql://localhost/testdb
    TestDS.user=testuser
    TestDS.password=testing
</pre>
<p>The code to obtain it would be:</p>
<pre>
      InitialContext ctxt = new InitialContext();
      DataSource ds = (DataSource) ctxt.lookup("application1/ds/TestDS");
</pre>
<p>This example uses a delimiter of '/', which must be set with the <i>org.osjava.sj.delimiter</i> property. </p>

<h3>Connection pooling</h3>

<p>Often when using a DataSource you will want to pool the Connections the DataSource is handing out. Simple-JNDI delegates to the Jakarta Commons DBCP project for this feature so you will need commons-dbcp, commons-pool and commons-collections jars in your classpath. </p>
<p>The feature is turned on by adding a sub-parameter of '<i>pool=&lt;pool-name&gt;</i>' in your datasource properties file. For example, <i>ApacheDS.pool=apachePool</i>, will turn DBCP Connection pooling on for the ApacheDS datasource under a name of '<i>apachePool</i>'. </p>
<p>Note: The pool variable used to be a boolean '<i>true</i>' variable, but now a pool name is provided. This is fully backwards compatible as you'll just get a pool name of '<i>true</i>'. </p>
<p>The following parameters are used to configure DBCP:</p>
<table class="bodyTable">
      <tr class="a"><th>Param</th><th>Type</th><th>Default</th></tr>
      <tr class="b"><td>dbcpValidationQuery</td><td>String</td><td>not set</td></tr>
      <tr class="a"><td>dbcpDefaultReadOnly</td><td>true/false</td><td>false</td></tr>
      <tr class="b"><td>dbcpDefaultAutoCommit</td><td>true/false</td><td>true</td></tr>
      <tr class="a"><td>dbcpMaxActive</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_MAX_ACTIVE</td></tr>
      <tr class="b"><td>dbcpWhenExhaustedAction</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION</td></tr>
      <tr class="a"><td>dbcpMaxWait</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_MAX_WAIT</td></tr>
      <tr class="b"><td>dbcpMaxIdle</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_MAX_IDLE</td></tr>
      <tr class="a"><td>dbcpMinIdle</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_MIN_IDLE</td></tr>
      <tr class="b"><td>dbcpTestOnBorrow</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_TEST_ON_BORROW</td></tr>
      <tr class="a"><td>dbcpTestOnReturn</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_TEST_ON_RETURN</td></tr>
      <tr class="b"><td>dbcpTimeBetweenEvictionRunsMillis</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS</td></tr>
      <tr class="a"><td>dbcpNumTestsPerEvictionRun</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN</td></tr>
      <tr class="b"><td>dbcpMinEvictableIdleTimeMillis</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS</td></tr>
      <tr class="a"><td>dbcpTestWhileIdle</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_TEST_WHILE_IDLE</td></tr>
      <tr class="b"><td>dbcpSoftMinEvictableIdleTimeMillis</td><td>See DBCP's GenericObjectPool</td><td>GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS</td></tr>
</table>

<h3>Shared or unshared context?</h3>

<p>Setting <code>org.osjava.sj.jndi.shared=true</code> will put the in-memory JNDI implementation into a mode whereby all InitialContext's share the same memory. By default this is not set, so two separate InitialContext's do not share the same memory and what is bound to one will not be viewable in the other. This could be not what you want when using a DataSource or a connection pool because everytime you call new InitialContext() in your application a new DataSource or a new connection pool is created.</p>

<h3>Dealing with "java:comp/env" (Environment Naming Context, ENC) while loading</h3>

<p>Set the <code>org.osjava.sj.space</code> property. Whatever the property is set to will be automatically prepended to <i>every</i> value loaded into the system. Thus <code>org.osjava.sj.space=java:comp/env</code> simulates the JNDI environment of Tomcat. </p>

<p>Another way to achieve a similar result is putting a default.properties directly under your root. In this file declare all your context objects that should reside under "java:comp/env" by prefixing all properties with "java:comp/env", e. g. "java:comp/env/my/size=186". This way you can set some context objects in "java:comp/env" and other objects in a different name space.</p>

 <p>You could also put a file named "java:comp.properties" in your root directory or name a directory under your root directory "java:comp". But Windows does not like having a : in a filename, so to deal with the : you can use the <code>org.osjava.sj.colon.replace</code> property. If, for example, you choose to replace a <code>:</code> with <code>--</code> (ie <code>org.osjava.sj.colon.replace=--</code>), then you will need a file named <code>java--comp.properties</code>, or a directory named <code>java--comp</code> containing a file "env.properties".</p>


 <h3>Explanatory note</h3>
 
<p>This project is based on old https://github.com/hen/osjava/tree/master/simple-jndi .</p>
<ul>
<li>Several bugs fixed and many new tests added.
<li>Changed the way contexts are shared, because of ContextNotEmptyException with type=javax.sql.DataSource. 
<li>Tests ported to JUnit 4. 
<li>Maven 2/3 support. 
<li>Support for additional basic datatypes (Byte, Short, Integer, Long, Float, Double, Character) in type declaration.
<li>Support for values in quotation marks (quotation marks get removed)
</ul>

<h4>Failed Tests in 0.11.4.1</h4>
<pre>
SimpleJndi
	org.osjava.sj
		SimpleJndiTest.java
			beanNoSetterShared()
			beanNoSetterSharedNoPresetSpace()
			beanNoSetterSharedTwoInitialContexts()
			destroyContextShared()
			quotationMarks()
			sameNamesInDifferentBranchesShared()
			sharedContextsWithDifferentRoots()
			sharedContextWithDataSource()
			typedProperty1()
			closeContext()
	org.osjava.sj.memory
		JndiLoaderSharedTest.java
			testDbcp()
			testDbcp1()
			testDbcp2()
			testDbcp3()
			testDbcp4()
			testDbcpPooltest()
			testDefaultFile()
			testDirectory()
			testMultiValueAttributeBooleans()
			testMultiValueAttributeCharacters()
			testMultiValueAttributeShorts()
			testProperties()
			testTopLevelDataSource()
		JndiLoaderTest.java
			testMultiValueAttributeBooleans()
			testMultiValueAttributeCharacters()
			testMultiValueAttributeMultipleContexts()
			testMultiValueAttributeShorts()
		MemoryContextTestAbstract.java
		SharedMemoryTest.java
			testSjn73()
		SharedMemoryTestSimpleContextFactory.java
			testSjn73()
</pre>        
<h4>Tests in 0.12.0</h4>
<pre>
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.osjava.sj.EncTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 sec - in org.osjava.sj.EncTest
Running org.osjava.sj.loader.convert.ConstructorConverterTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.loader.convert.ConstructorConverterTest
Running org.osjava.sj.loader.JndiLoaderTest
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.177 sec - in org.osjava.sj.loader.JndiLoaderTest
Running org.osjava.sj.loader.util.PropertiesTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.loader.util.PropertiesTest
Running org.osjava.sj.loader.util.ReplaceTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.loader.util.ReplaceTest
Running org.osjava.sj.loader.util.SplitTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.001 sec - in org.osjava.sj.loader.util.SplitTest
Running org.osjava.sj.loader.util.XmlPropertiesTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.loader.util.XmlPropertiesTest
Running org.osjava.sj.memory.DotSeparatorTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.memory.DotSeparatorTest
Running org.osjava.sj.memory.JndiLoaderSharedTest
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.143 sec - in org.osjava.sj.memory.JndiLoaderSharedTest
Running org.osjava.sj.memory.JndiLoaderTest
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.115 sec - in org.osjava.sj.memory.JndiLoaderTest
Running org.osjava.sj.memory.SharedMemorySimpleContextFactoryTest
Tests run: 3, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.042 sec <<< FAILURE! - in org.osjava.sj.memory.SharedMemorySimpleContextFactoryTest
testSjn73(org.osjava.sj.memory.SharedMemorySimpleContextFactoryTest)  Time elapsed: 0.014 sec  <<< ERROR!
javax.naming.NamingException: Invalid subcontext 'path' in context ''
	at org.osjava.sj.memory.SharedMemorySimpleContextFactoryTest.testSjn73(SharedMemorySimpleContextFactoryTest.java:112)

Running org.osjava.sj.memory.SharedMemoryTest
Tests run: 3, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.013 sec <<< FAILURE! - in org.osjava.sj.memory.SharedMemoryTest
testSjn73(org.osjava.sj.memory.SharedMemoryTest)  Time elapsed: 0.012 sec  <<< ERROR!
javax.naming.NamingException: Invalid subcontext 'path' in context ''
	at org.osjava.sj.memory.SharedMemoryTest.testSjn73(SharedMemoryTest.java:112)

Running org.osjava.sj.memory.SlashSeparatorTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.memory.SlashSeparatorTest
Running org.osjava.sj.SimpleContextTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.09 sec - in org.osjava.sj.SimpleContextTest
Running org.osjava.sj.SimpleJndiTest
Expected Exception !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
javax.naming.NamingException: Invalid subcontext 'myContext1' in context 'java:comp/env'
	at org.osjava.sj.jndi.AbstractContext.lookup(AbstractContext.java:251)
	at org.osjava.sj.jndi.AbstractContext.lookup(AbstractContext.java:249)
	at org.osjava.sj.jndi.AbstractContext.lookup(AbstractContext.java:249)
	at org.osjava.sj.jndi.AbstractContext.lookup(AbstractContext.java:284)
	at org.osjava.sj.jndi.DelegatingContext.lookup(DelegatingContext.java:56)
	at javax.naming.InitialContext.lookup(InitialContext.java:392)
	at org.osjava.sj.jndi.DelegatingContext.lookup(DelegatingContext.java:56)
	at javax.naming.InitialContext.lookup(InitialContext.java:392)
	at org.osjava.sj.SimpleJndiTest.sharedContextsWithDifferentRoots(SimpleJndiTest.java:200)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.apache.maven.surefire.junit4.JUnit4Provider.execute(JUnit4Provider.java:367)
	at org.apache.maven.surefire.junit4.JUnit4Provider.executeWithRerun(JUnit4Provider.java:274)
	at org.apache.maven.surefire.junit4.JUnit4Provider.executeTestSet(JUnit4Provider.java:238)
	at org.apache.maven.surefire.junit4.JUnit4Provider.invoke(JUnit4Provider.java:161)
	at org.apache.maven.surefire.booter.ForkedBooter.invokeProviderInSameClassLoader(ForkedBooter.java:290)
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:242)
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:121)
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.136 sec - in org.osjava.sj.SimpleJndiTest
Running org.osjava.sj.Sjn77Test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.Sjn77Test
Running org.osjava.sj.SystemPropertyTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 sec - in org.osjava.sj.SystemPropertyTest

Results :

Tests in error: 
  SharedMemorySimpleContextFactoryTest.testSjn73:112 » Naming Invalid subcontext...
  SharedMemoryTest.testSjn73:112 » Naming Invalid subcontext 'path' in context '...

Tests run: 103, Failures: 0, Errors: 2, Skipped: 0
</pre>
