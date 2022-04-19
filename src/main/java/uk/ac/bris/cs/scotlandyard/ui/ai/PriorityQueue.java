package uk.ac.bris.cs.scotlandyard.ui.ai;


import java.util.Arrays;

// A POJO for queue data structure, used in dijkstra algorithm
// A queue which index 0 is always the front
// (The index which element is next to get popped out)
// Works as a queue (FIFO system), but has additional feature that
// automatically sorts in ascending order when new element is pushed


// Didn't implement the interface java.util.queue
// since we're going to use only part of the queue functions
public class PriorityQueue {

    // New inner class Distance to map the node with distance
    public static class Distance {
        private final int node;
        private int distance;

        
        Distance(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        public int getNode() {
            return node;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int value) {
            distance = value;
        }

        @Override
        public String toString() {
            return "D("+getNode()+"): "+getDistance();
        }
    }

    private final int maximum;
    private final Distance [] queue;
    // The size of the queue is equivalent to the value of tail
    private int tail;

    PriorityQueue(int max) {
        tail = 0;
        this.maximum = max;
        queue = new Distance[maximum];
    }

    // Adds new element, increases tail by one
    public void push(int node, int value) {
        assert(size() < maximum);
        Distance d = new Distance(node, value);
        queue[tail] = d;
        tail++;
        if(size() >= 2) rearrange(d);
    }

    // Removes the front element, decreases tail by one
    public Distance pop() {
        assert(size() != 0);
        Distance target = queue[0];
        for(int i = 0; i < this.size() - 1; i++) queue[i] = queue[i + 1];
        queue[size() - 1] = null;
        tail--;
        return target;
    }


    // Rearranges the queue by ascending order
    // Assuming that the queue is already arranged, finds the place to insert
    // like an InsertionSort algorithm
    public void rearrange(Distance d) {
        // Puts the parameter to the tail of the queue to assume that the queue is already arranged
        // Except the parameter
        for(int i = getIndex(d); i < size() - 1; i++) {queue[i] = queue[i + 1];}
//        if (size() - 1 - getIndex(d) >= 0)
//            System.arraycopy(queue, getIndex(d) + 1, queue, getIndex(d), size() - 1 - getIndex(d));
        queue[tail - 1] = d;
        int index = size() - 1;
        while((index > 0) && (queue[index - 1].getDistance() > d.getDistance())) {
            queue[index] = queue[index - 1];
            index--;
        }
        queue[index] = d;
    }

    // Returns the peak value (returns -1 if the queue is empty)
    public int peakDistance() {
        if(size() == 0) return -1;
        else return queue[0].getDistance();
    }

    // Returns the node of the peak (returns -1 if the queue is empty)
    public int peakedNode() {
        if(size() == 0) return -1;
        else return queue[0].getNode();
    }

    // Returns the index of the queue given the distance
    public int getIndex(Distance d) {
        int index = 0;
        while ((!d.equals(queue[index])) && (index < maximum)) {
            index++;
        }
        return index;
    }

    // Updates the distance
    public void setDistance(int node, int value) {
        Distance target = null;
        for(Distance d : queue) {
            if (d != null) {
                if(d.getNode() == node) target = d;
            }
        }
        assert target != null;
        target.setDistance(value);
        rearrange(target);
    }

    public int size() {
        return tail;
    }


    public boolean contains(int value) {
        if (size() != 0 ) {
            for (Distance di : queue) {
                if (di != null) {
                    if (di.getNode() == value) return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(queue);
    }
}
