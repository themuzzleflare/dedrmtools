/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.util.Set;

import static cloud.tavitian.dedrmtools.Util.commaSeparatedStringToSanitisedSet;

public final class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            System.exit(1);
        }

        String infile = null;
        String outdir = null;

        Set<String> kdatabases = null;
        Set<String> pids = null;
        Set<String> serials = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-k") || args[i].equals("--database"))
                kdatabases = commaSeparatedStringToSanitisedSet(args[++i]);
            else if (args[i].equals("-p") || args[i].equals("--pid"))
                pids = commaSeparatedStringToSanitisedSet(args[++i]);
            else if (args[i].equals("-s") || args[i].equals("--serial"))
                serials = commaSeparatedStringToSanitisedSet(args[++i]);
            else if (args[i].equals("-v") || args[i].equals("--verbose")) Debug.setEnabled(true);
            else if (args[i].equals("-h") || args[i].equals("--help")) {
                usage();
                System.exit(0);
            } else if (infile == null) infile = args[i];
            else if (outdir == null) outdir = args[i];
            else {
                System.err.println("Too many arguments");
                usage();
                System.exit(1);
            }
        }

        if (infile == null || outdir == null) {
            System.err.println("Not enough arguments");
            usage();
            System.exit(1);
        }

        Debug.printf("infile: %s%n", infile);
        Debug.printf("outdir: %s%n", outdir);
        Debug.printf("kdatabases: %s%n", kdatabases);
        Debug.printf("pids: %s%n", pids);
        Debug.printf("serials: %s%n", serials);

        DeDRM.decryptBook(infile, outdir, kdatabases, serials, pids);
    }

    private static void usage() {
        System.out.println("Usage: java -jar DeDRM.jar [options] [infile] [outdir]");
        System.out.println("Options:");
        System.out.println("  -k, --database <kdatabase1,kdatabase2,...>  Use the specified Kindle database files");
        System.out.println("  -p, --pid <pid1,pid2,...>                   Use the specified Kindle PIDs");
        System.out.println("  -s, --serial <serial1,serial2,...>          Use the specified Kindle serial numbers");
        System.out.println("  -v, --verbose                               Enable verbose logging");
        System.out.println("  -h, --help                                  Display this help message");
    }
}
