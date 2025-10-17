This file was created from the CVS export from [TinyTimeTracker](https://sourceforge.net/projects/tinytimetracker/). But, not by me.
There's no guarantee of it working, secure, or without bugs, but maybe I might be able to resurrect it.

Nice to meet you and please fork if you feel it's something you can be involved in.

This as-is project probably won't work as the original project had hard-coded sourceforge.net hosting for weblaunch.

I'd like to see this work in some way that can run locally.

To compile, just run ant against the tinytimetracker folder. Current targets from original source:
- ```ant compile```
- ```ant jar```
- ```ant clean```

"deploy" and the Java webstart items have been removed and don't work anyway.

As of this writing, the app does not compile. There was a hiccup because legacy cruft already worked. 
