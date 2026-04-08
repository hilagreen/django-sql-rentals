public class LoadTree extends TwoThreeTree<DoctorLoadKey, String> {

    // Constructor
    public LoadTree() {
        // Initialize tree with Sentinels (using Integer MIN/MAX for queueSize)
        super(new DoctorLoadKey(Integer.MIN_VALUE, ""), new DoctorLoadKey(Integer.MAX_VALUE, ""));
    }

    // Override to maintain the 'subTreeSum' field
    @Override
    protected void updateAdditionalNodeData(Node node) {
        // Case 1: Leaf Node
        if (node.isLeaf) {
            // Check if it is a Sentinel
            if (node.key.queueSize == Integer.MIN_VALUE || node.key.queueSize == Integer.MAX_VALUE) {
                node.sum = 0; // Sentinels do not contribute to the sum
            } else {
                // Extract the queueSize from the key tuple
                node.sum = node.key.queueSize;
            }
        }
        // Case 2: Internal Node
        else {
            // The sum is the aggregation of all children's sums
            node.sum = 0;
            if (node.left != null) node.sum += node.left.sum;
            if (node.middle != null) node.sum += node.middle.sum;
            if (node.right != null) node.sum += node.right.sum;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Recursive implementation based on "Sum Of Smaller" algorithm from tutorial


    public int getSumInRange(DoctorLoadKey low, DoctorLoadKey high) {
        return getSumUpTo(high) - getSumStrictlyLessThan(low);
    }

    // --- Helper 1: Sum elements <= limit (Inclusive) ---

    private int getSumUpTo(DoctorLoadKey limit) {
        return getSumUpToRec(root, limit);
    }

    private int getSumUpToRec(Node x, DoctorLoadKey limit) {
        // 1. Stop condition: Leaf
        if (x.isLeaf) {
            // Check if leaf key is <= limit
            if (x.key.compareTo(limit) <= 0) {
                return x.sum; // Return the sum (load) stored in this node
            } else {
                return 0;
            }
        }

        // 2. Recursive Step (Standard navigation)

        // Case A: Limit is within/smaller than left child max
        if (limit.compareTo(x.left.key) <= 0) {
            return getSumUpToRec(x.left, limit);
        }
        // Case B: Limit is within/smaller than middle child max
        else if (x.right == null || limit.compareTo(x.middle.key) <= 0) {
            return x.left.sum + getSumUpToRec(x.middle, limit);
        }
        // Case C: Limit is in right child
        else {
            return x.left.sum + x.middle.sum + getSumUpToRec(x.right, limit);
        }
    }

    // --- Helper 2: Sum elements < limit (Strictly Less) ---
    // This replaces the need for "low - 1" logic

    private int getSumStrictlyLessThan(DoctorLoadKey limit) {
        return getSumStrictlyLessThanRec(root, limit);
    }

    private int getSumStrictlyLessThanRec(Node x, DoctorLoadKey limit) {
        // 1. Stop condition: Leaf
        if (x.isLeaf) {
            // DIFFERENT LOGIC HERE: Strictly Less (<)
            if (x.key.compareTo(limit) < 0) {
                return x.sum;
            } else {
                return 0;
            }
        }

        // 2. Recursive Step (Identical navigation logic to Inclusive version)
        // We compare against the max keys of subtrees to decide path

        if (limit.compareTo(x.left.key) <= 0) {
            return getSumStrictlyLessThanRec(x.left, limit);
        }
        else if (x.right == null || limit.compareTo(x.middle.key) <= 0) {
            return x.left.sum + getSumStrictlyLessThanRec(x.middle, limit);
        }
        else {
            return x.left.sum + x.middle.sum + getSumStrictlyLessThanRec(x.right, limit);
        }
    }

}