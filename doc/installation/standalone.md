### Install Oxalis Outbound (Standalone)

Oxalis SimpleSender does not come with an extension folder. So we need to add the extension logic that where define in the _run_ scripts our self.

We make a base folder named oxalis-standalone-as4 with two sub folders:
<pre>
oxalis-standalone-as4/    <-- Base folder, we will run our commands from here
├── standalone/    <-- We will putt our regular <em>Oxalis Standalone</em> application here...
│   ├── oxalis-standalone.jar
│   ├── posibly-other.jar
│   └── ...
└── as4/    <-- ...and our <em>AS4</em> extension here
    ├── oxalis-as4.jar
    ├── many-other.jar
    └── ...
</pre>

To run our combined application all we need to do is to run the following command (This command assumes we are standing in our base folder):
<pre>
java -classpath "standalone/*;as4/*" network.oxalis.ng.Main [followd by the argument like -f c:\some-invoice.xml]
</pre>

All this command does is to tell Java to load the content of both folders, then execute the logic in "_eu.sendregning.oxalis.Main_" (which is the starting point of the Standalone application).
By looking into the run scripts of Oxalis Server form our previous section we can see that this is in fact the same approach that is used there.
