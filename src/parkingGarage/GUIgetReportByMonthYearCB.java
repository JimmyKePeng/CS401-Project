package parkingGarage;

public interface GUIgetReportByMonthYearCB {
	// OwnerGUI and OperatorGUI will use this function to search report by
	// garage/month/year
	Report run(int garageID, int month, int year);
}
