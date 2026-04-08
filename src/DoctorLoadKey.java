//LoadKey canjust be tree size and then we get the docID as value?
    // The "Key" for the doctorsByLoad Tree
// We sort by Count first, then by ID to ensure uniqueness.
    public class DoctorLoadKey implements Comparable<DoctorLoadKey> {

        int queueSize;
        final String doctorID;

        public DoctorLoadKey(int count, String docId) {
            this.queueSize = count;
            this.doctorID = docId;
        }
/*
ensure no doctor is inserted twice so our keys remain unique
 */
        @Override
        public int compareTo(DoctorLoadKey other) {
            if (this.queueSize != other.queueSize) {
                return Integer.compare(this.queueSize, other.queueSize);
            }
            return this.doctorID.compareTo(other.doctorID);
        }
    }

