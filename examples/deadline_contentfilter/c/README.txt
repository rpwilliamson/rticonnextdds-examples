
Purpose
=======
PUBLISHER:
The Publisher adopts a 1.5 second deadline, promising to write samples at least this fast. Note that deadlines apply to each instance individually. After 15 seconds, we stop writing to the 2nd instance, and we get the on_offered_deadline_missed callback.

SUBSCRIBER:
The Subscriber requests a 2 second deadline, and prints out the offending instance when this deadline is missed. Note the interaction with filtering. After 10 seconds, we set up a filter that ignores the 2nd instance. Even though the publisher is still sending samples at this point, the instance is flagged as missing its deadline.

With a time-based filter, however, if the requested deadline is less than the minimum separation, the QoS is considered incompatible.

Building
=======

Before compiling or running the example, make sure the environment variable NDDSHOME is set to the directory where your version of RTI Data Distribution Service is installed.

Run rtiddsgen with the -example option and the target architecture of your choice (for example, i86Win32VS2005). The RTI Data Distribution Service Getting Started Guide describes this process in detail. Follow the same procedure to generate the code and build the examples. Do not use the -replace option.

After running rtiddsgen like this...

C:\local\deadline_contentfilter\c> rtiddsgen -language C -example i86Win32VS2005 deadline_contentfilter.idl

...you will see messages that look like this:

File C:\local\deadline_contentfilter\c\deadline_contentfilter_subscriber.c already exists and will not be replaced with updated content. If you would like to get a new file with the new content, either remove this file or supply -replace option.
File C:\local\deadline_contentfilter\c\deadline_contentfilter_publisher.c already exists and will not be replaced with updated content. If you would like to get a new file with the new content, either remove this file or supply -replace option.
File c:\local\deadline_contentfilter\c\USER_QOS_PROFILES.xml already exists and will not be replaced with updated content. If you would like to get a new file with the new content, either remove this file or supply -replace option.

This is normal and is only informing you that the subscriber/publisher code has not been replaced, which is fine since all the source files for the example are already provided.

Running
=======
In two separate command prompt windows for the publisher and subscriber, navigate to the objs/<arch> directory and run these commands:

Windows systems:

    * deadline_contentfilter_publisher.exe <domain#> 13
    * deadline_contentfilter_subscriber.exe <domain#> 15

UNIX systems:

    * ./deadline_contentfilter_publisher <domain#> 13
    * ./deadline_contentfilter_subscriber <domain#> 15

The applications accept two arguments:

   1. The <domain #>. Both applications must use the same domain # in order to communicate. The default is 0.
   2. How long the examples should run, measured in samples for the publisher and sleep periods for the subscriber. A value of '0' instructs the application to run forever; this is the default.

While generating the output below, we used values that would capture the most interesting behavior.

Publisher Output
=============
Writing instance0, x = 1, y = 1

Subscriber Output
==============
@ t=3.00s, Instance0: <1,1>