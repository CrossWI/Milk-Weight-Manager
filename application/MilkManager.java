package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MilkManager class that manages farms and their data
 * @author richardwang
 *
 */
public class MilkManager {
	private HashMap<String, Farm> map;
	MilkDate minDate = null;
	MilkDate maxDate = null;

	public MilkManager() {
		this.map = new HashMap<>();
	}

	/**
	 * Inner class to hold a weight and a percent for each month
	 * 
	 * @author Collin Krause
	 *
	 */
	class WeightPercentPair {

		int milkWeight;
		double percent;

		WeightPercentPair(int milkWeight, double percent) {
			this.milkWeight = milkWeight;
			this.percent = percent;
		}

		public int getMilkWeight() {
			return milkWeight;
		}

		public double getPercent() {
			return percent;
		}
	}

	/**
	 * constructs the data set given a files 
	 * @param file list of files that gets parsed
	 * @throws IOException if reading line has error
	 * @throws ParseException if file cannot be parsed
	 */
	public void constructMap(List<File> file) throws IOException, ParseException {
		for (int i = 0; i < file.size(); i++) {
			String row = null;
			BufferedReader br = new BufferedReader(new FileReader(file.get(i)));
			while ((row = br.readLine()) != null) {
				String[] data = row.split(",");
				MilkDate date;
				try {
					date = new MilkDate(data[0]);
					Integer.parseInt(data[2]);

					if (minDate == null && maxDate == null && i == 0) {
						minDate = date;
						maxDate = date;
					}

					else if (date.compareTo(minDate) < 0)
						minDate = date;

					else if (date.compareTo(maxDate) > 0)
						maxDate = date;

					// if the farm is already in the database
					if (map.containsKey(data[1])) {
						map.get(data[1]).addMilk(data[0], Integer.parseInt(data[2]));
					}

					// if a new farm is being added
					else {
						Farm farm = new Farm(data[0], data[1], Integer.parseInt(data[2]));
						map.put(data[1], farm);
					}
				} catch (InvalidDateException e) {
					continue;
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
	}

	/**
	 * Adds more milk at the specified date to the specified farm
	 * 
	 * @param farmID The name of the farm to add to
	 * @param date   the date to add to
	 * @param milk   the amount of milk to add
	 * @throws FarmNotFoundException if a farm is not found
	 * @throws DataAlreadyExistsException 
	 * @throws MissingDataException 
	 */
	public void addMilk(String farmID, MilkDate date, int milkWeight)
			throws FarmNotFoundException, NegativeMilkWeightException {
		// If the milk weight to add is negative, throw and exception
		if (milkWeight < 0)
			throw new NegativeMilkWeightException();

		// Check if the farmID is in the database
		if (map.containsKey(farmID)) {
			map.get(farmID).addMilk(date.toString(), milkWeight);
		}

		// If a farm is not found throw an exception
		else
			throw new FarmNotFoundException();
	}

	/**
	 * Removes milk at the specified date from the specified farm
	 * 
	 * @param farmID the id of the farm to remove milk from
	 * @param date   the date to remove from
	 * @param milk   the amount of milk to remove
	 * @throws FarmNotFoundException if the farm is not found
	 * @throws MissingDataException 
	 */
	public void removeMilk(String farmID, MilkDate date) throws FarmNotFoundException, MissingDataException {

		// Check if the farmID is in the database
		if (map.containsKey(farmID))
			map.get(farmID).removeMilk(date.toString());

		// If a farm is not found throw an exception
		else
			throw new FarmNotFoundException();
	}

	/**
	 * returns the smallest date in the given list of files
	 * @return smallest date in the given list of files
	 */
	public MilkDate getMinDate() {
		return minDate;
	}

	/**
	 * returns the largest date in the given list of files
	 * @return largest date in the given list of files
	 */
	public MilkDate getMaxDate() {
		return maxDate;
	}

	/**
	 * gets a list of years given the data set
	 * @return list of years given the data set
	 * @throws InvalidDateException if a date is invalid
	 */
	public ArrayList<String> getYears() throws InvalidDateException {
		ArrayList<String> years = new ArrayList<String>();

		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			for (Map.Entry<String, Integer> farm : map.get(entry.getKey()).getMilkWeightLog().entrySet()) {
				MilkDate date = new MilkDate(farm.getKey());
				if (!years.contains(String.valueOf(date.getYear())))
					years.add(String.valueOf(date.getYear()));
			}
		}
		Collections.sort(years);
		return years;
	}
	
	/**
	 * gets a list of months given the data set
	 * @return list of months given the data set
	 * @throws InvalidDateException if a date is invalid
	 */
	public ArrayList<String> getMonths() throws InvalidDateException {
		ArrayList<String> months = new ArrayList<String>();

		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			for (Map.Entry<String, Integer> farm : map.get(entry.getKey()).getMilkWeightLog().entrySet()) {
				MilkDate date = new MilkDate(farm.getKey());
				if (!months.contains(String.valueOf(date.monthToString())))
					months.add(date.monthToString());
			}
		}
		Collections.sort(months);
		return months;
	}
	
	/**
	 * gets list farm IDs 
	 * @return list of farms' IDs
	 */
	public ArrayList<String> getFarms() {
		ArrayList<String> farms = new ArrayList<String>();

		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			if (!farms.contains(entry.getKey()))
				farms.add(entry.getKey());
		}
		Collections.sort(farms);
		return farms;
	}

	/**
	 * Returns a list containing the total weight for each month for a single farm.
	 * Also contains the percent of the total weight for each month
	 * 
	 * @param farmID the farm to get the weights for
	 * @param year   the year to get the weights for
	 * @return the List containing the 12 month's data
	 * @throws InvalidDateException 
	 */
	public HashMap<Integer, WeightPercentPair> farmReport(String farmID, String year) throws InvalidDateException {

		int[] monthMilkWeight = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // initial milk weights by year
		HashMap<Integer, WeightPercentPair> farmReportList = new HashMap<>();
		int yearValue = Integer.parseInt(year);

		// iterate through farm milk data for farm with specified farm ID
		for (Map.Entry<String, Integer> entry : map.get(farmID).getMilkWeightLog().entrySet()) {
			MilkDate date = new MilkDate(entry.getKey());
			// check to see if data point is in the correct year
			if (date.getYear() == yearValue) { // add milk weight value of data point based on
															// the month it was in
				if (date.getMonth() == 1)
					monthMilkWeight[0] += entry.getValue();

				if (date.getMonth() == 2)
					monthMilkWeight[1] += entry.getValue();

				if (date.getMonth() == 3)
					monthMilkWeight[2] += entry.getValue();

				if (date.getMonth() == 4)
					monthMilkWeight[3] += entry.getValue();

				if (date.getMonth() == 5)
					monthMilkWeight[4] += entry.getValue();

				if (date.getMonth() == 6)
					monthMilkWeight[5] += entry.getValue();

				if (date.getMonth() == 7)
					monthMilkWeight[6] += entry.getValue();

				if (date.getMonth() == 8)
					monthMilkWeight[7] += entry.getValue();

				if (date.getMonth() == 9)
					monthMilkWeight[8] += entry.getValue();

				if (date.getMonth() == 10)
					monthMilkWeight[9] += entry.getValue();

				if (date.getMonth() == 11)
					monthMilkWeight[10] += entry.getValue();

				if (date.getMonth() == 12)
					monthMilkWeight[11] += entry.getValue();
			}
		}

		// Find the total milk weight for the year
		int totalWeight = 0;
		for (int i = 0; i < monthMilkWeight.length; i++) {
			totalWeight += monthMilkWeight[i];
		}

		// Iterate through the array to add the farms to the right MonthPair
		for (int i = 0; i < monthMilkWeight.length; i++) {
			double percent = (double) monthMilkWeight[i] / totalWeight;
			WeightPercentPair pair = new WeightPercentPair(monthMilkWeight[i], percent);
			farmReportList.put(i, pair);
		}
		return farmReportList;
	}

	/**
	 * Private helper to get the total milk weight for a farm for a specified year
	 * 
	 * @param farmID Farm to use
	 * @param year   year to use
	 * @return total milk weight in the specified year for the farm
	 * @throws InvalidDateException 
	 */
	private int getAnnualMilkWeight(String farmID, String year) throws InvalidDateException {
		int[] monthMilkWeight = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // initial milk weights by year
		int yearValue = Integer.parseInt(year);

		// Iterate through farm's milk weight data
		for (Map.Entry<String, Integer> entry : map.get(farmID).getMilkWeightLog().entrySet()) {
			MilkDate date = new MilkDate(entry.getKey());
			// check to see if data point is in the correct year
			if (date.getYear() == yearValue) { // add milk weight value of data point based on
															// the month it was in
				if (date.getMonth() == 1)
					monthMilkWeight[0] += entry.getValue();

				if (date.getMonth() == 2)
					monthMilkWeight[1] += entry.getValue();

				if (date.getMonth() == 3)
					monthMilkWeight[2] += entry.getValue();

				if (date.getMonth() == 4)
					monthMilkWeight[3] += entry.getValue();

				if (date.getMonth() == 5)
					monthMilkWeight[4] += entry.getValue();

				if (date.getMonth() == 6)
					monthMilkWeight[5] += entry.getValue();

				if (date.getMonth() == 7)
					monthMilkWeight[6] += entry.getValue();

				if (date.getMonth() == 8)
					monthMilkWeight[7] += entry.getValue();

				if (date.getMonth() == 9)
					monthMilkWeight[8] += entry.getValue();

				if (date.getMonth() == 10)
					monthMilkWeight[9] += entry.getValue();

				if (date.getMonth() == 11)
					monthMilkWeight[10] += entry.getValue();

				if (date.getMonth() == 12)
					monthMilkWeight[11] += entry.getValue();
			}
		}

		// Find the total milk weight for the year
		int totalWeight = 0;
		for (int i = 0; i < monthMilkWeight.length; i++) {
			totalWeight += monthMilkWeight[i];
		}
		return totalWeight;
	}

	/**
	 * Returns the total weight and percent of total weight of all farms by farm for
	 * the specified year
	 * 
	 * @param year year to use
	 * @return HashMap with contains all of the farm IDs as keys, and their
	 *         contribution to the total weight
	 * @throws InvalidDateException 
	 */
	public HashMap<String, WeightPercentPair> annualReport(String year) throws InvalidDateException {
		int totalMilkWeight = 0;
		HashMap<String, WeightPercentPair> farmMap = new HashMap<>();

		// Iterate through the entrySet to get the total milk weight
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			totalMilkWeight += getAnnualMilkWeight(entry.getKey(), year);
		}

		// Iterate through the entrySet to create weight, percent pairs
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			int milkWeight = getAnnualMilkWeight(entry.getKey(), year);
			double percent = (double) milkWeight / totalMilkWeight;
			WeightPercentPair pair = new WeightPercentPair(milkWeight, percent);

			farmMap.put(entry.getKey(), pair);
		}
		return farmMap;
	}

	/**
	 * Private helper to get the total milk weight for a farm for a specified month
	 * 
	 * @param farmID Farm to use
	 * @param month  month to use
	 * @param year   year to use
	 * @return total milk weight in the specified month for the farm
	 * @throws InvalidMonthException
	 */
	private int getMonthlyMilkWeight(String farmID, String month, String year) throws InvalidDateException {
		int totalMilkWeight = 0;
		int yearValue = Integer.parseInt(year);
		int monthValue = 0;

		if (month.equalsIgnoreCase("january") || month.equalsIgnoreCase("jan"))
			monthValue = 1;

		if (month.equalsIgnoreCase("february") || month.equalsIgnoreCase("feb"))
			monthValue = 2;

		if (month.equalsIgnoreCase("march") || month.equalsIgnoreCase("mar"))
			monthValue = 3;

		if (month.equalsIgnoreCase("april") || month.equalsIgnoreCase("apr"))
			monthValue = 4;

		if (month.equalsIgnoreCase("may"))
			monthValue = 5;

		if (month.equalsIgnoreCase("june") || month.equalsIgnoreCase("jun"))
			monthValue = 6;

		if (month.equalsIgnoreCase("july") || month.equalsIgnoreCase("jul"))
			monthValue = 7;

		if (month.equalsIgnoreCase("august") || month.equalsIgnoreCase("aug"))
			monthValue = 8;

		if (month.equalsIgnoreCase("september") || month.equalsIgnoreCase("sep"))
			monthValue = 9;

		if (month.equalsIgnoreCase("october") || month.equalsIgnoreCase("oct"))
			monthValue = 10;

		if (month.equalsIgnoreCase("november") || month.equalsIgnoreCase("nov"))
			monthValue = 11;

		if (month.equalsIgnoreCase("december") || month.equalsIgnoreCase("dec"))
			monthValue = 12;

		if (monthValue == 0)
			throw new InvalidDateException();

		for (Map.Entry<String, Integer> entry : map.get(farmID).getMilkWeightLog().entrySet()) {
			MilkDate date = new MilkDate(entry.getKey());
			if (date.getYear() == yearValue && date.getMonth() == monthValue)
				totalMilkWeight += entry.getValue();
		}

		return totalMilkWeight;
	}

	/**
	 * Returns the total weight and percent of total weight of all farms by farm for
	 * the specified month
	 * 
	 * @param year  year to use
	 * @param month month to use
	 * @return HashMap with contains all of the farm IDs as keys, and their
	 *         contribution to the total weight
	 * @throws InvalidMonthException
	 */
	public HashMap<String, WeightPercentPair> monthlyReport(String month, String year) throws InvalidDateException {
		int totalMilkWeight = 0;
		HashMap<String, WeightPercentPair> farmMap = new HashMap<>();

		// Iterate through the entrySet to get the total milk weight
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			totalMilkWeight += getMonthlyMilkWeight(entry.getKey(), month, year);
		}

		// Iterate through the entrySet to create weight, percent pairs
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			int milkWeight = getMonthlyMilkWeight(entry.getKey(), month, year);
			double percent = (double) milkWeight / totalMilkWeight;
			WeightPercentPair pair = new WeightPercentPair(milkWeight, percent);

			farmMap.put(entry.getKey(), pair);
		}
		return farmMap;
	}

	/**
	 * Private helper to get the total milk weight for a farm in a specified date
	 * range
	 * 
	 * @param farmID    Farm to use
	 * @param startDate Beginning of date range
	 * @param endDate   End of date range
	 * @return total milk weight in the given range for the farm
	 * @throws InvalidDateException
	 */
	private int getMilkWeightInRange(String farmID, String startDateString, String endDateString)
			throws InvalidDateException {
		int totalMilkWeight = 0;
		MilkDate startDate = new MilkDate(startDateString);
		MilkDate endDate = new MilkDate(endDateString);

		// iterate through farm milk weight data
		for (Map.Entry<String, Integer> entry : map.get(farmID).getMilkWeightLog().entrySet()) {
			MilkDate date = new MilkDate(entry.getKey());
			// if the data is inside the specified range
			if (startDate.compareTo(date) <= 0 && endDate.compareTo(date) >= 0)
				totalMilkWeight += entry.getValue(); // increment total milk weight
		}
		return totalMilkWeight;
	}

	/**
	 * Returns the total weight and percent of total weight of all farms by farm for
	 * the specified date range
	 * 
	 * @param startDate Beginning of date range
	 * @param endDate   End of date range
	 * @return HashMap with contains all of the farm IDs as keys, and their
	 *         contribution to the total weight
	 * @throws InvalidDateException
	 */
	public HashMap<String, WeightPercentPair> dateRangeReport(String startDate, String endDate)
			throws InvalidDateException {
		int totalMilkWeight = 0;
		HashMap<String, WeightPercentPair> farmMap = new HashMap<>();

		// Iterate through the entrySet to get total milk weight
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			totalMilkWeight += getMilkWeightInRange(entry.getKey(), startDate, endDate);
		}

		// Iterate through the entrySet to create weight, percent pairs
		for (Map.Entry<String, Farm> entry : map.entrySet()) {
			int milkWeight = getMilkWeightInRange(entry.getKey(), startDate, endDate);
			double percent = (double) milkWeight / totalMilkWeight;
			WeightPercentPair pair = new WeightPercentPair(milkWeight, percent);

			farmMap.put(entry.getKey(), pair);
		}
		return farmMap;
	}
}
