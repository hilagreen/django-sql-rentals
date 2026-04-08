public class Doctor {
    /*
    class for the doctor object

    Initialise doctor object
    will contain comparable methods
     */

    private final String id;

    /*The Waiting Room: Ordered by arrival ticket (0, 1, 5...)
     Key: Int-> Arrival Ticket
     Value: String -> patient ID
    */
    private TwoThreeTree<Integer, String> waitingRoom;
//NOTE: id field is not needed, we access the id through the doctor tree key
    public Doctor(String id) {
        this.id = id;
        // Initializing the waiting room with Integer sentinels
       // TwoThreeTree.Node root = new TwoThreeTree.Node(Integer.MIN_VALUE, Integer.MAX_VALUE);
        this.waitingRoom = new TwoThreeTree<>(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public String getId() {
        return id;
    }
    /**
     * Adds a patient to the waiting room.
     * Takes O(log P) due to insertion where we add to back of tree
     * order by queuePositiom.
     * * @param patientId The ID of the patient to be inserted.
     * Store in the manager class for each patenut so that we can remove a patient by its ticketID
     * in manager after calling this method update field in WaitingRoom load
     */
    public void addPatientToWaitingRoom(int ticket, String patientId) {
        this.waitingRoom.insert(ticket, patientId);
    }

    /**
     * Removes a specific patient from the waiting room by their ticket
     * Used for the method patientLeaveEarly
     * Complexity: O(log P), we are given any patient in queue and have to remove it
     * * @param ticket The arrival ticket of the patient to remove.
     *      * in manager after calling this method update field in WaitingRoom load, call getCurrentLoad()
     */
    public void removePatientFromWaitingRoom(int queuePosition) {
        this.waitingRoom.delete(queuePosition);
    }
    /**
     * Retrieves and removes the next patient in line
     * Used for nextPatientLeave in manager class
     *  O(log P) to find and delete the minimum node.
     *  @return The ID of the removed patient, or null if empty
     *  in manager if null is returned remember to say the queue was empty for this doctor.
     */
    public String removeNextPatient() {
        // Find the patient that arrived first
        //this is the left most ticket before the sentinel
        // We use the sentinels so the real min is the successor of the MIN sentinel.
        //findMin returns the minimum before the sentinel

        // Check if waiting room is empty (only sentinels exist)

        if (this.waitingRoom.isEmpty()) return null;
        else{
            //returns the left most pateint before the sentinel
            String nextPatientID = this.waitingRoom.findMinVal();
            int nextPatientPosition = this.waitingRoom.findMinKey();

            // 2. Remove them from the tree

            //delete by key
            this.waitingRoom.delete(nextPatientPosition);
            return nextPatientID;
        }
    }
    /**
     * Returns the number of patients currently waiting.
     * Complexity: O(1) because the TwoThreeTree maintains subtreeSize.
     */
    public int getCurrentLoad() {
        // implementation doesn't count sentinels in subtreeSize.
        return this.waitingRoom.root.size;
    }
/*
@return next patient in line for this doctor
 */
    public String getNextPatientID() {
        //returns the value of the leftest Node in 2-3 tree, which is the ID of patient next in line.
        return this.waitingRoom.findMinVal();
    }




}
