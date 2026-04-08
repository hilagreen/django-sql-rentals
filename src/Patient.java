public class Patient {
    /*
    class for the patient value
     */
    String doctorID;
    int queuePosition;

    public Patient(String doctorID, int queuePosition) {
        this.doctorID = doctorID;
        this.queuePosition = queuePosition;
    }
    public String getDoctorID() {
        return doctorID;
    }
}
