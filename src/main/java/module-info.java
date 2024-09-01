module dedrmtools {
    requires com.google.gson;
    requires org.apache.commons.text;
    requires org.tukaani.xz;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    opens cloud.tavitian.dedrmtools to com.google.gson;
    opens cloud.tavitian.dedrmtools.kindlekeys to com.google.gson;
    exports cloud.tavitian.dedrmtools;
}
