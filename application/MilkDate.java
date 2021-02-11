package application;

/**
 * MilkDate class to format the date inputs 
 * @author richardwang
 *
 */
public class MilkDate {
	private int year;
	private int month;
	private int day;

	/**
	 * constructor for a MilkDate object
	 * @param date to be formatted
	 * @throws InvalidDateException if the date input cannot be parsed
	 */
	public MilkDate(String date) throws InvalidDateException {
		String[] dateComponents = date.split("-");

		if (dateComponents.length != 3) {
			throw new InvalidDateException();
		}

		this.year = Integer.parseInt(dateComponents[0]);
		this.month = Integer.parseInt(dateComponents[1]);
		this.day = Integer.parseInt(dateComponents[2]);
	}

	/**
	 * gets the day in MilkDate
	 * @return day in MilkDate
	 */
	public int getDay() {
		return day;
	}

	/**
	 * gets the month in MilkDate
	 * @return month in MilkDate
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * gets the year in MilkDate
	 * @return year in MilkDate
	 */
	public int getYear() {
		return year;
	}

	/**
	 * compares 2 MilkDate objects
	 * @param otherDate the other date object to be compared
	 * @return 0 if the two dates are the same, positive number if the current object is more recent, negative number otherwise
	 */
	public int compareTo(MilkDate otherDate) {
		if (this.getYear() > otherDate.getYear()
				|| (this.getMonth() > otherDate.getMonth() && this.getYear() == otherDate.getYear())
				|| (this.getDay() > otherDate.getDay() && this.getMonth() == otherDate.getMonth()
						&& this.getYear() == otherDate.getYear()))
			return 1;

		else if (this.getYear() < otherDate.getYear()
				|| (this.getMonth() < otherDate.getMonth() && this.getYear() == otherDate.getYear())
				|| (this.getDay() < otherDate.getDay() && this.getMonth() == otherDate.getMonth()
						&& this.getYear() == otherDate.getYear()))
			return -1;

		return 0;
	}

	/**
	 * converts integer version of month to string representation @return= returns
	 * string representation of month
	 */
	public String monthToString() {
		if (this.getMonth() == 1)
			return "January";

		if (this.getMonth() == 2)
			return "February";

		if (this.getMonth() == 3)
			return "March";

		if (this.getMonth() == 4)
			return "April";

		if (this.getMonth() == 5)
			return "May";

		if (this.getMonth() == 6)
			return "June";

		if (this.getMonth() == 7)
			return "July";

		if (this.getMonth() == 8)
			return "August";

		if (this.getMonth() == 9)
			return "September";

		if (this.getMonth() == 10)
			return "October";

		if (this.getMonth() == 11)
			return "Novemeber";

		return "Decemeber";
	}

	/**
	 * creates a string in correct format for parsing
	 * 
	 * @return- returns date in correct format for use in program
	 */
	public String toString() {
		return this.getYear() + "-" + this.getMonth() + "-" + this.getDay();
	}
}
