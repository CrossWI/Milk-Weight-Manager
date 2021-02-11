package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import application.MilkManager.WeightPercentPair;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Main GUI class. Displays all data and allows user to in
 * 
 * @author Collin Krause, Cam Cross, Richard Wang, Ryan Demar
 *
 */
public class Main extends Application {
	// store any command-line arguments that were entered.
	// NOTE: this.getParameters().getRaw() will get these also
	private List<String> args;
	MilkManager milkManager;
	boolean farmReportButtonPressed = false; // Used for exception handling
	boolean farmNotFoundExceptionBool = false; // Used for exception handling
	boolean negativeMilkWeightExceptionBool = false; // Used for exception handling
	boolean updated = false;
	boolean displayPercentages = true;
	ObservableList<String> yearList; // Lists to hold user inputed data
	ObservableList<String> monthList;
	ObservableList<String> farmIDList;
	DecimalFormat df = new DecimalFormat("#.##");

	private static final int WINDOW_WIDTH = 1000;
	private static final int WINDOW_HEIGHT = 900;
	private static final String APP_TITLE = "Milk Weights Display";

	/**
	 * Display method that runs and manages the GUI
	 */
	public void start(Stage primaryStage) throws Exception {

		// Create a MilkManager Object and construct the hash table to hold the data
		milkManager = new MilkManager();

		// save the runtime args
		args = this.getParameters().getRaw();

		// Create a label for left, top, and right titles and each to a HBox
		Text t1 = new Text(APP_TITLE);
		t1.setStyle("-fx-font: 45 arial;");
		HBox title = new HBox();
		title.setStyle("-fx-background-color: #d9b08c");
		title.getChildren().add(t1);
		title.setAlignment(Pos.CENTER);

		// Create the main VBoxes for the left, right, and center column
		VBox left = new VBox();
		VBox right = new VBox();
		VBox center = new VBox();

		// Creates the left and right columns of the GUI
		formatLeft(left, center, primaryStage);
		formatRight(left, right, center, primaryStage);

		// CSS style for left panel
		String leftBoarder = "-fx-border-color: #116466;\n" + "-fx-border-insets: 5;\n" + "-fx-border-width: 6;\n"
				+ "-fx-background-color: #c1e0d8";

		// CSS style for right panel
		String rightBorder = "-fx-border-color: #116466;\n" + "-fx-border-insets: 5;\n" + "-fx-border-width: 6;\n"
				+ "-fx-background-color: #c1e0d8";

		// CSS style for center panel
		String centerBorder = "-fx-border-color: #116466;\n" + "-fx-border-insets: 5;\n" + "-fx-border-width: 5;\n"
				+ "-fx-background-color: #c1e0d8";

		left.setStyle(leftBoarder);
		right.setStyle(rightBorder);

		// Main layout is Border Pane example (top,left,center,right,bottom)
		BorderPane root = new BorderPane();

		// Border courtesy of:
		// https://stackoverflow.com/questions/27712213/how-do-i-make-a-simple-solid-border-around-a-flowpane-in-javafx
		root.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		ScrollPane scrollPane = new ScrollPane(center);
		scrollPane.fitToHeightProperty();
		scrollPane.setStyle(centerBorder);

		// Add labels to the left and right and top of the scene
		root.setLeft(left);
		root.setRight(right);
		root.setTop(title);
		root.setCenter(scrollPane);

		// Creates the main GUI interface
		Scene mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
		mainScene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());

		// Add the stuff and set the primary stage
		primaryStage.setTitle(APP_TITLE);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	/**
	 * Creates all the elements used in the left column of the GUI
	 * 
	 * @param left         Vertical box to hold all the elements
	 * @param center       Vertical box to hold all elements in the center of the
	 *                     GUI. Used to pass to other methods
	 * @param primaryStage main GUI element passed to other methods
	 */
	private void formatLeft(VBox left, VBox center, Stage primaryStage) {

		// Necessary elements for the left column title
		Text text = new Text("Edit Data:");
		text.setStyle("-fx-font: 25 arial;");
		text.setUnderline(true);
		HBox editDataTitle = new HBox();
		editDataTitle.getChildren().add(text);

		// Add labels and text fields to edit existing data
		Label blankSpace = new Label("");
		Label editData = new Label("EDIT EXISTING DATA");
		
		// Elements used to create the farm drop down
		ComboBox farmIDComboBox1 = new ComboBox();
		farmIDComboBox1.setPromptText("Select a Farm");
		farmIDComboBox1.setItems(farmIDList);

		DatePicker datePick = new DatePicker();

		// This button will launch a new window to edit/add/remove data
		Button editDataButton = new Button("Edit Data");
		HBox buttonBox = new HBox(editDataButton);

		// Event handling for the edit data button
		editDataButton.setOnAction(e -> {
			try {
				// Stores the information inputed by the user
				String farmID = (String) farmIDComboBox1.getValue();
				MilkDate date = new MilkDate(String.valueOf(datePick.getValue().getYear()) + "-"
						+ String.valueOf(datePick.getValue().getMonthValue()) + "-"
						+ String.valueOf(datePick.getValue().getDayOfMonth()));
				editDataButtonPopUp(farmID, date, primaryStage); // Helper method for this buttons actions
			} catch (InvalidDateException e1) { // Opens a warning pop up when the user inputed date is
												// invalid
				Alert alert = new Alert(AlertType.WARNING,
						"The date is not in the date range, please enter a valid date.", ButtonType.OK);
				alert.showAndWait();
			}
		});
		
		// Adds the edit data elements to the left column
		left.getChildren().addAll(editDataTitle, blankSpace, editData, farmIDComboBox1, datePick, buttonBox);

		// Spacing between functions
		Label blankSpace1 = new Label("");
		Separator separator1 = new Separator(Orientation.HORIZONTAL);
		Label blankSpace2 = new Label("");
		left.getChildren().addAll(blankSpace1, separator1, blankSpace2);

		// Add necessary labels and text fields for the farm report
		Text t4 = new Text("Reports:");
		t4.setStyle("-fx-font: 25 arial;");
		t4.setUnderline(true);
		Label blankSpace3 = new Label("");
		Label farmReport = new Label("FARM REPORT");

		// Necessary elements for the farmID and year drop downs
		ComboBox farmIDComboBox2 = new ComboBox();
		farmIDComboBox2.setPromptText("Select a Farm");
		farmIDComboBox2.setItems(farmIDList);

		ComboBox yearComboBox1 = new ComboBox();
		yearComboBox1.setPromptText("Select a Year");
		yearComboBox1.setItems(yearList);

		// Button elemetns
		Button displayDataButton1 = new Button("Display Data");
		HBox buttonBox1 = new HBox(displayDataButton1);

		// Event handling for the farm report display button
		displayDataButton1.setOnAction(e -> {
			String farmID1 = (String) farmIDComboBox2.getValue(); // Stores user inputed data
			String year1 = (String) yearComboBox1.getValue();

			// Opens up pop up windows for invalid farmID or year
			try {
				farmReportButtonAction(farmID1, year1, center);
			} catch (FarmNotFoundException e1) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.WARNING, "The farm you entered does not exist in the given file.",
						ButtonType.OK);
				alert.showAndWait();

			} catch (NullPointerException e1) {
				Alert alert = new Alert(AlertType.WARNING, "The farm you entered does not exist in the given file.",
						ButtonType.OK);
				alert.showAndWait();
			} catch (NumberFormatException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please select a year.", ButtonType.OK);
				alert.showAndWait();
			} catch (InvalidDateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		// Adds the farm report elements to the left column
		left.getChildren().addAll(t4, blankSpace3, farmReport, farmIDComboBox2, yearComboBox1, buttonBox1);

		// Add necessary labels and text fields for the annual report
		Label blankSpace4 = new Label("");
		Label annualReport = new Label("ANNUAL REPORT");

		// Necessary elements for the year drop dowm
		ComboBox yearComboBox2 = new ComboBox();
		yearComboBox2.setPromptText("Select a Year");
		yearComboBox2.setItems(yearList);

		// Display data button elements
		Button displayDataButton2 = new Button("Display Data");
		HBox buttonBox2 = new HBox(displayDataButton2);

		// Event handling
		displayDataButton2.setOnAction(e -> {
			String year2 = (String) yearComboBox2.getValue(); // Stores user input

			// Opens pop up windows for invalid farmID
			try {
				if (year2 == null) {
					throw new NullPointerException();
				}
				annualReportButtonAction(year2, center);
			} catch (NumberFormatException e1) {
				Alert alert = new Alert(AlertType.WARNING, "The farm you entered does not exist in the given file.",
						ButtonType.OK);
				alert.showAndWait();
			} catch (NullPointerException e1) {
				Alert alert = new Alert(AlertType.WARNING,
						"The farm you entered does not exist in the given file. NullPointerException", ButtonType.OK);
				alert.showAndWait();
			} catch (InvalidDateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		// Adds the annual report elements to the left column
		left.getChildren().addAll(blankSpace4, annualReport, yearComboBox2, buttonBox2);

		// Add necessary labels and text fields for the monthly report
		Label blankSpace5 = new Label("");
		Label monthlyReport = new Label("MONTHLY REPORT");

		// Elements for the year and month drop downs
		ComboBox yearComboBox3 = new ComboBox();
		yearComboBox3.setPromptText("Select a Year");
		yearComboBox3.setItems(yearList);

		ComboBox monthComboBox3 = new ComboBox(monthList);
		monthComboBox3.setPromptText("Select a Month");
		monthComboBox3.setItems(monthList);

		HBox dateBox = new HBox(monthComboBox3, yearComboBox3);

		// Display data button elements
		Button displayDataButton3 = new Button("Display Data");
		HBox buttonBox3 = new HBox(displayDataButton3);

		// Event handling for the monthly report display data button
		displayDataButton3.setOnAction(e -> {
			String month3 = (String) monthComboBox3.getValue(); // Stores user input
			String year3 = (String) yearComboBox3.getValue();

			try {

				// Opens an alert pop up if the year is not selected
				if (year3 == null) {
					Alert alert = new Alert(AlertType.WARNING, "Year not selected, please select a year.",
							ButtonType.OK);
					alert.showAndWait();
				}
				monthlyReportButtonAction(month3, year3, center); // Helper method for necessary actions for
																	// when the button is pressed
			}

			// Opens alert pop ups for invalid farmID or month
			catch (FarmNotFoundException e1) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.WARNING, "The farm you entered does not exist in the given file.",
						ButtonType.OK);
				alert.showAndWait();
			} catch (InvalidDateException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Month not selected, please selec a month.", ButtonType.OK);
				alert.showAndWait();
			}
		});

		// Adds month report elements to the left column
		left.getChildren().addAll(blankSpace5, monthlyReport, dateBox, buttonBox3);

		// Add all necessary labels and text fields for the date range report
		Label blankSpace6 = new Label("");
		Label dateRangeReport = new Label("DATE RANGE REPORT");
		Label startDate = new Label("  Start Date:");
		Label endDate = new Label("  End Date:");

		// Elements for the date picker
		DatePicker startDatePick = new DatePicker();
		DatePicker endDatePick = new DatePicker();

		// Button elements for the date range report display data button
		Button displayDataButton4 = new Button("Display Data");
		HBox buttonBox4 = new HBox(displayDataButton4);

		// Event handling for the month report display data button
		displayDataButton4.setOnAction(e -> {
			String startTime = null;
			String endTime = null;
			try {
				// Stores user input for the date picker
				startTime = startDatePick.getValue().getYear() + "-" + startDatePick.getValue().getMonthValue() + "-"
						+ startDatePick.getValue().getDayOfMonth();
				endTime = endDatePick.getValue().getYear() + "-" + endDatePick.getValue().getMonthValue() + "-"
						+ endDatePick.getValue().getDayOfMonth();
				dateRangeReportButtonAction(startTime, endTime, center);
			}

			// Opens alert windows for invalid user input
			catch (FarmNotFoundException e1) {
				Alert alert = new Alert(AlertType.WARNING, "The farm you entered does not exist in the given file.",
						ButtonType.OK);
				alert.showAndWait();

			} catch (NullPointerException e1) {
				System.out.println(startTime);
				if (startTime == null) {
					Alert alert = new Alert(AlertType.WARNING, "Start date not entered, please enter a start time.",
							ButtonType.OK);
					alert.showAndWait();
				}
				if (endTime == null) {
					Alert alert = new Alert(AlertType.WARNING, "End date not entered, please enter a end time.",
							ButtonType.OK);
					alert.showAndWait();
				}
			} catch (InvalidDateException e1) {
				Alert alert = new Alert(AlertType.WARNING,
						"The start date comes after the end date, please choose a new start date.", ButtonType.OK);
				alert.showAndWait();
			} catch (StartDateOutOfRangeException e1) {
				Alert alert = new Alert(AlertType.WARNING,
						"The start date is not in the date range, please enter a valid start date.", ButtonType.OK);
				alert.showAndWait();
			} catch (EndDateOutOfRangeException e1) {
				Alert alert = new Alert(AlertType.WARNING,
						"The end date is not in the date range, please enter a valid start date.", ButtonType.OK);
				alert.showAndWait();
			}
		});

		// Adds date range elements to the left column
		left.getChildren().addAll(blankSpace6, dateRangeReport, startDate, startDatePick, endDate, endDatePick,
				buttonBox4);
	}

	/**
	 * Creates all the elements used in the right column of the GUI
	 * 
	 * @param left         Vertical box to hold all the elements in the left column
	 * @param right        Vertical box used to hold the right column elements. Used
	 *                     to pass to other methods
	 * @param center       Vertical box to hold all elements in the center of the
	 *                     GUI. Used to pass to other methods
	 * @param primaryStage main GUI element passed to other methods
	 */
	private void formatRight(VBox left, VBox right, VBox center, Stage primaryStage) {

		// Title elements for the add data section
		Text text = new Text("Add Data:");
		text.setStyle("-fx-font: 25 arial;");
		text.setUnderline(true);
		HBox addDataTitle = new HBox();
		addDataTitle.getChildren().add(text);

		// Add necessary labels, text fields, boxes, and buttons for the add data
		// section
		Label blankSpace = new Label("");

		// Elements for the file chooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("csv Files", "*.csv"));

		Button addFile = new Button("Add");
		HBox addButton = new HBox(addFile);
		addButton.setAlignment(Pos.CENTER);

		// Event handling for the add files button
		addFile.setOnAction(e -> {
			List<File> files = fileChooser.showOpenMultipleDialog(primaryStage); // Holds all the user
																					// selected files
			if (!files.isEmpty())
				try {
					milkManager.constructMap(files);

					// Lists to hold all the possible farms, years, and months parsed from the user
					// selected
					// files
					yearList = FXCollections.observableArrayList(milkManager.getYears());
					monthList = FXCollections.observableArrayList(milkManager.getMonths());
					farmIDList = FXCollections.observableArrayList(milkManager.getFarms());
					left.getChildren().clear(); // Clears the left and right columns
					right.getChildren().clear();

					// Reformats the left and right columns so the
					// drop boxes contain the correct options
					formatLeft(left, center, primaryStage);
					formatRight(left, right, center, primaryStage);
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block

				} catch (IOException e1) {
					// TODO Auto-generated catch block

				} catch (ParseException e1) {
					// TODO Auto-generated catch block

				} catch (InvalidDateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		});

		// Adds of the add file elements
		right.getChildren().addAll(addDataTitle, blankSpace, addFile);

		// Spacing between functions
		Label blankSpace1 = new Label("");
		Separator separator1 = new Separator(Orientation.HORIZONTAL);
		Label blankSpace2 = new Label("");
		right.getChildren().addAll(blankSpace1, separator1, blankSpace2);

		// Add necessary labels, text fields, box, and buttons for the display indvidual
		// farms section
		Text t1 = new Text("Display Individual Farms:");
		t1.setStyle("-fx-font: 15 arial;");
		t1.setUnderline(true);
		Label blankSpace3 = new Label("");

		// Create an observable list to hold the items in the combo box
		ObservableList<String> items = FXCollections.observableArrayList("Minimum", "Maximum", "Average");

		// Create a combo box that shows a drop down box
		ComboBox<String> comboBox = new ComboBox<String>(items);
		comboBox.setPromptText("Pick Min, Max, or Average");

		// Elements for the farmID and year drop downs
		ComboBox farmIDComboBox1 = new ComboBox();
		farmIDComboBox1.setPromptText("Select a Farm");
		farmIDComboBox1.setItems(farmIDList);

		ComboBox yearComboBox = new ComboBox(yearList);
		yearComboBox.setPromptText("Select a Year");
		HBox yearBox = new HBox(yearComboBox);

		// Display individual farms display data button elements
		Button button2 = new Button("Display Data");
		HBox buttonBox2 = new HBox(button2);

		// Event handling for the display individual farms display data button
		button2.setOnAction(e -> {
			String farmID = (String) farmIDComboBox1.getValue(); // Stores the user input
			String year = (String) yearComboBox.getValue();
			String displayChoice = comboBox.getValue();
			try {
				// Helper method to display data to the center of the GUI
				displayIndividualFarmsButtonAction(farmID, year, displayChoice, center);
			}
			// Opens alert windows for invalid farmID and year input
			catch (NullPointerException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please select a farm and a display type.", ButtonType.OK);
				alert.showAndWait();
			} catch (NumberFormatException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please select a year.", ButtonType.OK);
				alert.showAndWait();
			} catch (InvalidDateException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please enter a valid date.", ButtonType.OK);
				alert.showAndWait();
			}
		});

		// Adds all display individual farms elemetns to the right column
		right.getChildren().addAll(t1, blankSpace3, comboBox, farmIDComboBox1, yearBox, buttonBox2);

		// Spacing between functions
		Label blankSpace4 = new Label("");
		Separator separator2 = new Separator(Orientation.HORIZONTAL);
		Label blankSpace5 = new Label("");
		right.getChildren().addAll(blankSpace4, separator2, blankSpace5);

		// Add necessary labels, text fields, box, and buttons for the display indvidual
		// farms section
		Text t2 = new Text("Display for all Farms:");
		t2.setStyle("-fx-font: 25 arial;");
		t2.setUnderline(true);
		Label blankSpace6 = new Label("");

		// Create a combo box that shows a drop down box
		ComboBox<String> comboBox2 = new ComboBox<String>(items);
		comboBox2.setPromptText("Pick Min, Max, or Average");

		// Elements for the year and month drop downs
		ComboBox yearComboBox2 = new ComboBox(yearList);
		yearComboBox2.setPromptText("Select a Year");
		HBox yearBox2 = new HBox(yearComboBox2);

		ComboBox monthComboBox = new ComboBox(monthList);
		monthComboBox.setPromptText("Select a Month");
		HBox monthBox2 = new HBox(monthComboBox);

		// Elements for the display for all farms display data button
		Button button3 = new Button("Display Data");
		HBox buttonBox3 = new HBox(button3);

		// Event handling for the display for all farms display data button
		button3.setOnAction(e -> {
			String month = (String) monthComboBox.getValue(); // Stores user input
			String year = (String) yearComboBox2.getValue();
			String displayChoice = comboBox2.getValue();
			try {
				// Helper method to display data to the center of the GUI when the button is
				// pressed
				displayForAllFarmsButtonAction(month, year, displayChoice, center);
			}
			// Opens up alert windows for invalid user input
			catch (NullPointerException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please select a farm and a display type.", ButtonType.OK);
				alert.showAndWait();
			} catch (NumberFormatException e1) {
				Alert alert = new Alert(AlertType.WARNING, "Please select a year.", ButtonType.OK);
				alert.showAndWait();
			}
		});

		// Adds display for all farms elements to the right column
		right.getChildren().addAll(t2, blankSpace6, comboBox2, monthBox2, yearBox2, buttonBox3);

		// Spacing between functions
		Label blankSpace7 = new Label("");
		Separator separator3 = new Separator(Orientation.HORIZONTAL);
		Label blankSpace8 = new Label("");
		right.getChildren().addAll(blankSpace7, separator3, blankSpace8);

		// Add necessary labels, text fields, boxes, and buttons for the display farm
		// share section
		Text t3 = new Text("        Display Percentages       ");
		t3.setUnderline(true);
		CheckBox checkBox = new CheckBox("Display Percentages");
		checkBox.setSelected(displayPercentages);
		checkBox.setOnAction(e -> displayPercentages = !displayPercentages);
		Label newLabel = new Label("(Must redisplay data to take effect)");

		// Adds the checkbox elements to the right column
		right.getChildren().addAll(t3, checkBox, newLabel);

	}

	/**
	 * Formats the center of the GUI when the farm report button is pressed
	 * 
	 * @param center     Vertical box to hold all the data displayed in the center
	 *                   of the GUI
	 * @param farmReport HashMap holding all the necessary farm data
	 * @param farmID     the farmID of the farm to display
	 * @param year       the year to display the data from
	 */
	public void formatCenterFarmReport(VBox center, HashMap<Integer, WeightPercentPair> farmReport, String farmID,
			String year) {

		// Create the first column of data
		VBox column1 = new VBox();

		// Elements for the title
		Label title = new Label(farmID + " - " + year);
		title.setUnderline(true);
		column1.getChildren().add(title);

		// Save report button elements
		Button saveReport = new Button("Save Report");

		// January data
		if (farmReport.get(0).getMilkWeight() != 0) { // Checks for non-zero weight data

			// Elements for the month label
			Label blankSpace1 = new Label("");
			Label janLabel = new Label("JANUARY:  ");
			janLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair janPair = farmReport.get(0);
			int janWeight = janPair.getMilkWeight();
			double janPercent = janPair.getPercent();

			HBox jan = new HBox();

			Label janMilkWeight = new Label(janWeight + " lbs   ");

			// Progress bar elements to display the percentage
			ProgressBar janPB = new ProgressBar();
			janPB.setProgress(janPercent);

			Label janPercentage = new Label("  (" + df.format(janPercent * 100) + "%)");

			// Only displays the percent if the display percent chek=ckbox is ticked
			if (displayPercentages) {
				jan.getChildren().addAll(janMilkWeight, janPB, janPercentage);
			} else {
				jan.getChildren().addAll(janMilkWeight);
			}

			// Adds the jan elements to the center of the GUI
			column1.getChildren().addAll(blankSpace1, janLabel, jan);
		}

		// February data
		if (farmReport.get(1).getMilkWeight() != 0) { // Checks for non-zero weight data

			// Elements for the month label
			Label blankSpace2 = new Label("");
			Label febLabel = new Label("FEBRUARY:  ");
			febLabel.setUnderline(true);

			// Get the weight and percent for the month
			WeightPercentPair febPair = farmReport.get(1);
			int febWeight = febPair.getMilkWeight();
			double febPercent = febPair.getPercent();

			HBox feb = new HBox();

			Label febMilkWeight = new Label(febWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar febPB = new ProgressBar();
			febPB.setProgress(febPercent);

			Label febPercentage = new Label("  (" + df.format(febPercent * 100) + "%)");

			// Only display the percentage if the display percentage checkbox is ticked
			if (displayPercentages) {
				feb.getChildren().addAll(febMilkWeight, febPB, febPercentage);
			} else {
				feb.getChildren().addAll(febMilkWeight);
			}

			// Adds the feb elements to the center of the GUI
			column1.getChildren().addAll(blankSpace2, febLabel, feb);
		}

		// March data
		if (farmReport.get(2).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace3 = new Label("");
			Label marLabel = new Label("MARCH:  ");
			marLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair marPair = farmReport.get(2);
			int marWeight = marPair.getMilkWeight();
			double marPercent = marPair.getPercent();

			HBox mar = new HBox();

			Label marMilkWeight = new Label(marWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar marPB = new ProgressBar();
			marPB.setProgress(marPercent);

			Label marPercentage = new Label("  (" + df.format(marPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				mar.getChildren().addAll(marMilkWeight, marPB, marPercentage);
			} else {
				mar.getChildren().addAll(marMilkWeight);
			}

			// Adds the march elements to the center of the GUI
			column1.getChildren().addAll(blankSpace3, marLabel, mar);
		}

		// April data
		if (farmReport.get(3).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace4 = new Label("");
			Label aprLabel = new Label("APRIL:  ");
			aprLabel.setUnderline(true);

			// Get the weight and percent for the month
			WeightPercentPair aprPair = farmReport.get(3);
			int aprWeight = aprPair.getMilkWeight();
			double aprPercent = aprPair.getPercent();

			HBox apr = new HBox();

			Label aprMilkWeight = new Label(aprWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar aprPB = new ProgressBar();
			aprPB.setProgress(aprPercent);

			Label febPercentage = new Label("  (" + df.format(aprPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				apr.getChildren().addAll(aprMilkWeight, aprPB, febPercentage);
			} else {
				apr.getChildren().addAll(aprMilkWeight);
			}

			// Adds the april elements to the center of the GUI
			column1.getChildren().addAll(blankSpace4, aprLabel, apr);
		}

		// May data
		if (farmReport.get(4).getMilkWeight() != 0) { // Checks for non-zero values

			// Month title elements
			Label blankSpace5 = new Label("");
			Label blankSpaceA = new Label("");
			Label mayLabel = new Label("MAY:  ");
			mayLabel.setUnderline(true);

			// Get the weight and percent for the month
			WeightPercentPair mayPair = farmReport.get(4);
			int mayWeight = mayPair.getMilkWeight();
			double mayPercent = mayPair.getPercent();

			HBox may = new HBox();

			Label mayMilkWeight = new Label(mayWeight + " lbs   ");

			// Progress bar to display the percent
			ProgressBar mayPB = new ProgressBar();
			mayPB.setProgress(mayPercent);

			Label febPercentage = new Label("  (" + df.format(mayPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				may.getChildren().addAll(mayMilkWeight, mayPB, febPercentage);
			} else {
				may.getChildren().addAll(mayMilkWeight);
			}

			// Adds the may elements to the GUI
			column1.getChildren().addAll(blankSpace5, mayLabel, may);
		}

		// June data
		if (farmReport.get(5).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace6 = new Label("");
			Label junLabel = new Label("JUNE:  ");
			junLabel.setUnderline(true);

			// Gets the weight and percent for month
			WeightPercentPair junPair = farmReport.get(5);
			int junWeight = junPair.getMilkWeight();
			double junPercent = junPair.getPercent();

			HBox jun = new HBox();

			Label junMilkWeight = new Label(junWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar febPB = new ProgressBar();
			febPB.setProgress(junPercent);

			Label febPercentage = new Label("  (" + df.format(junPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				jun.getChildren().addAll(junMilkWeight, febPB, febPercentage);
			} else {
				jun.getChildren().add(junMilkWeight);
			}

			// Adds the june elements to the center of the GUI
			column1.getChildren().addAll(blankSpace6, junLabel, jun);
		}

		// July data
		if (farmReport.get(6).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace7 = new Label("");
			Label julLabel = new Label("JULY:  ");
			julLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair julPair = farmReport.get(6);
			int julWeight = julPair.getMilkWeight();
			double julPercent = julPair.getPercent();

			HBox jul = new HBox();

			Label julMilkWeight = new Label(julWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar julPB = new ProgressBar();
			julPB.setProgress(julPercent);

			Label julPercentage = new Label("  (" + df.format(julPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				jul.getChildren().addAll(julMilkWeight, julPB, julPercentage);
			} else {
				jul.getChildren().add(julMilkWeight);
			}

			// Adds the july elements to the center of the GUI
			column1.getChildren().addAll(blankSpace7, julLabel, jul);
		}

		// August data
		if (farmReport.get(7).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace8 = new Label("");
			Label augLabel = new Label("AUGUST:  ");
			augLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair augPair = farmReport.get(7);
			int augWeight = augPair.getMilkWeight();
			double augPercent = augPair.getPercent();

			HBox aug = new HBox();

			Label augMilkWeight = new Label(augWeight + " lbs   ");

			// Progress bar elements to display the elements
			ProgressBar augPB = new ProgressBar();
			augPB.setProgress(augPercent);

			Label augPercentage = new Label("  (" + df.format(augPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				aug.getChildren().addAll(augMilkWeight, augPB, augPercentage);
			} else {
				aug.getChildren().add(augMilkWeight);
			}

			// Adds the august elements to the center of the GUI
			column1.getChildren().addAll(blankSpace8, augLabel, aug);
		}

		// (Wake me up when) September (ends) Data
		if (farmReport.get(8).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace9 = new Label("");
			Label blankSpaceB = new Label("");
			Label sepLabel = new Label("SEPTEMBER:  ");
			sepLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair sepPair = farmReport.get(8);
			int sepWeight = sepPair.getMilkWeight();
			double sepPercent = sepPair.getPercent();

			HBox sep = new HBox();

			Label sepMilkWeight = new Label(sepWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar sepPB = new ProgressBar();
			sepPB.setProgress(sepPercent);

			Label sepPercentage = new Label("  (" + df.format(sepPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				sep.getChildren().addAll(sepMilkWeight, sepPB, sepPercentage);
			} else {
				sep.getChildren().add(sepMilkWeight);
			}

			// Adds the september elements to the center of the GUI
			column1.getChildren().addAll(blankSpace9, sepLabel, sep);
		}

		// October data
		if (farmReport.get(9).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace10 = new Label("");
			Label octLabel = new Label("OCTOBER:  ");
			octLabel.setUnderline(true);

			// Gets the Weight and percent for the month
			WeightPercentPair octPair = farmReport.get(9);
			int octWeight = octPair.getMilkWeight();
			double octPercent = octPair.getPercent();

			HBox oct = new HBox();

			Label octMilkWeight = new Label(octWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar octPB = new ProgressBar();
			octPB.setProgress(octPercent);

			Label octPercentage = new Label("  (" + df.format(octPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				oct.getChildren().addAll(octMilkWeight, octPB, octPercentage);
			} else {
				oct.getChildren().add(octMilkWeight);
			}

			// Adds the october elements to the center of the GUI
			column1.getChildren().addAll(blankSpace10, octLabel, oct);
		}

		// November data
		if (farmReport.get(10).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elements
			Label blankSpace11 = new Label("");
			Label novLabel = new Label("NOVEMBER:  ");
			novLabel.setUnderline(true);

			// Get the weight and percent of the month
			WeightPercentPair novPair = farmReport.get(10);
			int novWeight = novPair.getMilkWeight();
			double novPercent = novPair.getPercent();

			HBox nov = new HBox();

			Label novMilkWeight = new Label(novWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar novPB = new ProgressBar();
			novPB.setProgress(novPercent);

			Label novPercentage = new Label("  (" + df.format(novPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				nov.getChildren().addAll(novMilkWeight, novPB, novPercentage);
			} else {
				nov.getChildren().add(novMilkWeight);
			}

			// Adds the november elements to the center of the GUI
			column1.getChildren().addAll(blankSpace11, novLabel, nov);
		}

		// December data
		if (farmReport.get(11).getMilkWeight() != 0) { // Checks for non-zero weights

			// Month title elemtents
			Label blankSpace12 = new Label("");
			Label decLabel = new Label("DECEMBER:  ");
			decLabel.setUnderline(true);

			// Gets the weight and percent for the month
			WeightPercentPair decPair = farmReport.get(11);
			int decWeight = decPair.getMilkWeight();
			double decPercent = decPair.getPercent();

			HBox dec = new HBox();

			Label decMilkWeight = new Label(decWeight + " lbs   ");

			// Progress bar elements to display the percent
			ProgressBar decPB = new ProgressBar();
			decPB.setProgress(decPercent);

			Label decPercentage = new Label("  (" + df.format(decPercent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				dec.getChildren().addAll(decMilkWeight, decPB, decPercentage);
			} else {
				dec.getChildren().add(decMilkWeight);
			}

			// Adds the december elements to the center of the GUI
			column1.getChildren().addAll(blankSpace12, decLabel, dec);
		}
		column1.getChildren().add(saveReport);
		
		// Clears the center and then adds all the month elements
		center.getChildren().clear();
		center.getChildren().add(column1);

		// Event handling for the save farm report button
		saveReport.setOnAction(e -> {
			// Helper method to save the farm report data to a file
			saveFarmReport(farmReport, farmID, year);
		});
	}

	/**
	 * Helper method that calls the farm report formater
	 * 
	 * @param farmID the farm to display
	 * @param year   the year to get the data from
	 * @param center Vertical box to add the data to
	 * @throws FarmNotFoundException if the user inputed farmID is invalid
	 * @throws InvalidDateException  if the user inputed date is invalid
	 */
	public void farmReportButtonAction(String farmID, String year, VBox center)
			throws FarmNotFoundException, InvalidDateException {
		// Get the farms data
		HashMap<Integer, WeightPercentPair> farmReport = milkManager.farmReport(farmID, year);
		formatCenterFarmReport(center, farmReport, farmID, year);
	}

	/**
	 * Formats the center of the GUI to display data when the AnnualReport display
	 * data button is called
	 * 
	 * @param center       Vertical box to add the display data to
	 * @param annualReport HashMap containing all the necessary data
	 * @param year         the year of the display data
	 */
	public void formatCenterAnnualReport(VBox center, HashMap<String, WeightPercentPair> annualReport, String year) {

		// Create the first column of data
		VBox column1 = new VBox();

		// Year title elements
		Label title = new Label(year + ":");
		title.setUnderline(true);
		column1.getChildren().add(title);

		// Button elemetns
		Button saveReport = new Button("Save Report");

		// courtesy of https://mkyong.com/java8/java-8-how-to-sort-a-map/
		Map<String, WeightPercentPair> sorted = annualReport.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));

		// Iterates through the farm data
		for (Map.Entry<String, WeightPercentPair> entry : sorted.entrySet()) {
			Label farmName = new Label(entry.getKey());
			farmName.setUnderline(true);

			// Gets the weight and percent
			WeightPercentPair pair = annualReport.get(entry.getKey());
			int weight = pair.getMilkWeight();
			double percent = pair.getPercent();

			HBox arBox = new HBox();

			Label milkWeight = new Label(weight + " lbs   ");

			ProgressBar pb = new ProgressBar();
			pb.setProgress(percent);

			Label percentage = new Label("  (" + df.format(percent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				arBox.getChildren().addAll(milkWeight, pb, percentage);
			} else {
				arBox.getChildren().add(milkWeight);
			}
			column1.getChildren().addAll(farmName, arBox);
		}
		column1.getChildren().add(saveReport);
		
		// Clears the center of the GUI and then adds the data
		center.getChildren().clear();
		center.getChildren().add(column1);

		// Event handling for the the save report button
		saveReport.setOnAction(e -> {
			saveAnnualReport(annualReport, year);
		});
	}

	/**
	 * Helper method that calls a formating method to display the annual report
	 * 
	 * @param year   the year of the data to display
	 * @param center Vertical box to add the data to
	 * @throws InvalidDateException if the user input date is invalid
	 */
	public void annualReportButtonAction(String year, VBox center) throws InvalidDateException {
		HashMap<String, WeightPercentPair> annualReport = milkManager.annualReport(year);
		formatCenterAnnualReport(center, annualReport, year);
	}

	/**
	 * Formats the center of the GUI to display the monthly report
	 * 
	 * @param center        Vertical box to add the display data to
	 * @param monthlyReport HashMap holding all the necessary farm data
	 * @param month         the month of the data
	 * @param year          the year of the data
	 */
	public void formatCenterMonthlyReport(VBox center, HashMap<String, WeightPercentPair> monthlyReport, String month,
			String year) {

		// Create the first column of data
		VBox column1 = new VBox();

		Label title = new Label(month + ", " + year + ":");
		title.setUnderline(true);
		column1.getChildren().add(title);

		Button saveReport = new Button("Save Report");

		// courtesy of https://mkyong.com/java8/java-8-how-to-sort-a-map/
		Map<String, WeightPercentPair> sorted = monthlyReport.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));

		// Iterates through the data
		for (Map.Entry<String, WeightPercentPair> entry : sorted.entrySet()) {
			Label farmName = new Label(entry.getKey());
			farmName.setUnderline(true);

			// Gets the weight and percent
			WeightPercentPair pair = monthlyReport.get(entry.getKey());
			int weight = pair.getMilkWeight();
			double percent = pair.getPercent();

			HBox mrBox = new HBox();

			Label milkWeight = new Label(weight + " lbs   ");

			ProgressBar pb = new ProgressBar();
			pb.setProgress(percent);

			Label percentage = new Label("  (" + df.format(percent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				mrBox.getChildren().addAll(milkWeight, pb, percentage);
			} else {
				mrBox.getChildren().add(milkWeight);
			}
			column1.getChildren().addAll(farmName, mrBox);
		}
		column1.getChildren().add(saveReport);
		
		// Clears the center of the GUI and then adds the display data
		center.getChildren().clear();
		center.getChildren().add(column1);

		saveReport.setOnAction(e -> {
			saveMonthlyReport(monthlyReport, month, year);
		});
	}

	/**
	 * Helper method that calls a formater to display the month report data
	 * 
	 * @param month  the month of the data
	 * @param year   the year of the data
	 * @param center vertical box to hold the display data
	 * @throws FarmNotFoundException if the user inputed farmID is invalid
	 * @throws InvalidDateException  if the user inputed data is invalid
	 */
	public void monthlyReportButtonAction(String month, String year, VBox center)
			throws FarmNotFoundException, InvalidDateException {
		HashMap<String, WeightPercentPair> monthlyReport = milkManager.monthlyReport(month, year);
		formatCenterMonthlyReport(center, monthlyReport, month, year);
	}

	/**
	 * Formats the center of the GUI to display the date range report
	 * 
	 * @param center          Vertical box to hold the display data
	 * @param dateRangeReport HashMap that holds all the necessary farm data
	 * @param startDate       the start date of the data
	 * @param endDate         end date of the data
	 * @throws InvalidDateException         if the user inputed data is invalid
	 * @throws StartDateOutOfRangeException if the user inputed start date is
	 *                                      invalid
	 * @throws EndDateOutOfRangeException   if the user input end date is invalid
	 */
	public void formatCenterDateRangeReport(VBox center, HashMap<String, WeightPercentPair> dateRangeReport,
			String startDate, String endDate)
			throws InvalidDateException, StartDateOutOfRangeException, EndDateOutOfRangeException {
		// check if start date is after end date
		MilkDate startDateVerify = new MilkDate(startDate);
		MilkDate endDateVerify = new MilkDate(endDate);

		if (startDateVerify.compareTo(endDateVerify) > 0)
			throw new InvalidDateException();

		if (startDateVerify.compareTo(milkManager.minDate) < 0 || startDateVerify.compareTo(milkManager.maxDate) > 0)
			throw new StartDateOutOfRangeException();

		if (endDateVerify.compareTo(milkManager.maxDate) > 0 || endDateVerify.compareTo(milkManager.minDate) < 0)
			throw new EndDateOutOfRangeException();

		// Create the first column of data
		VBox column1 = new VBox();

		Label title = new Label(startDate + " - " + endDate);
		title.setUnderline(true);
		column1.getChildren().add(title);

		Button saveReport = new Button("Save Report");

		// courtesy of https://mkyong.com/java8/java-8-how-to-sort-a-map/
		Map<String, WeightPercentPair> sorted = dateRangeReport.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));

		// Iterates through the data
		for (Map.Entry<String, WeightPercentPair> entry : sorted.entrySet()) {
			Label farmName = new Label(entry.getKey());
			farmName.setUnderline(true);

			// Gets the weight and percent
			WeightPercentPair pair = dateRangeReport.get(entry.getKey());
			int weight = pair.getMilkWeight();
			double percent = pair.getPercent();

			HBox drrBox = new HBox();

			Label milkWeight = new Label(weight + " lbs   ");

			ProgressBar pb = new ProgressBar();
			pb.setProgress(percent);

			Label percentage = new Label("  (" + df.format(percent * 100) + "%)");

			// Only displays the percent if the display percentages check box is checked
			if (displayPercentages) {
				drrBox.getChildren().addAll(milkWeight, pb, percentage);
			} else {
				drrBox.getChildren().add(milkWeight);
			}
			column1.getChildren().addAll(farmName, drrBox);
		}
		column1.getChildren().add(saveReport);
		
		// Clears and then adds the display data to the center of the GUI
		center.getChildren().clear();
		center.getChildren().add(column1);

		// Event handling for the save report button
		saveReport.setOnAction(e -> {
			saveDateRangeReport(dateRangeReport, startDate, endDate);
		});
	}

	/**
	 * Helper method that calls a method to format the center of the GUI to display
	 * the date range report data
	 * 
	 * @param startDate start date of the data
	 * @param endDate   end date of the data
	 * @param center    Verticle box to hold the display data
	 * @throws FarmNotFoundException        if the user inputed farmID is invalid
	 * @throws InvalidDateException         if the user inpued date is invalid
	 * @throws StartDateOutOfRangeException
	 * @throws EndDateOutOfRangeException
	 */
	public void dateRangeReportButtonAction(String startDate, String endDate, VBox center) throws FarmNotFoundException,
			InvalidDateException, StartDateOutOfRangeException, EndDateOutOfRangeException {
		HashMap<String, WeightPercentPair> dateRangeReport = milkManager.dateRangeReport(startDate, endDate);
		formatCenterDateRangeReport(center, dateRangeReport, startDate, endDate);
	}

	/**
	 * Creates a pop up when the user presses the edit data button
	 * 
	 * @param farmID       the farm to edit
	 * @param date         the date to edit
	 * @param primaryStage main GUI element
	 */
	public void editDataButtonPopUp(String farmID, MilkDate date, Stage primaryStage) {

		Label exceptionLabel = new Label("");
		Label blankSpace1 = new Label("");

		// Check for previous exceptions
		if (negativeMilkWeightExceptionBool) {
			negativeMilkWeightExceptionBool = false;
			exceptionLabel.setText("Enter a Positive MilkWeight");
		}

		// Add the necessary fields and boxes to the window
		ObservableList<String> list = FXCollections.observableArrayList("Edit Milk Entry", "Remove Milk Entry");
		ComboBox<String> box = new ComboBox<String>(list);
		box.setPromptText("Select Edit Method");

		Label textLabel = new Label("Enter New Amount (Leave blank if removing data entry)");
		TextField milkAmountField = new TextField();
		Button editButton = new Button("Finish Editing");
		Label blankSpace = new Label("");

		HBox buttonBox = new HBox();
		buttonBox.getChildren().addAll(editButton);

		VBox windowBox = new VBox();
		windowBox.getChildren().addAll(exceptionLabel, blankSpace1, box, blankSpace, textLabel, milkAmountField,
				buttonBox);

		// Create new popup window
		GridPane form = new GridPane();
		Scene newScene = new Scene(form, 600, 200);
		final Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setScene(newScene);
		dialog.show();
		form.getChildren().add(windowBox);

		// Close the window on button press
		editButton.setOnAction(e -> {
			try {
				if (box.getValue().equals("Edit Milk Entry")) {
					int milkValue = Integer.parseInt(milkAmountField.getText());
					milkManager.addMilk(farmID, date, milkValue);
				} else if (box.getValue().equals("Remove Milk Entry")) {
					milkManager.removeMilk(farmID, date);
				}
				dialog.close();
				Alert alert = new Alert(AlertType.CONFIRMATION,
						"Data edited successfully (must redisplay to take affect).", ButtonType.OK);
				alert.showAndWait();
			} catch (FarmNotFoundException e1) {
				farmNotFoundExceptionBool = true;
				dialog.close();
			} catch (NegativeMilkWeightException e2) {
				negativeMilkWeightExceptionBool = true;
				dialog.close();
				editDataButtonPopUp(farmID, date, primaryStage);
			} catch (MissingDataException e1) {
				Alert alert = new Alert(AlertType.WARNING,
						"Cannot remove milk weight data for this date because it does not exist. Please enter valid date.",
						ButtonType.OK);
				alert.showAndWait();
			}
		});
	}

	/**
	 * Button action when all farms button is pressed
	 * 
	 * @param farmID farm name selected
	 * @param year year selected
	 * @param displayChoice choice of the display
	 * @param ceneter panel to use
	 */
	private void displayIndividualFarmsButtonAction(String farmID, String year, String displayChoice, VBox Center)
			throws InvalidDateException {
		// Get the milk weight data
		HashMap<Integer, WeightPercentPair> farmData = milkManager.farmReport(farmID, year);
		formatCenterDisplayIndividualFarms(Center, farmData, farmID, year, displayChoice);
	}

	/**
	 * Formatter for the center panel when an individual farm summary is called
	 * 
	 * @param Center panel to use
	 * @param farmData data to use
	 * @param farmID farm name selected
	 * @param year year selected
	 * @param displayChoice choice of the display
	 */
	private void formatCenterDisplayIndividualFarms(VBox Center, HashMap<Integer, WeightPercentPair> farmData,
			String farmID, String year, String displayChoice) {

		// Parse the user's display choice
		int choice = -1;
		if (displayChoice.equals("Minimum")) {
			choice = 0;
		} else if (displayChoice.equals("Maximum")) {
			choice = 1;
		} else if (displayChoice.equals("Average")) {
			choice = 2;
		}

		// Find the min or max milkWeight
		int displayData = farmData.get(Calendar.JANUARY).getMilkWeight();
		int total = 0;
		double percent = 0;
		String month = "";
		for (int i = 0; i < farmData.size(); i++) {
			int curr = farmData.get(i).getMilkWeight();

			// Find min val
			if (choice == 0) {
				if (curr < displayData) {
					displayData = curr;
					month = Integer.toString(i);
					percent = farmData.get(i).getPercent();
				}
			}

			// Find max value
			if (choice == 1) {
				if (curr > displayData) {
					displayData = curr;
					month = Integer.toString(i);
					percent = farmData.get(i).getPercent();
				}
			}

			// Get the total milk weight value
			total += farmData.get(0).getMilkWeight();
		}

		// Find the average milk weight
		if (choice == 2) {
			displayData = total / 12;
		}

		// Find the month of the min/max val
		else {
			if (month.equals("0"))
				month = "January";
			else if (month.equals("1"))
				month = "February";
			else if (month.equals("2"))
				month = "March";
			else if (month.equals("3"))
				month = "April";
			else if (month.equals("4"))
				month = "May";
			else if (month.equals("5"))
				month = "June";
			else if (month.equals("6"))
				month = "July";
			else if (month.equals("7"))
				month = "August";
			else if (month.equals("8"))
				month = "September";
			else if (month.equals("9"))
				month = "October";
			else if (month.equals("10"))
				month = "November";
			else if (month.equals("11"))
				month = "December";
		}

		// create label
		Label yearLabel = new Label(farmID + " - " + year + ":");
		yearLabel.setUnderline(true);
		Label blankSpace = new Label("");
		Label choiceLabel = new Label("");
		Label percentage = new Label("  (" + df.format(percent * 100) + "%)");

		Button saveReport = new Button("Save Report");

		// select choice
		if (choice == 0) {
			choiceLabel.setText("Minimum Value Month: " + month + ": " + displayData + " lbs ");
		} else if (choice == 1) {
			choiceLabel.setText("Maximum Value Month: " + month + ": " + displayData + " lbs ");
		} else if (choice == 2) {
			choiceLabel.setText("Average Value: " + displayData + " lbs");
		}

		// Clear center panel
		Center.getChildren().clear();

		if (displayPercentages && choice != 2) {
			HBox hBox = new HBox(choiceLabel, percentage);
			Center.getChildren().addAll(yearLabel, blankSpace, hBox);
		} else {
			Center.getChildren().addAll(yearLabel, blankSpace, choiceLabel);
		}
		
		// add save to output button
		Center.getChildren().add(saveReport);
		
		saveReport.setOnAction(e -> {
			saveIndvidualFarm(farmData, farmID, year, displayChoice);
		});
	}
	
	/**
	 * Button action when all farms button is pressed
	 * 
	 * @param month month selected
	 * @param year year selected
	 * @param displayChoice choice of the display
	 * @param ceneter panel to use
	 */
	private void displayForAllFarmsButtonAction(String month, String year, String displayChoice, VBox Center) {
		// Get the farm data
		HashMap<String, WeightPercentPair> farmData = null;
		try {
			farmData = milkManager.monthlyReport(month, year);
		} catch (InvalidDateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		formatCenterDisplayForAllFarms(Center, farmData, month, year, displayChoice);
	}

	/**
	 * Formatter for the center panel when a all farm summary is called
	 * 
	 * @param Center panel to use
	 * @param farmData data to use
	 * @param month month selected
	 * @param year year selected
	 * @param displayChoice choice of the display
	 */
	private void formatCenterDisplayForAllFarms(VBox Center, HashMap<String, WeightPercentPair> farmData, String month,
			String year, String displayChoice) {

		// Parse the user's display choice
		int choice = -1;
		if (displayChoice.equals("Minimum")) {
			choice = 0;
		} else if (displayChoice.equals("Maximum")) {
			choice = 1;
		} else if (displayChoice.equals("Average")) {
			choice = 2;
		}

		// Find the min/max of the milk weights for the month
		List<String> farmList = milkManager.getFarms();
		String farmIDData = farmList.get(0);
		int displayData = farmData.get(farmIDData).getMilkWeight();
		int totalWeight = 0;
		double percent = 0;
		String farmID;

		for (int i = 0; i < farmList.size(); i++) {
			int curr = farmData.get(farmList.get(i)).getMilkWeight();

			// Find min val
			if (choice == 0) {
				if (curr < displayData) {
					displayData = curr;
					percent = farmData.get(farmList.get(i)).getPercent();
					farmIDData = farmList.get(i);
				}
			}

			// Find max val
			if (choice == 1) {
				if (curr > displayData) {
					curr = displayData;
					percent = farmData.get(farmList.get(i)).getPercent();
					farmIDData = farmList.get(i);
				}
			}

			// Get the total weight for the average
			totalWeight += farmData.get(farmList.get(i)).getMilkWeight();
		}

		if (choice == 2) {
			displayData = totalWeight / 12;
		}

		Label titleLabel = new Label(month + ", " + year);
		titleLabel.setUnderline(true);
		Label blankSpace = new Label("");
		Label choiceLabel = new Label("");
		Label percentage = new Label("  (" + df.format(percent * 100) + "%)");

		Button saveReport = new Button("Save Report");
		
		if (choice == 0) {
			choiceLabel.setText("Year Minimum: " + farmIDData + ": " + displayData + " lbs ");
		} else if (choice == 1) {
			choiceLabel.setText("Year Maximum: " + farmIDData + ": " + displayData + " lbs ");
		} else if (choice == 2) {
			choiceLabel.setText("Year Average: " + displayData + " lbs");
		}

		Center.getChildren().clear();

		if (displayPercentages && choice != 2) {
			HBox hBox = new HBox(choiceLabel, percentage);
			Center.getChildren().addAll(titleLabel, blankSpace, hBox);
		} else {
			Center.getChildren().addAll(titleLabel, blankSpace, choiceLabel);
		}
		
		// add save to output button
		Center.getChildren().add(saveReport);
		
		saveReport.setOnAction(e -> {
			saveAllFarms(farmData, month, year, displayChoice);
		});
	}

	/**
	 * Writes the content of the annual report to a file
	 * 
	 * @param farmReport data to use
	 * @param farmID     farm name
	 * @param year       year to use
	 */
	private void saveFarmReport(HashMap<Integer, WeightPercentPair> farmReport, String farmID, String year) {
		// log the data
		File file = new File(farmID + " - " + year + "Farm_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);

			String janLabel = "JANUARY: ";
			WeightPercentPair janPair = farmReport.get(0);
			int janWeight = janPair.getMilkWeight();
			double janPercent = janPair.getPercent();
			String january = janLabel + janWeight + " lbs," + " (" + df.format(janPercent * 100) + "%)\n";

			String febLabel = "FEBRUARY: ";
			WeightPercentPair febPair = farmReport.get(1);
			int febWeight = febPair.getMilkWeight();
			double febPercent = febPair.getPercent();
			String february = febLabel + febWeight + " lbs," + " (" + df.format(febPercent * 100) + "%)\n";

			String marLabel = "MARCH: ";
			WeightPercentPair marPair = farmReport.get(2);
			int marWeight = marPair.getMilkWeight();
			double marPercent = marPair.getPercent();
			String march = marLabel + marWeight + " lbs," + " (" + df.format(marPercent * 100) + "%)\n";

			String aprLabel = "APRIL: ";
			WeightPercentPair aprPair = farmReport.get(3);
			int aprWeight = aprPair.getMilkWeight();
			double aprPercent = aprPair.getPercent();
			String april = aprLabel + aprWeight + " lbs," + " (" + df.format(aprPercent * 100) + "%)\n";

			String mayLabel = "MAY: ";
			WeightPercentPair mayPair = farmReport.get(4);
			int mayWeight = mayPair.getMilkWeight();
			double mayPercent = mayPair.getPercent();
			String may = mayLabel + mayWeight + " lbs," + " (" + df.format(mayPercent * 100) + "%)\n";

			String junLabel = "JUNE: ";
			WeightPercentPair junPair = farmReport.get(5);
			int junWeight = junPair.getMilkWeight();
			double junPercent = junPair.getPercent();
			String june = junLabel + junWeight + " lbs," + " (" + df.format(junPercent * 100) + "%)\n";

			String julLabel = "JULY: ";
			WeightPercentPair julPair = farmReport.get(6);
			int julWeight = julPair.getMilkWeight();
			double julPercent = julPair.getPercent();
			String july = julLabel + julWeight + " lbs," + " (" + df.format(julPercent * 100) + "%)\n";

			String augLabel = "AUGUST: ";
			WeightPercentPair augPair = farmReport.get(7);
			int augWeight = augPair.getMilkWeight();
			double augPercent = augPair.getPercent();
			String august = augLabel + augWeight + " lbs," + " (" + df.format(augPercent * 100) + "%)\n";

			String sepLabel = "SEPTEMBER: ";
			WeightPercentPair sepPair = farmReport.get(8);
			int sepWeight = sepPair.getMilkWeight();
			double sepPercent = sepPair.getPercent();
			String september = sepLabel + sepWeight + " lbs," + " (" + df.format(sepPercent * 100) + "%)\n";

			String octLabel = "OCTOBER: ";
			WeightPercentPair octPair = farmReport.get(9);
			int octWeight = octPair.getMilkWeight();
			double octPercent = octPair.getPercent();
			String october = octLabel + octWeight + " lbs," + " (" + df.format(octPercent * 100) + "%)\n";

			String novLabel = "NOVEMBER: ";
			WeightPercentPair novPair = farmReport.get(10);
			int novWeight = novPair.getMilkWeight();
			double novPercent = novPair.getPercent();
			String november = novLabel + novWeight + " lbs," + " (" + df.format(novPercent * 100) + "%)\n";

			String decLabel = "DECEMBER: ";
			WeightPercentPair decPair = farmReport.get(11);
			int decWeight = decPair.getMilkWeight();
			double decPercent = decPair.getPercent();
			String december = decLabel + decWeight + " lbs," + " (" + df.format(decPercent * 100) + "%)\n";

			// write the data
			writer.write("Year: " + year + "\n");
			writer.write(january);
			writer.write(february);
			writer.write(march);
			writer.write(april);
			writer.write(may);
			writer.write(june);
			writer.write(july);
			writer.write(august);
			writer.write(september);
			writer.write(october);
			writer.write(november);
			writer.write(december);
			writer.close();
		} catch (IOException e1) {
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
			e1.printStackTrace();
		}
	}

	/**
	 * Writes the content of the annual report to a file
	 * 
	 * @param annualReport data to use
	 * @param year         year to use
	 */
	private void saveAnnualReport(HashMap<String, WeightPercentPair> annualReport, String year) {
		// log the data
		File file = new File(year + "_Annual_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);
			// write the data
			writer.write("Year: " + year + "\n");
			for (Map.Entry<String, WeightPercentPair> entry : annualReport.entrySet()) {
				WeightPercentPair pair = annualReport.get(entry.getKey());
				String farmName = new String(entry.getKey());
				int weight = pair.getMilkWeight();
				double percent = pair.getPercent();
				writer.write(farmName + ": " + weight + " (" + df.format(percent * 100) + "%)\n");
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
		}
	}

	/**
	 * Writes the content of the month report to a file
	 * 
	 * @param monthlyReport data to use
	 * @param month         month to use
	 * @param year          year to use
	 */
	private void saveMonthlyReport(HashMap<String, WeightPercentPair> monthlyReport, String month, String year) {
		// log the data
		File file = new File(month + "-" + year + "_Monthly_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);
			// write the data
			writer.write(month + "-" + year + "\n");
			for (Map.Entry<String, WeightPercentPair> entry : monthlyReport.entrySet()) {
				WeightPercentPair pair = monthlyReport.get(entry.getKey());
				String farmName = new String(entry.getKey());
				int weight = pair.getMilkWeight();
				double percent = pair.getPercent();
				writer.write(farmName + ": " + weight + " (" + df.format(percent * 100) + "%)\n");
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
		}
	}

	/**
	 * Writes the content of the date range report to a file
	 * 
	 * @param dateRangeReport data to use
	 * @param startDate       start date
	 * @param endDate         end date
	 */
	private void saveDateRangeReport(HashMap<String, WeightPercentPair> dateRangeReport, String startDate,
			String endDate) {
		// log the data
		File file = new File(startDate + " - " + endDate + "Date_Range_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);
			// write the data
			writer.write(startDate + "-" + endDate + "\n");
			for (Map.Entry<String, WeightPercentPair> entry : dateRangeReport.entrySet()) {
				WeightPercentPair pair = dateRangeReport.get(entry.getKey());
				String farmName = new String(entry.getKey());
				int weight = pair.getMilkWeight();
				double percent = pair.getPercent();
				writer.write(farmName + ": " + weight + " (" + df.format(percent * 100) + "%)\n");
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
		}
	}

	/**
	 * Writes the contents from individual farm summary to a file
	 * 
	 * @param farmData      data to examine
	 * @param farmID        farm name
	 * @param year          year to use
	 * @param displayChoice choice choosen by user
	 */
	private void saveIndvidualFarm(HashMap<Integer, WeightPercentPair> farmData, String farmID, String year,
			String displayChoice) {
		// log the data
		File file = new File(farmID + " - " + year + "-" + displayChoice + "_Date_Range_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);
			// write the data
			writer.write(farmID + " - " + year + ":\n");

			// Parse the user's display choice
			int choice = -1;
			if (displayChoice.equals("Minimum")) {
				choice = 0;
			} else if (displayChoice.equals("Maximum")) {
				choice = 1;
			} else if (displayChoice.equals("Average")) {
				choice = 2;
			}

			// Find the min or max milkWeight
			int displayData = farmData.get(Calendar.JANUARY).getMilkWeight();
			int total = 0;
			double percent = 0;
			String month = "";
			for (int i = 0; i < farmData.size(); i++) {
				int curr = farmData.get(i).getMilkWeight();

				// Find min val
				if (choice == 0) {
					if (curr < displayData) {
						displayData = curr;
						month = Integer.toString(i);
						percent = farmData.get(i).getPercent();
					}
				}

				// Find max value
				if (choice == 1) {
					if (curr > displayData) {
						displayData = curr;
						month = Integer.toString(i);
						percent = farmData.get(i).getPercent();
					}
				}
				// Get the total milk weight value
				total += farmData.get(0).getMilkWeight();
			}

			// Find the average milk weight
			if (choice == 2) {
				displayData = total / 12;
			}

			// Find the month of the min/max val
			else {
				if (month.equals("0"))
					month = "January";
				else if (month.equals("1"))
					month = "February";
				else if (month.equals("2"))
					month = "March";
				else if (month.equals("3"))
					month = "April";
				else if (month.equals("4"))
					month = "May";
				else if (month.equals("5"))
					month = "June";
				else if (month.equals("6"))
					month = "July";
				else if (month.equals("7"))
					month = "August";
				else if (month.equals("8"))
					month = "September";
				else if (month.equals("9"))
					month = "October";
				else if (month.equals("10"))
					month = "November";
				else if (month.equals("11"))
					month = "December";

				// write to file based on selection
				if (choice == 0) {
					writer.write("Minimum Value Month: " + month + ": " + displayData + " lbs " + " ("
							+ df.format(percent * 100) + "%)");
				} else if (choice == 1) {
					writer.write("Maximum Value Month: " + month + ": " + displayData + " lbs " + " ("
							+ df.format(percent * 100) + "%)");
				} else if (choice == 2) {
					writer.write("Average Value: " + displayData + " lbs" + " (" + df.format(percent * 100) + "%)");
				}
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
		}
	}

	/**
	 * Writes the content from all farm summary to a file
	 * 
	 * @param farmData      data to use
	 * @param month         selected month
	 * @param year          selected year
	 * @param displayChoice choice chosen to use
	 */
	private void saveAllFarms(HashMap<String, WeightPercentPair> farmData, String month, String year,
			String displayChoice) {
		// log the data
		File file = new File(month + "-" + year + "-" + displayChoice + "_Date_Range_Report.txt");
		try {
			FileWriter writer = new FileWriter(file);
			// write the data
			writer.write(month + ", " + year + ":\n");
			// Parse the user's display choice
			int choice = -1;
			if (displayChoice.equals("Minimum")) {
				choice = 0;
			} else if (displayChoice.equals("Maximum")) {
				choice = 1;
			} else if (displayChoice.equals("Average")) {
				choice = 2;
			}

			// Find the min/max of the milk weights for the month
			List<String> farmList = milkManager.getFarms();
			String farmIDData = farmList.get(0);
			int displayData = farmData.get(farmIDData).getMilkWeight();
			int totalWeight = 0;
			double percent = 0;
			String farmID;

			for (int i = 0; i < farmList.size(); i++) {
				int curr = farmData.get(farmList.get(i)).getMilkWeight();

				// Find min val
				if (choice == 0) {
					if (curr < displayData) {
						displayData = curr;
						percent = farmData.get(farmList.get(i)).getPercent();
						farmIDData = farmList.get(i);
					}
				}

				// Find max val
				if (choice == 1) {
					if (curr > displayData) {
						curr = displayData;
						percent = farmData.get(farmList.get(i)).getPercent();
						farmIDData = farmList.get(i);
					}
				}

				// Get the total weight for the average
				totalWeight += farmData.get(farmList.get(i)).getMilkWeight();
			}

			if (choice == 2) {
				displayData = totalWeight / 12;
			}

			// write to file based on input
			if (choice == 0) {
				writer.write("Year Minimum: " + month + ", " + year + ": " + displayData + " lbs " + " ("
						+ df.format(percent * 100) + "%)");
			} else if (choice == 1) {
				writer.write("Year Maximum: " + month + ", " + year + ": " + displayData + " lbs " + " ("
						+ df.format(percent * 100) + "%)");
			} else if (choice == 2) {
				writer.write("Year Average: " + displayData + " lbs" + " (" + df.format(percent * 100) + "%)");
			}
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.WARNING, "Error occurred while writing log", ButtonType.OK);
			alert.showAndWait();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
