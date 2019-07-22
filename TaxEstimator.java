/*
	Tax Estimator created by Devan Dutta
	July 2019
	Note: This app will calculate estimated federal and state taxes for the 50 U.S. States
*/

import java.io.FileNotFoundException;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

enum FilingStatus {
    Single,
    Married_Joint,
    Married_Separate,
    Head_of_Household
}


public class TaxEstimator {
    FilingStatus filingStatus;
    JSONObject federal_tax_data;
    JSONObject selected_state_tax_data;
    HashMap<String, ArrayList<Bracket>> federal_brackets;
    HashMap<String, ArrayList<Bracket>> selected_state_brackets;
    HashMap<String, String> state_abbr_to_file_name;

    public TaxEstimator() {
        federal_brackets = new HashMap<String, ArrayList<Bracket>>();
        selected_state_brackets = new HashMap<String, ArrayList<Bracket>>();
        state_abbr_to_file_name = new HashMap<String, String>();
        fill_state_abbr_to_file_name_map();
    }

    public void fill_state_abbr_to_file_name_map() {
        state_abbr_to_file_name.put("AL", "alabama");
        state_abbr_to_file_name.put("AK", "alaska");
        state_abbr_to_file_name.put("AZ", "arizona");
        state_abbr_to_file_name.put("AR", "arkansas");
        state_abbr_to_file_name.put("CA", "california");
        state_abbr_to_file_name.put("CO", "colorado");
        state_abbr_to_file_name.put("CT", "connecticut");
        state_abbr_to_file_name.put("DE", "delaware");
        state_abbr_to_file_name.put("DC", "district_of_columbia");
        state_abbr_to_file_name.put("FL", "florida");
        state_abbr_to_file_name.put("GA", "georgia");
        state_abbr_to_file_name.put("HI", "hawaii");
        state_abbr_to_file_name.put("ID", "idaho");
        state_abbr_to_file_name.put("IL", "illinois");
        state_abbr_to_file_name.put("IN", "indiana");
        state_abbr_to_file_name.put("IA", "iowa");
        state_abbr_to_file_name.put("KS", "kansas");
        state_abbr_to_file_name.put("KY", "kentucky");
        state_abbr_to_file_name.put("LA", "louisiana");
        state_abbr_to_file_name.put("ME", "maine");
        state_abbr_to_file_name.put("MD", "maryland");
        state_abbr_to_file_name.put("MA", "massachusetts");
        state_abbr_to_file_name.put("MI", "michigan");
        state_abbr_to_file_name.put("MN", "minnesota");
        state_abbr_to_file_name.put("MS", "mississippi");
        state_abbr_to_file_name.put("MO", "missouri");
        state_abbr_to_file_name.put("MT", "montana");
        state_abbr_to_file_name.put("NE", "nebraska");
        state_abbr_to_file_name.put("NV", "nevada");
        state_abbr_to_file_name.put("NH", "new_hampshire");
        state_abbr_to_file_name.put("NJ", "new_jersey");
        state_abbr_to_file_name.put("NM", "new_mexico");
        state_abbr_to_file_name.put("NY", "new_york");
        state_abbr_to_file_name.put("NC", "north_carolina");
        state_abbr_to_file_name.put("ND", "north_dakota");
        state_abbr_to_file_name.put("OH", "ohio");
        state_abbr_to_file_name.put("OK", "oklahoma");
        state_abbr_to_file_name.put("OR", "oregon");
        state_abbr_to_file_name.put("PA", "pennsylvania");
        state_abbr_to_file_name.put("RI", "rhode_island");
        state_abbr_to_file_name.put("SC", "south_carolina");
        state_abbr_to_file_name.put("SD", "south_dakota");
        state_abbr_to_file_name.put("TN", "tennessee");
        state_abbr_to_file_name.put("TX", "texas");
        state_abbr_to_file_name.put("UT", "utah");
        state_abbr_to_file_name.put("VT", "vermont");
        state_abbr_to_file_name.put("VA", "virginia");
        state_abbr_to_file_name.put("WA", "washington");
        state_abbr_to_file_name.put("WV", "west_virginia");
        state_abbr_to_file_name.put("WI", "wisconsin");
        state_abbr_to_file_name.put("WY", "wyoming");
    }


    public class Bracket {
        Long bracketLow;
        Long bracketHigh;
        double taxRate;

        public String toString() {
            return String.format("Bracket: {taxRate: %f, bracketLow: %d, bracketHigh: %d}", taxRate, bracketLow, bracketHigh);
        }
    }

    public static void printFederalTaxFilingOptions() {
        System.out.println("The following options exist for federal tax filings.");
        System.out.println("[1]\tSingle");
        System.out.println("[2]\tMarried, filing jointly");
        System.out.println("[3]\tMarried, filing separately");
        System.out.println("[4]\tHead of household");

        System.out.printf("Please select the filing status that applies to you: ");
    }

    public void getFederalFilingStatus() {
        Scanner filingStatusInput = new Scanner(System.in);

        boolean filingStatusSelected = false;

        do {
            TaxEstimator.printFederalTaxFilingOptions();
            int filingStatusCode = filingStatusInput.nextInt();

            switch (filingStatusCode) {
                case 1:
                    filingStatus = FilingStatus.Single;
                    filingStatusSelected = true;
                    break;
                case 2:
                    filingStatus = FilingStatus.Married_Joint;
                    filingStatusSelected = true;
                    break;
                case 3:
                    filingStatus = FilingStatus.Married_Separate;
                    filingStatusSelected = true;
                    break;
                case 4:
                    filingStatus = FilingStatus.Head_of_Household;
                    filingStatusSelected = true;
                    break;
                default:
                    System.out.println("Invalid entry. Please select from one of the available options:");
            }
        } while (!filingStatusSelected);
    }

    public void parse_federal_brackets_from_json() {
        // Use the federal_tax_data JSONObject to read all the brackets for each filing status and store them in
        // the federal_brackets hash map

        Iterator iterator = federal_tax_data.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            JSONArray income_brackets = (JSONArray) ((JSONObject) federal_tax_data.get(key)).get("income_tax_brackets");
            ArrayList<Bracket> brackets_for_specific_status = new ArrayList<Bracket>();

            for (int bracket_object_index = 0; bracket_object_index < income_brackets.size(); bracket_object_index++) {
                JSONObject bracket = (JSONObject) income_brackets.get(bracket_object_index);

                Bracket newBracket = new Bracket();

                // Get the low part of the bracket
                // The income brackets from taxee start the next level at the upper end of the previous bracket
                // So increment by 1

                if (bracket_object_index == 0) {
                    newBracket.bracketLow = 0L;
                }
                else {
                    newBracket.bracketLow = ((Long) bracket.get("bracket")) + 1;
                }

                // Get the high part of the bracket
                newBracket.bracketHigh = 0L;
                if (bracket_object_index != income_brackets.size() - 1) {
                    newBracket.bracketHigh = (Long) ((JSONObject) income_brackets.get(bracket_object_index + 1)).get("bracket");
                }

                if (bracket.get("marginal_rate") instanceof Double) {
                    newBracket.taxRate = (double) bracket.get("marginal_rate");
                }

                if (bracket.get("marginal_rate") instanceof Long) {
                    newBracket.taxRate = ((Long) bracket.get("marginal_rate")).doubleValue();
                }

                brackets_for_specific_status.add(newBracket);
            }

            federal_brackets.put(key, brackets_for_specific_status);
            // System.out.println(String.format("Added {%s: %s} to federal_brackets", key, brackets_for_specific_status));
        }

        //System.out.println("Finished parsing all JSON data. Added the following to federal_brackets hash map:");
        //System.out.println(federal_brackets);
    }

    public void parse_selected_state_brackets_from_json() {
        // Use the selected_state_tax_data JSONObject to read all the brackets for each filing status and store them in
        // the selected_state_brackets hash map

        Iterator iterator = selected_state_tax_data.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            JSONArray income_brackets = (JSONArray) ((JSONObject) selected_state_tax_data.get(key)).get("income_tax_brackets");
            ArrayList<Bracket> brackets_for_specific_status = new ArrayList<Bracket>();

            for (int bracket_object_index=0; bracket_object_index < income_brackets.size(); bracket_object_index++) {
                JSONObject bracket = (JSONObject) income_brackets.get(bracket_object_index);

                Bracket newBracket = new Bracket();

                // Get the low part of the bracket
                // The income brackets from taxee start the next level at the upper end of the previous bracket
                // So increment by 1

                if (bracket_object_index == 0) {
                    newBracket.bracketLow = 0L;
                }
                else {
                    newBracket.bracketLow = ((Long) bracket.get("bracket")) + 1;
                }

                // Get the high part of the bracket
                newBracket.bracketHigh = 0L;
                if (bracket_object_index != income_brackets.size() - 1) {
                    newBracket.bracketHigh = (Long) ((JSONObject) income_brackets.get(bracket_object_index + 1)).get("bracket");
                }

                //System.out.println("bracket marginal rate type: " + bracket.get("marginal_rate").getClass().getName());
                //System.out.println("bracket marginal rate: " + bracket.get("marginal_rate"));

                if (bracket.get("marginal_rate") instanceof Double) {
                    newBracket.taxRate = (double) bracket.get("marginal_rate");
                }

                if (bracket.get("marginal_rate") instanceof Long) {
                    newBracket.taxRate = ((Long) bracket.get("marginal_rate")).doubleValue();
                }

                brackets_for_specific_status.add(newBracket);
            }

            selected_state_brackets.put(key, brackets_for_specific_status);
        }
    }

    public static String filingStatusToKey(FilingStatus filingStatus) {
        String federal_brackets_key = "";
        if (filingStatus == FilingStatus.Single) {
            federal_brackets_key = "single";
        }

        else if(filingStatus == FilingStatus.Married_Joint) {
            federal_brackets_key = "married";
        }

        else if(filingStatus == FilingStatus.Married_Separate) {
            federal_brackets_key = "married_separately";
        }

        else if(filingStatus == FilingStatus.Head_of_Household) {
            federal_brackets_key = "head_of_household";
        }

        else {
            System.out.println("Invalid filing status entered.");
            System.exit(-1);
        }

        return federal_brackets_key;
    }

    public double calculateFederalTaxes(FilingStatus filingStatus, int taxableIncome) {
        double taxValue = 0.0;

        // Iterate through all brackets that are less than the taxableIncome and add their contributions to the tax value
        String federal_brackets_key = TaxEstimator.filingStatusToKey(filingStatus);

        ArrayList<Bracket> brackets_for_filing_status = federal_brackets.get(federal_brackets_key);

        for (int bracket_index = 0; bracket_index < brackets_for_filing_status.size(); bracket_index++) {
            Bracket currentBracket = brackets_for_filing_status.get(bracket_index);

            // If the taxable income is higher than the bracketHigh, include the entire bracket's contribution
            // Else, calculate the difference between taxable income and bracketLow

            if (taxableIncome > currentBracket.bracketHigh) {
                // If you are at the last bracket, then bracketHigh will be set to 0 by default, so use taxableIncome - bracketLow
                if (bracket_index == (brackets_for_filing_status.size() - 1)) {
                    taxValue = taxValue + (currentBracket.taxRate/100) * (taxableIncome - currentBracket.bracketLow);
                }

                // Else, you are at an intermediate bracket, so just take the whole bracket's tax contribution
                else {
                    taxValue = taxValue + (currentBracket.taxRate/100) * (currentBracket.bracketHigh - currentBracket.bracketLow);
                }
            }

            else {
                taxValue = taxValue + (currentBracket.taxRate/100) * (taxableIncome - currentBracket.bracketLow);
                break;
            }
        }

        return taxValue;
    }

    public double calculateSelectedStateTaxes(FilingStatus filingStatus, int taxableIncome) {
        double taxValue = 0.0;

        // Iterate through all brackets that are less than the taxableIncome and add their contributions to the tax value
        String selected_state_brackets_key = TaxEstimator.filingStatusToKey(filingStatus);

        ArrayList<Bracket> brackets_for_filing_status = selected_state_brackets.get(selected_state_brackets_key);

        for (int bracket_index = 0; bracket_index < brackets_for_filing_status.size(); bracket_index++) {
            Bracket currentBracket = brackets_for_filing_status.get(bracket_index);

            // If the taxable income is higher than the bracketHigh, include the entire bracket's contribution
            // Else, calculate the difference between taxable income and bracketLow

            if (taxableIncome > currentBracket.bracketHigh) {
                // If you are at the last bracket, then bracketHigh will be set to 0 by default, so use taxableIncome - bracketLow
                if (bracket_index == (brackets_for_filing_status.size() - 1)) {
                    taxValue = taxValue + (currentBracket.taxRate/100) * (taxableIncome - currentBracket.bracketLow);
                }

                // Else, you are at an intermediate bracket, so just take the whole bracket's tax contribution
                else {
                    taxValue = taxValue + (currentBracket.taxRate/100) * (currentBracket.bracketHigh - currentBracket.bracketLow);
                }
            }

            else {
                taxValue = taxValue + (currentBracket.taxRate/100) * (taxableIncome - currentBracket.bracketLow);
                break;
            }
        }

        return taxValue;
    }

    public void printFederalTaxBracketTable(FilingStatus filingStatus) {
        System.out.println("Federal Tax Brackets for " + filingStatus + " filing status");

        String federal_brackets_key = TaxEstimator.filingStatusToKey(filingStatus);

        // Output each tax bracket for the given filing status, by using the federal_brackets HashMap
        ArrayList<Bracket> brackets_for_filing_status = federal_brackets.get(federal_brackets_key);
        System.out.printf("%-20s%s%n", "Tax Rate", "Income Bracket");
        for (int bracket_index = 0; bracket_index < brackets_for_filing_status.size(); bracket_index++) {
            Bracket currentBracket = brackets_for_filing_status.get(bracket_index);
            if (currentBracket.bracketHigh != 0L)
            {
                // This format means:
                // 1. Left justify tax rate and make the size of the field 20 characters
                // 2. Put bracket info after it to the right
                //Note: To print a %, type "%%", because % is used to escape %
                System.out.printf("%-20s%s%n", String.format(currentBracket.taxRate + "%%"), String.format("$%d - $%d", currentBracket.bracketLow, currentBracket.bracketHigh));
            }

            else {
                // This format means:
                // 1. Left justify tax rate and make the size of the field 20 characters
                // 2. Put bracket info after it to the right
                // Note: %% is used to print a % (you use % to escape %)
                System.out.printf("%-20s%s%n", String.format(currentBracket.taxRate + "%%"), String.format("$%d+", currentBracket.bracketLow));
            }
        }
    }

    public void printSelectedStateTaxBracketTable(FilingStatus filingStatus) {
        System.out.println("State Tax Brackets for " + filingStatus + " filing status");

        String selected_state_brackets_key = TaxEstimator.filingStatusToKey(filingStatus);

        //Output each tax bracket for the given filing status, by using the selected_state_brackets HashMap

        ArrayList<Bracket> brackets_for_filing_status = selected_state_brackets.get(selected_state_brackets_key);
        System.out.printf("%-20s%s%n", "Tax Rate", "Income Bracket");
        for (int bracket_index = 0; bracket_index < brackets_for_filing_status.size(); bracket_index++) {
            Bracket currentBracket = brackets_for_filing_status.get(bracket_index);
            if (currentBracket.bracketHigh != 0L)
            {
                // This format means:
                // 1. Left justify tax rate and make the size of the field 20 characters
                // 2. Put bracket info after it to the right
                //Note: To print a %, type "%%", because % is used to escape %
                System.out.printf("%-20s%s%n", String.format(currentBracket.taxRate + "%%"), String.format("$%d - $%d", currentBracket.bracketLow, currentBracket.bracketHigh));
            }

            else {
                // This format means:
                // 1. Left justify tax rate and make the size of the field 20 characters
                // 2. Put bracket info after it to the right
                // Note: %% is used to print a % (you use % to escape %)
                System.out.printf("%-20s%s%n", String.format(currentBracket.taxRate + "%%"), String.format("$%d+", currentBracket.bracketLow));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the tax estimator!");

        // Determine the year
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        System.out.println("Checking if you have the most recent federal tax brackets for this year (" + year + ").");

        try {
            FileReader reader = new FileReader("federal_brackets.json");
            System.out.println("All good! You have the most recent tax data.");
        }
        catch (FileNotFoundException e) {
            System.out.println("No tax bracket data found!");

            System.out.println("Downloading most recent federal tax brackets from taxee github source.");
            System.out.println("Source: https://github.com/taxee/taxee-tax-statistics");
            System.out.println();

            String url = "https://raw.githubusercontent.com/taxee/taxee-tax-statistics/master/src/statistics/" + year + "/federal.json";

            // Download the json
            try {
                InputStream in = new URL(url).openStream();
                Files.copy(in, Paths.get("federal_brackets.json"), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception download_exception) {
                System.out.println("Error occurred downloading file: " + download_exception);
            }
        }

        System.out.println("Let's start by estimating your federal taxes.");
        TaxEstimator taxEstObject = new TaxEstimator();
        taxEstObject.getFederalFilingStatus();
        System.out.println("Filing Status: " + taxEstObject.filingStatus);

        System.out.printf("Please enter your taxable income: $");

        Scanner taxableIncomeInput = new Scanner(System.in);
        int taxableIncome = taxableIncomeInput.nextInt();

        System.out.println("Given your filing status and the current year (" + year + ")" + ", these are the tax brackets:");

        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader("federal_brackets.json");

            JSONObject json_object = (JSONObject) parser.parse(reader);
            taxEstObject.federal_tax_data = (JSONObject) ((JSONObject) json_object.get("tax_withholding_percentage_method_tables")).get("annual");

            taxEstObject.parse_federal_brackets_from_json();

            taxEstObject.printFederalTaxBracketTable(taxEstObject.filingStatus);
        }

        catch(FileNotFoundException e) {
            System.out.println("The federal tax brackets json file was not found.");
            e.printStackTrace();
        }

        catch(IOException e) {
            System.out.println("An IOException occurred: e");
        }

        catch(ParseException e) {
            System.out.println("There was a JSON parsing error: " + e);
        }

        //Calculate federal taxes
        double estimated_federal_taxes = 0.0;
        estimated_federal_taxes = taxEstObject.calculateFederalTaxes(taxEstObject.filingStatus, taxableIncome);
        System.out.println("Your estimated federal taxes are: $" + estimated_federal_taxes);

        System.out.print("Would you also like to estimate your state taxes [y/n]: ");
        Scanner continueWithStateTaxes = new Scanner(System.in);
        String stateTaxesInput = continueWithStateTaxes.nextLine();
        if (!(stateTaxesInput.equals("y") || stateTaxesInput.equals("Y"))) {
            System.out.println("Ok, exiting now.");
            System.exit(0);
        }

        String stateInput = "";
        do {
            System.out.printf("Please enter your state's abbreviated name: ");
            Scanner stateInputScanner = new Scanner(System.in);
            stateInput = stateInputScanner.nextLine().toUpperCase();

            if (taxEstObject.state_abbr_to_file_name.containsKey(stateInput)) {
                break;
            }

            else {
                System.out.println("Invalid state abbreviation.");
            }
        } while(!taxEstObject.state_abbr_to_file_name.containsKey(stateInput));

        System.out.println("Checking if you have the most recent state tax brackets for this year (" + year + ").");

        String json_state_file = (String) taxEstObject.state_abbr_to_file_name.get(stateInput) + ".json";

        try {
            FileReader reader = new FileReader(json_state_file);
            System.out.println("All good! You have the most recent tax data.");
        }
        catch (FileNotFoundException e) {
            System.out.println("No tax bracket data found for " + taxEstObject.state_abbr_to_file_name.get(stateInput) + "!");

            System.out.println("Downloading most recent state tax brackets from taxee github source.");
            System.out.println("Source: https://github.com/taxee/taxee-tax-statistics");
            System.out.println();

            String url = "https://raw.githubusercontent.com/taxee/taxee-tax-statistics/master/src/statistics/" + year + "/" + json_state_file;

            // Download the json
            try {
                InputStream in = new URL(url).openStream();
                Files.copy(in, Paths.get(json_state_file), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception download_exception) {
                System.out.println("Error occurred downloading file: " + download_exception);
            }
        }

        System.out.println("Given your filing status and the current year (" + year + ")" + ", these are the tax brackets for " + taxEstObject.state_abbr_to_file_name.get(stateInput) + ":");

        try {
            JSONParser parser = new JSONParser();
            FileReader reader = new FileReader(json_state_file);

            JSONObject json_object = (JSONObject) parser.parse(reader);
            taxEstObject.selected_state_tax_data = json_object;

            taxEstObject.parse_selected_state_brackets_from_json();

            taxEstObject.printSelectedStateTaxBracketTable(taxEstObject.filingStatus);
        }

        catch(FileNotFoundException e) {
            System.out.println("The selected state tax brackets json file was not found.");
            e.printStackTrace();
        }

        catch(IOException e) {
            System.out.println("An IOException occurred: e");
        }

        catch(ParseException e) {
            System.out.println("There was a JSON parsing error: " + e);
        }

        //Calculate state taxes
        double estimated_state_taxes = 0.0;
        estimated_state_taxes = taxEstObject.calculateSelectedStateTaxes(taxEstObject.filingStatus, taxableIncome);
        System.out.println();
        System.out.println("Your estimated state taxes for " + stateInput.toUpperCase() + " are: $" + String.format("%.2f", estimated_state_taxes));

        System.out.println("========================================");
        System.out.println("SUMMARY:");

        /* Summary Output Format

                        TaxableIncome   Taxes   EffectiveTaxRate
            Federal     $...            $...    ..%

            State       $...            $...    ..%
         */

        /* Note on Formatting:

           1st line: 15 spaces, 20 characters for "Taxable Income", 15 characters for "Taxes", 10 characters for "Effective Tax Rate"
           2nd line: 15 spaces for "Federal", 20 characters for taxableIncome, 15 characters for federal taxes with 2 digits precision, 10 characters for Effective Tax Rate with 2 digits precision
           3rd line: 15 spaces for "State", 20 characters for taxableIncome, 15 characters for state taxes with 2 digits precision, 10 characters for Effective Tax Rate with 2 digits precision

           And the "-" before each number in the specifiers means "left justified"
        */

        System.out.printf("%-15s%-20s%-15s%-10s%n", "", "Taxable Income", "Taxes", "Effective Tax Rate");
        System.out.printf("%-15s%-20s%-15s%-10s%n", "Federal", String.format("$%.2f", (double) taxableIncome), String.format("$%.2f", estimated_federal_taxes), String.format("%.2f%%", (estimated_federal_taxes/taxableIncome)*100));
        System.out.printf("%-15s%-20s%-15s%-10s%n", "State", String.format("$%.2f", (double) taxableIncome), String.format("$%.2f", estimated_state_taxes), String.format("%.2f%%", (estimated_state_taxes/taxableIncome)*100));
        System.out.println();
        System.out.printf("%-15s%-20s%-15s%-10s%n", "Total", String.format("$%.2f", (double) taxableIncome), String.format("$%.2f", estimated_state_taxes + estimated_federal_taxes), String.format("%.2f%%", ((estimated_state_taxes + estimated_federal_taxes)/taxableIncome)*100));
        System.out.println();
    }
}
