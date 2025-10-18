This file was created from the CVS export from [TinyTimeTracker](https://sourceforge.net/projects/tinytimetracker/). But, by Russell Black.
There's no guarantee of it working, secure, or without bugs, but maybe I might be able to resurrect it.

Nice to meet you and please fork if you feel it's something you can be involved in.

To compile, just run [ant](https://ant.apache.org) against the tinytimetracker (sub)folder. Current targets from original source:
- ```ant compile```
- ```ant jar```
- ```ant clean```
- ```ant run```

"deploy" and the Java webstart items have been removed and don't work anyway.

This compiles and runs on Java 25. It has not (yet) been tested with Java 8+ (but it should run).

added libraries:
```
- commons-io-2.13.0.jar
- log4j-api-2.20.0.jar
- log4j-core-2.20.0.jar
- poi-5.4.1.jar
  ```
