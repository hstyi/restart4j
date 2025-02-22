# restart4j

All Binary `restarter` from: https://github.com/JetBrains/intellij-community

## pom.xml

```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://www.jitpack.io</url>
  </repository>
</repositories>

<dependency>
    <groupId>com.github.hstyi</groupId>
    <artifactId>restart4j</artifactId>
    <version>0.0.1</version>
</dependency>
```

## usage

```java
// Starting notepad after the JVM process has shut down
com.github.hstyi.restart4j.Restarter.restart(new String[]{"notepad"});
```
