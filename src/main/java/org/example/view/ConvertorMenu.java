package org.example.view;

import org.example.controller.ConvertorService;
import org.example.model.CurrencyDTO;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ConvertorMenu {
        //Scanner to be used in Menu and helper methods to pass user input to service class
        private final static Scanner scanner = new Scanner(System.in);
        //Business logic
        private final static ConvertorService convertorService = new ConvertorService();

        public static void showMenu() throws IOException, InterruptedException {
            while (true) {
                // Check if file to store data is already created.  If not, create a new file to be written
                convertorService.createHistoryFileIfNotExists();
                System.out.println("\n---->>>  Currency Exchanger Menu <<<----\n");
                System.out.println("1. Convert COP to USD");
                System.out.println("2. Convert USD to COP");
                System.out.println("3. Convert COP to EUR");
                System.out.println("4. Convert EUR to COP");
                System.out.println("5. Convert COP to BRL");
                System.out.println("6. Convert from any currency to any currency");
                System.out.println("7. Show all supported currencies");
                System.out.println("8. Show conversion history");
                System.out.println("9. Delete conversion history");
                System.out.println("0. Exit");
                System.out.print("\nEnter your choice: ");

                int choice = inputChoiceInMenu();

                switch (choice) {
                    case 1:
                        convertCurrencyPairs("COP", "USD");
                        break;
                    case 2:
                        convertCurrencyPairs("USD", "COP");
                        break;
                    case 3:
                        convertCurrencyPairs("COP", "EUR");
                        break;
                    case 4:
                        convertCurrencyPairs("EUR", "COP");
                        break;
                    case 5:
                        convertCurrencyPairs("COP", "BRL");
                        break;
                    case 6:
                        // Consumer any left input
                        scanner.nextLine();
                        // Ask for currency to be converted FROM
                        System.out.println("\nInput the currency to be converted FROM");
                        String convertFrom = scanner.nextLine().toUpperCase().replaceAll("\\s", "");
                        // Ask for currency to be converted TO
                        System.out.println("\nInput the currency to be converted TO");
                        String convertTo = scanner.nextLine().toUpperCase().replaceAll("\\s", "");
                        convertCurrencyPairs(convertFrom, convertTo);
                        break;
                    case 7:
                        String supportedCodesString = convertorService.jsonResponseAllCurrencies();
                        System.out.println("\nAvailable Currencies:\n");
                        System.out.println(formatSupportedCurrencies(supportedCodesString));
                        break;
                    case 8:
                        System.out.println(ConvertorService.readFileAndGetRecords());
                        break;
                    case 9:
                        convertorService.deleteFile();
                        break;
                    case 0:
                        System.out.println("\n---> Exiting Currency Exchanger...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("\nInvalid choice. Please try again.");
                }
            }
        }

    private static void convertCurrencyPairs(String convertFrom, String convertTo) throws IOException, InterruptedException {
        // Ask for user input amount to be converted
        System.out.format("\nInput the amount you want to convert from %s to %s\n", convertFrom, convertTo);
        long amountToConvert = inputAmountToConvert();
        // Get a mapped DTO from JSON response
        CurrencyDTO currencyDTO = convertorService.jsonResponsePairConversion(convertFrom, convertTo, amountToConvert);
        // Show result to used based on mapped response. If there was an error, a warning message will be displayed
        convertorService.showResultToUser(currencyDTO, amountToConvert);
    }


    // Check if user's input is a valid long number to convert. While input is invalid, keep asking for a right amount
    private static long inputAmountToConvert() {
        long amountToConvert;
        while (true) {
            try {
                amountToConvert = scanner.nextLong();
                if (amountToConvert > 0) {
                    break; // Exit the loop if a valid positive amount is entered
                } else {
                    System.out.println("\nPlease enter a positive amount to convert.\n");
                    scanner.nextLine(); // Clear the scanner buffer
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid number.\n");
                scanner.nextLine(); // Clear the scanner buffer
            }
        }
        return amountToConvert;
    }

    private static int inputChoiceInMenu() {
        int menuChoice;
        while (true) {
            try {
                // If scanner doesn't catch any exception then just exit loop
                menuChoice = scanner.nextInt();
                break;
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid number.\n");
                scanner.nextLine(); // Clear the scanner buffer
            }
        }
        return menuChoice;
    }

    private static String formatSupportedCurrencies(String supportedCurrenciesStr){
        // Remove surrounding square brackets and quotes
        supportedCurrenciesStr = supportedCurrenciesStr.substring(2, supportedCurrenciesStr.length() - 2);
        // Split the String based on "],[" separators
        String[] currencyPairs = supportedCurrenciesStr.split("\\],\\[");

        StringBuilder formattedCurrencies = new StringBuilder();
        for (String currencyPair : currencyPairs) {
            // Split each pair to extract code and country
            String[] codeAndCountry = currencyPair.split(",");
            String code = codeAndCountry[0].replaceAll("^\"|\"$", ""); // Remove quotes
            String country = codeAndCountry[1].replaceAll("^\"|\"$", ""); // Remove quotes
            formattedCurrencies.append(String.format("[%s, %s]\n", code, country));
        }

        return formattedCurrencies.toString();
    }
}
