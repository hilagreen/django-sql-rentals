/*
Uncertainty, accessing fields v.s. nodes and how to switch between them. i assume it will be Node.property
 */

public class ClinicManager {
    public static final String MIN_ID = "";
    public static final String MAX_ID = "\uFFFF\uFFFF\uFFFF\uFFFF";
    private int queuePosition = 0;

    TwoThreeTree<String, Doctor> doctorsByID;
    // Tree 2: Maps PatientID -> DoctorID
    // Standard 2-3 Tree
    TwoThreeTree<String, Patient> patientsByID;
    LoadTree doctorsByLoad;

    // Tree 3: Maps (Load, DocID) -> DocID
    // MUST be the "Augmented" version (Stat = Integer/Double) for Range queries
    // Let's assume you implemented the "SumTree" variant discussed earlier

    public ClinicManager() {
        doctorsByID = new TwoThreeTree<>(MIN_ID, MAX_ID);
        patientsByID = new TwoThreeTree<>(MIN_ID, MAX_ID);
        doctorsByLoad = new LoadTree();
    }


    public void doctorEnter(String doctorId) {
        //validation check to ensure this doctor does not already exist
        //-->have a Doctor 2-3 tree object and call up its search method
        //if exists use the add method we will have in 2-3 tree class for a general 2-3 tree.
        if ((doctorId.compareTo(MIN_ID) > 0) && (doctorId.compareTo(MAX_ID) < 0)) {
            if (this.doctorsByID.searchByKey(doctorId) == null) {

                Doctor newDoctorValue = new Doctor(doctorId);
                this.doctorsByID.insert(doctorId, newDoctorValue);
                // Add to load tree with 0 load
                this.doctorsByLoad.insert(new DoctorLoadKey(0, doctorId), null);

            }
            //this.doctorsByID.searchByKey(doctorId) + is already in the system
            else throw new IllegalArgumentException("ILLEGAL");
            //"Invalid DoctorID"
        } else throw new IllegalArgumentException("ILLEGAL");
    }

    //removes a doc based off of his ID
    //only if empty waiting room
    public void doctorLeave(String doctorId) {
        //Ensure doc is in system use search algorithim from general 2-3 tree +logn
        //Check if waiting room is empty.
        // use check existence method from general tree class
        //if all conditions are satisfied call general 2-3 tree delete algorithim +logn
        Doctor docToGo = this.doctorsByID.searchByKey(doctorId);
        if (docToGo != null) {
            if (docToGo.getCurrentLoad() == 0) {
                doctorsByID.delete(doctorId);
                doctorsByLoad.delete(new DoctorLoadKey(0, doctorId));
            }
            //"cannot delete doctor who has a patient in his waiting room"
            else throw new IllegalArgumentException("ILLEGAL");
        }
        //"Doctor does not exist in system"
        else throw new IllegalArgumentException("ILLEGAL");
    }

    public void patientEnter(String doctorId, String patientId) {
/*insert for patient: inserts the patient by patientID and then has a second doctorID attribute,
when this happens we need to put it in the doctors waiting list with a tracker variable by updating the patient count of that doctor.
Waiting list is ordered by date - oldest to newest, then by patient ID
Check existance of patient in that list already:{if in DId.waitingRoom has patient x dont add to system.
 */
        if ((patientId.compareTo(MIN_ID) > 0) && (patientId.compareTo(MAX_ID) < 0)) {
            if (this.patientsByID.searchByKey(patientId) == null) {
                Doctor doc = this.doctorsByID.searchByKey(doctorId);
                if (doc != null) {
                    // a patient not in the system for Patients will not belong to any waiting room
                    queuePosition++;
                    Patient patientValue = new Patient(doctorId, queuePosition);
                    //add patient to patient tree
                    //add patient to their doctors queue
                    ;

                    //  Remove doctor from Load Tree (using OLD load)
                    int oldLoad = doc.getCurrentLoad();
                    this.doctorsByLoad.delete(new DoctorLoadKey(oldLoad, doctorId));

                    //insert patient into PatientTree and to their docs waiting room
                    this.patientsByID.insert(patientId, patientValue);
                    doc.addPatientToWaitingRoom(queuePosition, patientId);

                    //  Re-insert doctor into Load Tree with new load
                    int newLoad = doc.getCurrentLoad();
                    this.doctorsByLoad.insert(new DoctorLoadKey(newLoad, doctorId), null);

                } else throw new IllegalArgumentException("ILLEGAL");
                //the doctor requested by the user, is not in the syste.
            } else throw new IllegalArgumentException("ILLEGAL");
            // PATIENT is already in the system"
        }
        //"Invalid PatientID"
        else throw new IllegalArgumentException("ILLEGAL");
    }

    /*
    removes next patient in line for a specicic doctors waiting room
    @return the patientID of patient to be removed
     */
    public String nextPatientLeave(String doctorId) {
        Doctor doc = this.doctorsByID.searchByKey(doctorId);
        //Doctor does not exist in system
        if (doc == null) throw new IllegalArgumentException("ILLEGAL");
        //waiting room is empty"
        if (doc.getCurrentLoad() == 0) throw new IllegalArgumentException("ILLEGAL");
        else {
            //REmoving the docotr with current load from load tree
            int oldLoad = doc.getCurrentLoad();
            this.doctorsByLoad.delete(new DoctorLoadKey(oldLoad, doctorId));

            //patient removed from their doctor waiting room
            String nextPatient = doc.removeNextPatient();
            //patient removed from Patient Tree
            this.patientsByID.delete(nextPatient);

            // insert doctor into Load Tree with new load
            int newLoad = doc.getCurrentLoad();
            this.doctorsByLoad.insert(new DoctorLoadKey(newLoad, doctorId), null);

            return nextPatient;

        }
    }
    /*
    removes the next patient in line which is just the leftest leaf cos entered with that condition th later date is added to back
    and then delete from q
    call delete on this node from TwoThreeTree
    delete from patient tree
    return: {patientID} of node that was removed.

     */

    public void patientLeaveEarly(String patientId) {
    /*
    delete this patient by PId from patient tree
    CHECK THAT IT EXISTS THERE FIRST
    1. search by pid , get the Did it belonged to
    2. Take the node, get its doctorID property
    3. go to DoctorTree,search by Did
    4. in his Waiting room array, delete by patientID
    5. in PD tree, reduce numpatient count for that doctor
     */
        Patient patientValue = this.patientsByID.searchByKey(patientId);
        //"patient does not exist in system"
        if (patientValue == null) throw new IllegalArgumentException("ILLEGAL");
        else {

            //if patient is in the system then we know that his doctor is also in the system and that the pateint is in his waiting room
            String docID = patientValue.doctorID;
            Doctor doctorValue = this.doctorsByID.searchByKey(docID);

            //remove patient from loadTree
            int currentLoad = doctorValue.getCurrentLoad();
            this.doctorsByLoad.delete(new DoctorLoadKey(currentLoad, docID));
            //remove patient from Waiting Room
            doctorValue.removePatientFromWaitingRoom(patientValue.queuePosition);
            //put back into load tree now with updated Load
            int newLoad = doctorValue.getCurrentLoad();
            this.doctorsByLoad.insert(new DoctorLoadKey(newLoad, docID), null);


            //remove patient from system
            this.patientsByID.delete(patientId);
        }
    }

    public int numPatients(String doctorId) {
        /*
        we will have a doctorId with property patient count/waiting roon tree size.
        search by DId and return his patient count
         */
        Doctor doctorValue = this.doctorsByID.searchByKey(doctorId);
        if (doctorValue != null) return doctorValue.getCurrentLoad();
        //"doctor does not exist in system"
        throw new IllegalArgumentException("ILLEGAL");
    }

    public String nextPatient(String doctorId) {
        Doctor doctorValue = this.doctorsByID.searchByKey(doctorId);
        if (doctorValue == null) throw new IllegalArgumentException("ILLEGAL");
        if (doctorValue.getCurrentLoad() == 0) {
            //the doctors waiting room is empty so there is no one waiting
            throw new IllegalArgumentException("ILLEGAL");
        } else {
            String nextPatient = doctorValue.getNextPatientID();
            return nextPatient;
        }
    }

    public String waitingForDoctor(String patientId) {
        //using search by patientID and then just accessing its DoctorID
        Patient nextPatient = patientsByID.searchByKey(patientId);
        if (nextPatient == null) throw new IllegalArgumentException("ILLEGAL");
        return nextPatient.getDoctorID();
    }

    public int numDoctorsWithLoadInRange(int low, int high) {

        DoctorLoadKey minKey = new DoctorLoadKey(low, ClinicManager.MIN_ID);
        DoctorLoadKey maxKey = new DoctorLoadKey(high, ClinicManager.MAX_ID);

        if (doctorsByLoad.getSizeInRange(minKey, maxKey) == 0) return 0;

        return doctorsByLoad.getSizeInRange(minKey, maxKey);
    }

    public int averageLoadWithinRange(int low, int high) {
        int numDoctorsWithLoadInRange = numDoctorsWithLoadInRange(low, high);
        if (numDoctorsWithLoadInRange == 0) return 0;
        //this is the sum of all the patients in each doctors waiting room of doctors having num patients between b and a

        DoctorLoadKey minKey = new DoctorLoadKey(low, ClinicManager.MIN_ID);
        DoctorLoadKey maxKey = new DoctorLoadKey(high, ClinicManager.MAX_ID);

        int totalPatientInWaitingRooms = doctorsByLoad.getSumInRange(minKey, maxKey);
        int averageLoad = totalPatientInWaitingRooms / numDoctorsWithLoadInRange;
        return totalPatientInWaitingRooms / numDoctorsWithLoadInRange;
    }
}