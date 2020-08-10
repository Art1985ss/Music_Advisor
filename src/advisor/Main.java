package advisor;

import java.util.Arrays;

public class Main {
    private static String accessPoint = "https://accounts.spotify.com";
    private static String resource = "https://api.spotify.com";
    private static int pages = 5;

    public static void main(String[] args) {
        processArgs(args);
        Application application = new Application(accessPoint, resource, pages);
        application.execute();
    }

    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                accessPoint = args[i + 1] == null ? accessPoint : args[i + 1];
                continue;
            }
            if ("-resource".equals(args[i])) {
                resource = args[i + 1];
                continue;
            }
            if ("-page".equals(args[i])) {
                pages = Integer.parseInt(args[i + 1]);
            }
        }
    }
}
