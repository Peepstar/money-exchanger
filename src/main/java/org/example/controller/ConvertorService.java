package org.example.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.CurrencyDTO;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConvertorService {
    final private static String apiKey = "ae61cdde469d6fb9b00448e5";
    final private static String baseUrlString = "https://v6.exchangerate-api.com/v6/" + apiKey;
    //  Create a ObjectMapper from Jackson library to data bind JSON response to CurrencyDTO. Configure map to ignore properties not present in DTO
    final private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public CurrencyDTO jsonResponsePairConversion(String convertFrom, String convertTo, long amountToConvert) throws IOException, InterruptedException {
        // Create a new HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        // Build the GET request using HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                //  Create URL with currencies provided by Menu
                .uri(URI.create(baseUrlString + "/pair" + "/" + convertFrom + "/" + convertTo + "/" + amountToConvert))
                .GET()
                .build();


        // Send the request and get the response to return
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Check for successful response. If not, return error message and return null
        if (!(response.statusCode() == 200)) {
            System.out.println("\nError retrieving data: Status code " + response.statusCode());
            return null;
        }
        return objectMapper.readValue(response.body(), CurrencyDTO.class);
    }

    public String jsonResponseAllCurrencies() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // Build the GET request using HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                //  Create URL to get all codes
                .uri(URI.create(baseUrlString + "/codes"))
                .GET()
                .build();

        // Send the request and get the response to return
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Check for successful response. If not, return error message and return null
        if (!(response.statusCode() == 200)) {
            System.out.println("\nError retrieving data: Status code " + response.statusCode());
            return null;
        }
        // Get JSON body and only node needed for supported codes
        String jsonResponse = response.body();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        return jsonNode.get("supported_codes").toString();
    }


    //  Amount to be converted is passed in a7s parameter since it is not included in HTTP response from API
    public void showResultToUser(CurrencyDTO mappedDTO, long amountConverted){
        // Check if mappedDTO is null (it is null when an error occurs with the HTTP request)
        if(mappedDTO == null){
            System.out.println("\nUnable to complete conversion due to an error with the server. Please try again and check that you are using correct conversion codes\n");
        }else {
            // If DTO is not null then return information to user and save query to file
            System.out.format("\nThe conversion of %d %s is equal to = %s %s\n\n", amountConverted, mappedDTO.baseCode(),
                    mappedDTO.conversionResult(), mappedDTO.targetCode());

            System.out.println("   --> Additional Data <--   ");
            System.out.println("- Base code: " + mappedDTO.baseCode());
            System.out.println("- Target code: " + mappedDTO.targetCode());
            System.out.println("- Conversion rate:  1 " + mappedDTO.baseCode() + " = " + mappedDTO.conversionRate() + " " + mappedDTO.targetCode());
            // Formatting date to be more readable and avoiding unnecessary data
            int indexOfPlus = mappedDTO.timeLastUpdate().indexOf('+');
            if (indexOfPlus > 0) {
                // Extract the part before the "+" sign and print formatted time
                String formattedTime = mappedDTO.timeLastUpdate().substring(0, indexOfPlus);
                System.out.println("- Time of last update: " + formattedTime +  "\n");
            }
            //  Now add the conversion to history of conversions file
            addNewEntryToFile(mappedDTO);
        }
    }

    public void createHistoryFileIfNotExists(){
        File currencyFile = new File("currency-history.txt");
        if(!currencyFile.exists()){
            // Create a new file in case that doesn't exist.
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("currency-history.txt"));
                bufferedWriter.close();
            } catch (IOException e) {
                System.out.println("File doesn't exist\n");
            }
        }
    }

    private static void addNewEntryToFile(CurrencyDTO currencyDTO){
        // Read existing file to re-write data
        String currentRecords = readFileAndGetRecords();
        // Build a String from currencyDTO and current time(formatted) to be saved in file
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        StringBuilder newHistoryEntry = new StringBuilder();
        newHistoryEntry.append("From ").append(currencyDTO.baseCode()).append(" To ").append(currencyDTO.targetCode())
                .append(". Conversion = ").append(currencyDTO.conversionRate())
                .append(" Time: ").append(dtf.format(LocalDateTime.now())).append("\n");

        // Write existing records and new record to file
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("currency-history.txt"));
            bufferedWriter.write(currentRecords);
            bufferedWriter.write(newHistoryEntry.toString());
            bufferedWriter.close();
            System.out.println("---> History updated!\n");
        } catch (IOException e) {
            System.out.println("File doesn't exist\n");
        }
    }

    public static String readFileAndGetRecords(){
        // Read existing file and assign it to StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        try{
            // Create reader and read each line in the file
            BufferedReader bufferedReader = new BufferedReader(new FileReader("currency-history.txt"));
            String currLine;
            while((currLine = bufferedReader.readLine()) != null){
                // assign current line of the file to StringBuilder
                stringBuilder.append(currLine).append("\n");
            }
            bufferedReader.close();
        }catch (IOException e){
            System.out.println("File doesn't exist\n");
        }
        // Return builder as String
        return stringBuilder.toString();
    }

    public void deleteFile(){
        System.out.println("Algo paso");
        File currencyFile = new File("currency-history.txt");
        if(currencyFile.isFile()){
            boolean deletedFile = currencyFile.delete();
            if(deletedFile){
                System.out.println("---> File was successfully deleted!\n");
            }else{
                System.out.println("NOSE");
            }
        }else{
            System.out.println("File doesn't exists\n");
        }
    }

}
