package application;

import java.util.HashMap;

/**
 * Farm class that holds data for each individual farm
 * @author richardwang
 *
 */
public class Farm {
	private String name;
	private HashMap<String, Integer> map;
	
	/**
	 * constructor with no data in the farm
	 * @param name of the farm
	 */
	public Farm(String name) {
		this.name = name;
		map = new HashMap<>();
	}
	
	/**
	 * constructor with 1 data input into the farm object
	 * @param date date of the input
	 * @param name of the farm
	 * @param milkWeight weight of milk produced that on the given date
	 */
	public Farm(String date, String name, int milkWeight) {
		this.name = name;
		map = new HashMap<>();

		map.put(date, milkWeight);
	}
	
	/**
	 * returns the name of the farm
	 * @return name of the farm
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * adds an entry in farm, an entry contains date and milk weight
	 * @param date of the entry
	 * @param milkWeight of the entry
	 */
	public void addMilk(String date, int milkWeight) {
		map.put(date, milkWeight);
	}
	
	/**
	 * removes an entry in farm given a certain date
	 * @param date of the entry
	 * @throws MissingDataException if there are no entries that contain the given date
	 */
	public void removeMilk(String date) throws MissingDataException {
		if (!map.containsKey(date))
			throw new MissingDataException();

		map.remove(date);
	}

	/**
	 * sets the name of the farm
	 * @param name to be set for the farm
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * gets the list of entries 
	 * @return HashMap of entries for the farm
	 */
	public HashMap<String, Integer> getMilkWeightLog() {
		return map;
	}
}
