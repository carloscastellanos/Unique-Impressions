
With this example project you should be able to compile the HelloWorld class 
simply by choosing Build from the Build menu or pressing command-B.  The build 
proceeds with the following steps:

1. The HelloWorld.java source code is compiled into a java class.  The 
HelloWorld.class file will be put inside a subfolder called "build/classes".  

2. This new HelloWorld.class file is copied to the 
/Library/Application Support/Cycling '74/java/classes folder.  Note that this 
overwrites any HelloWorld.class that already exists in that folder.

3. The build subfolder is deleted.  This is done as a manual "Clean All", which 
seems to be necessary for any changes in the source code to actually take 
effect.  


This is the most convenient solution I've found for building mxj classes in 
Xcode.  I've not yet been able to get debugging to work.  If you can suggest 
any improvements to this example project, please contact me!

To modify this project for use with your own class (let's call it YourClass),

1. Make a copy of the HelloWorld project folder.  Rename HelloWorld.xcode 
YourClass.xcode.

2. To start from the HelloWorld source code, select HelloWorld.java in the 
Groups & Files browser on the left and use the File:Rename menu option to 
rename it to MyClass.java.  In the source, replace all instances of "HelloWorld" 
with "YourClass".
Alternatively, to use a new YourClass.java source file, select HelloWorld.java 
in the Groups & Files browser and use the delete key to remove it from the 
project.  Drag and drop YourClass.java to where HelloWorld.java was.

3. Select the HelloWorld target in the Groups & Files browser and use 
File:Rename to change its name to YourClass.



bbn@cycling74.com
2004.03.10