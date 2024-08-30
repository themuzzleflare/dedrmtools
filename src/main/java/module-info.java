module dedrmtools {
    requires com.google.gson;
    requires org.apache.commons.text;
    requires org.tukaani.xz;

    opens cloud.tavitian.dedrmtools to com.google.gson;
    opens cloud.tavitian.dedrmtools.kindlekeys to com.google.gson;
    exports cloud.tavitian.dedrmtools;
}
