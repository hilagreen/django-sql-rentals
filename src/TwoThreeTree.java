public class TwoThreeTree<Key extends Comparable<Key>, Value> {

    // Tree Foundation -----------------------------------------------------------------------------------------------
    protected class Node {
        // Pointers for tree structure
        Node left;
        Node middle;
        Node right;
        Node parent;

        // Data attributes
        Key key;      // Internal: Max key in subtree. Leaf: The key itself.
        Value value;  // Used ONLY for leaves (objects stored here)

        // Helper to know if it's a leaf
        boolean isLeaf;

        // Augmented fields (for later use in statistics)
        int size;
        int sum;

        // Constructor for a LEAF (creates an object)
        public Node(Key key, Value value) {
            this.isLeaf = true;
            this.key = key;
            this.value = value;
            this.size = 1;
        }

        // Constructor for an INTERNAL NODE
        public Node(Node left, Node middle, Node right) {
            this.isLeaf = false;
            this.left = left;
            this.middle = middle;
            this.right = right;
        }
    }

    protected Node root;

    //Tree Constructor
    public TwoThreeTree(Key minKey, Key maxKey){
        Node min = new Node(minKey, null);
        Node max = new Node(maxKey, null);

        // Creating root
        this.root = new Node(min, max, null);

        // connecting senttals to root
        min.parent = root;
        max.parent = root;

        min.size = 0;
        max.size = 0;
        // Updating root info
        updateInternalData(root);
    }



    // Public Interface  -------------------------------------------------------------------------------------------------

    // Search method to return sepcific value based in given key
    public Value searchByKey(Key key) {
        Node x = getNode(key);
        if (x==null) return null;
        else
            return x.value;
    }

    public boolean isEmpty() {
        return this.root.size == 0;
    }

    // Inserting a new Node
    public void insert(Key key, Value value) {
        //  Create the new leaf node (z)
        Node z = new Node(key, value);

        updateAdditionalNodeData(z);
        // 2. Navigate down to the correct leaf position
        // (We use the same logic as getNode/search)
        Node y = root;
        while (!y.isLeaf) {
            if (key.compareTo(y.left.key) <= 0) {
                y = y.left;
            } else if (y.right == null || key.compareTo(y.middle.key) <= 0) {
                y = y.middle;
            } else {
                y = y.right;
            }
        }

        // 3. Insert into the parent of the leaf (y.parent)
        // We start the split process from the parent
        Node x = y.parent;
        Node splitNode = insertAndSplit(x, z); // Attempt to insert z into x

        // 4. Propagate splits upwards (Go up the tree)
        while (x != root) {
            x = x.parent; // Move up

            if (splitNode != null) {
                // If the child split, we try to insert the new sibling into the current parent
                splitNode = insertAndSplit(x, splitNode);
            } else {
                // If no split happened, we just need to update the keys/sizes on the path up
                updateInternalData(x);
            }
        }

        // 5. Handle Root Split
        // If we finished the loop and splitNode is still not null, the root itself split.
        // We need a new root.
        if (splitNode != null) {
            Node newRoot = new Node(null, null, null);
            setChildren(newRoot, root, splitNode, null);
            this.root = newRoot;
        }
    }

    // Puclic function to deltete
    public void delete(Key key) {
        // finding node to deltet
        Node x = getNode(key);

        // In case it does not exist
        if (x == null) return;

        // Calling helper function to delte the node
        this.twoThreeDelete(x);
    }


    // Public functions to find min Key
    public Key findMinKey() {
        Node minNode = getMinNode();
        if (minNode == null) return null;
        return minNode.key;
    }

    public Value findMinVal() {
        Node minNode = getMinNode();
        if (minNode == null) return null;
        return minNode.value;
    }

    //Returns the number of elements with keys in the range [low, high] (inclusive).
    public int getSizeInRange(Key low, Key high) {
        return getSizeUpTo(high) - getSizeStrictlyLessThan(low);
    }


    //Implementation Logic ---------------------------------------------------------------------------------------------

    // private function to delete a node
    private void twoThreeDelete(Node x) {
        // Line 1: y = x.p
        Node y = x.parent;

        // --- Lines 2-7: Remove x from children of y ---
        if (x == y.left) {
            setChildren(y, y.middle, y.right, null); // Line 3
        } else if (x == y.middle) {
            setChildren(y, y.left, y.right, null);   // Line 5
        } else {
            setChildren(y, y.left, y.middle, null);  // Line 6
        }
        // Line 7: delete x (Implicitly done by removing pointers to it)

        // --- Lines 8-18: Loop upwards to fix the tree ---
        while (y != null) { // Line 8
            // Line 9: If y is valid (has at least 2 children)
            // Note: In 2-3 tree, if middle != null, it means we have 2 or 3 children.
            if (y.middle != null) {
                updateInternalData(y); // Line 10 (Update Key & Size)
                y = y.parent;          // Line 11
            }
            // Line 12: Else (y has only 1 child - Underflow)
            else {
                // Line 13: if y is NOT the root -> fix with borrow/merge
                if (y != this.root) {
                    y = borrowOrMerge(y); // Line 14
                }
                // Line 15: Else (y IS the root and has only 1 child) -> Height reduction
                else {
                    this.root = y.left;      // Line 15: The single child becomes new root
                    this.root.parent = null; // Line 16
                    // Line 17: delete y (Java GC handles it)
                    return;                  // Line 18
                }
            }
        }
    }

    private Node borrowOrMerge(Node y) {
        // Line 1: z = y.p
        Node z = y.parent;

        // --- CASE 1: y is the LEFT child --- (Lines 2-10)
        if (y == z.left) {
            Node x = z.middle; // Line 3: x = z.middle

            // Case 1a: Borrow (x has 3 children)
            if (x.right != null) { // Line 4
                setChildren(y, y.left, x.left, null);     // Line 5
                setChildren(x, x.middle, x.right, null);  // Line 6
            }
            // Case 1b: Merge (x has 2 children)
            else {
                setChildren(x, y.left, x.left, x.middle); // Line 7
                // Line 8: delete y (Java Garbage Collector handles this, we just unlink it)
                setChildren(z, x, z.right, null);         // Line 9
            }
            return z; // Line 10
        }

        // --- CASE 2: y is the MIDDLE child --- (Lines 11-19)
        else if (y == z.middle) {
            Node x = z.left; // Line 12: x = z.left

            // Case 2a: Borrow
            if (x.right != null) { // Line 13
                setChildren(y, x.right, y.left, null);    // Line 14
                setChildren(x, x.left, x.middle, null);   // Line 15
            }
            // Case 2b: Merge
            else {
                setChildren(x, x.left, x.middle, y.left); // Line 16
                // Line 17: delete y
                setChildren(z, x, z.right, null);         // Line 18
            }
            return z; // Line 19
        }

        // --- CASE 3: y is the RIGHT child --- (Lines 20-28)
        else { // y == z.right
            Node x = z.middle; // Line 21: x = z.middle

            // Case 3a: Borrow
            if (x.right != null) { // Line 22
                setChildren(y, x.right, y.left, null);    // Line 23
                setChildren(x, x.left, x.middle, null);   // Line 24
            }
            // Case 3b: Merge
            else {
                setChildren(x, x.left, x.middle, y.left); // Line 25
                // Line 26: delete y
                setChildren(z, z.left, x, null);          // Line 27
            }
            return z; // Line 28
        }
    }


    private Node insertAndSplit(Node x, Node z) {
        // Define l, m, r
        Node l = x.left;
        Node m = x.middle;
        Node r = x.right;

        // --- Case A: Node x is not full (r == NIL)
        if (r == null) {
            if (z.key.compareTo(l.key) < 0) {
                setChildren(x, z, l, m);
            } else if (z.key.compareTo(m.key) < 0) {
                setChildren(x, l, z, m);
            } else {
                setChildren(x, l, m, z);
            }
            return null;
        }

        // --- Case B: Node x is full (Split needed)
        //  new internal node y
        // We create it with nulls, setChildren will fill it immediately.
        Node y = new Node(null, null, null);

        //  Determine split based on z's key
        if (z.key.compareTo(l.key) < 0) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (z.key.compareTo(m.key) < 0) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (z.key.compareTo(r.key) < 0) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }

        return y;
    }
// Navigation Helpers -------------------------------------------------------------------------------------------------

    // getting size helpers
    // --- Helper 1: Count elements <= limit (Inclusive) ---

    private int getSizeUpTo(Key limit) {
        return getSizeUpToRec(root, limit);
    }

    private int getSizeUpToRec(Node x, Key limit) {
        // 1. Stop condition: Leaf
        if (x.isLeaf) {
            // Check if leaf key is <= limit
            if (x.key.compareTo(limit) <= 0) {
                return x.size; // Returns 1 (or 0 if sentinel)
            } else {
                return 0;
            }
        }

        // 2. Recursive Step (Exactly like Sum Of Smaller)

        // Case A: Limit is within/smaller than left child max
        if (limit.compareTo(x.left.key) <= 0) {
            return getSizeUpToRec(x.left, limit);
        }
        // Case B: Limit is within/smaller than middle child max
        else if (x.right == null || limit.compareTo(x.middle.key) <= 0) {
            return x.left.size + getSizeUpToRec(x.middle, limit);
        }
        // Case C: Limit is in right child
        else {
            return x.left.size + x.middle.size + getSizeUpToRec(x.right, limit);
        }
    }

    // --- Helper 2: Count elements < limit (Strictly Less) ---
    // Needed because we cannot do (low - 1) with generic keys

    private int getSizeStrictlyLessThan(Key limit) {
        return getSizeStrictlyLessThanRec(root, limit);
    }

    private int getSizeStrictlyLessThanRec(Node x, Key limit) {
        // 1. Stop condition: Leaf
        if (x.isLeaf) {
            // DIFFERENT LOGIC HERE: Strictly Less (<)
            if (x.key.compareTo(limit) < 0) {
                return x.size;
            } else {
                return 0;
            }
        }

        // 2. Recursive Step (Identical navigation logic)
        // We compare against the max keys of subtrees to decide path

        if (limit.compareTo(x.left.key) <= 0) {
            return getSizeStrictlyLessThanRec(x.left, limit);
        }
        else if (x.right == null || limit.compareTo(x.middle.key) <= 0) {
            return x.left.size + getSizeStrictlyLessThanRec(x.middle, limit);
        }
        else {
            return x.left.size + x.middle.size + getSizeStrictlyLessThanRec(x.right, limit);
        }
    }





    // Mthod to get Node based on given key
    protected Node getNode(Key key) {
        Node current = root;

        // going down the tree until there is a leaf
        while (!current.isLeaf) {
            if (key.compareTo(current.left.key) <= 0) {
                current = current.left;
            }
            else if (current.right == null || key.compareTo(current.middle.key) <= 0) {
                current = current.middle;
            }
            else {
                current = current.right;
            }
        }

        // reached leaf checking key equality
        if (current.key.compareTo(key) == 0) {
            return current;
        }
        return null;
    }


    public Node successor(Node x) {
        // Line 1: z = x.p
        Node z = x.parent;

        // Line 2-4: Climb up while we are the "rightmost" child
        // Rightmost means: we are 'right' OR (we are 'middle' and there is no 'right')
        while (z != null && (x == z.right || (z.right == null && x == z.middle))) {
            x = z;
            z = z.parent;
        }

        // If z is null, we reached beyond the root (No successor)
        if (z == null) return null;

        // Line 5-7: Move to the next sibling to the right
        Node y;
        if (x == z.left) {
            y = z.middle; // Line 6
        } else {
            y = z.right;  // Line 7
        }

        // Line 8-9: Dive down to the leftmost leaf of that sibling
        while (!y.isLeaf) {
            y = y.left;
        }

        // Line 10-12: If it's not the Max Sentinel, return it
        if (!isMaxSentinel(y)) {
            return y;
        } else {
            return null; // Reached +Infinity
        }
    }


    protected Node getMinNode() {
        if (root == null) return null;

        Node x = root;
        while (!x.isLeaf) {
            x = x.left;
        }

        x = x.parent.middle;

        if (x != null && !isMaxSentinel(x)) {
            return x;
        }
        return null;
    }




    //Data Maintenance ------------------------------------------------------------------------------------------------


    // Super function that handels all the updates of node data
    private void updateInternalData(Node x) {
        if (x == null || x.isLeaf) return;

        updateKey(x);                // Update the key of the node to be the max of its children
        updateSubTreeSize(x);        // Update the size parameter
        updateAdditionalNodeData(x); // function implemented by the load tree
    }



    // Updates node x's key to be the maximum of its children's keys
    private void updateKey(Node x) {
        if (x == null || x.isLeaf) return;

        x.key = x.left.key;
        if (x.middle != null) x.key = x.middle.key;
        if (x.right != null) x.key = x.right.key;
    }

    // --- Helper : Update the size field ---
    private void updateSubTreeSize(Node x) {
        x.size = 0;
        if (x.left != null) x.size += x.left.size;
        if (x.middle != null) x.size += x.middle.size;
        if (x.right != null) x.size += x.right.size;
    }

    // Sets the children of x and updates their parent pointers and x's key
    private void setChildren(Node x, Node l, Node m, Node r) {
        x.left = l;
        x.middle = m;
        x.right = r;

        if (l != null) l.parent = x;
        if (m != null) m.parent = x;
        if (r != null) r.parent = x;

        updateInternalData(x); // After setting the children we update the data of the parent
    }
    // Helper to check if a node is the +Infinity sentinel
    private boolean isMaxSentinel(Node x) {
        if (x == null || !x.isLeaf) return false;
        Node temp = root;
        while (!temp.isLeaf) {
            if (temp.right != null) temp = temp.right;
            else temp = temp.middle;
        }
        return x == temp;
    }

    // Helper to check if a node is the -Infinity sentinel
    private boolean isMinSentinel(Node x) {
        if (x == null || !x.isLeaf) return false;
        Node temp = root;
        while (!temp.isLeaf) {
            temp = temp.left;
        }
        return x == temp;
    }

    // Empty function for the Load tree class to overide
    protected void updateAdditionalNodeData(Node node) {
        // Empty in base class.
        // LoadTree will override this to do: node.sum = left.sum + middle.sum + ...
    }




}