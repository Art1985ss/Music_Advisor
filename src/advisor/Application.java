package advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Application {
    private final Scanner scanner = new Scanner(System.in);
    private final ApiManager apiManager;
    private final int pages;
    private boolean authorized = false;
    private List<String> entries;
    private int totalPages = 0;
    private int currentPage = 1;

    public Application(String accessPoint, String resource, int pages) {
        apiManager = new ApiManager(accessPoint, resource);
        this.pages = pages;
        entries = new ArrayList<>();
    }

    public void execute() {
        String userInput;
        boolean run = true;
        do {
            userInput = scanner.next();
            try {
                run = processUserInput(userInput);
            } catch (AdvisorException e) {
                System.out.println(e.getMessage());
            }
        } while (run);
    }

    private boolean processUserInput(String input) throws AdvisorException {
        UserRequest userRequest;
        try {
            userRequest = UserRequest.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AdvisorException("Invalid option!");
        }
        switch (userRequest) {
            case AUTH:
                auth();
                break;
            case FEATURED:
            case NEW:
            case CATEGORIES:
                if (authorized) {
                    entries = apiManager.getInfo(userRequest, null);
                } else {
                    throw new AdvisorException("Please, provide access for application.");
                }
                updatePages();
                next();
                break;
            case PLAYLISTS:
                String categoryName = scanner.nextLine();
                if (authorized) {
                    entries = apiManager.getInfo(userRequest, categoryName);
                } else {
                    throw new AdvisorException("Please, provide access for application.");
                }
                updatePages();
                next();
                break;
            case NEXT:
                next();
                break;
            case PREV:
                previous();
                break;
            case EXIT:
                System.out.println("---GOODBYE!---");
                return false;
            default:
        }
        return true;
    }

    private void auth() {
        apiManager.start();
        authorized = true;
        try {
            apiManager.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("---SUCCESS---");
    }

    private void next() {
        if (currentPage * pages + pages > entries.size()) {
            System.out.println("No more pages");
        } else {
            currentPage++;
        }
        printPage();
    }

    private void previous() {
        if (currentPage - 1 < 1) {
            System.out.println("No more pages");
        } else {
            currentPage--;
        }
        printPage();
    }

    private void pagesInfo() {
        System.out.println("---PAGE " + currentPage + " OF " + totalPages + "---");
    }

    private void updatePages() {
        totalPages = entries.size() / pages;
        currentPage = 0;
    }

    private void printPage() {
        for (int i = (currentPage - 1) * pages; i < pages * currentPage && i < entries.size(); i++) {
            System.out.println(entries.get(i));
        }
        pagesInfo();
    }
}
