### Install Oxalis Outbound (Standalone)

Oxalis SimpleSender does not come with an extension folder. So we need to add the extension logic that where define in the _run_ scripts our self.

To run our combined application all we need to do is to run the following command (This command assumes we are standing in our base folder):
<pre>
java -classpath "standalone/*" network.oxalis.ng.Main [followed by the argument like -f c:\some-invoice.xml]
</pre>

All this command does is to tell Java to load the content of both folders, then execute the logic in "_network.oxalis.ng.Main_" (which is the starting point of the Standalone application).
By looking into the run scripts of Oxalis NG Server form our previous section we can see that this is in fact the same approach that is used there.
