package matrix.cartesian;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class LinkedList<A> implements Iterable<A> {
    private Slot<A> actual, lower;
    private int negativeBound, positiveBound, pos;

    /*
    Assume that 0 is positive so 'negativeBound' will always be < 0 if there are elements in list
     */
    LinkedList(int negativeBound, int positiveBound, int pos) {
        if (negativeBound <= 0 && positiveBound >= 0 && pos >= negativeBound && pos <= positiveBound) {
            this.negativeBound = negativeBound;
            this.positiveBound = positiveBound;
            this.pos = pos;
            Slot<A> slot, zero;
            slot = zero = actual = new Slot<>();
            for (int i = -1; i >= negativeBound; i--) {
                if (pos == i) actual = slot;
                slot = slot.prev = new Slot(slot, null);
            }
            lower = slot;
            slot = zero;
            for (int i = 0; i < positiveBound; i++) {
                if (pos == i) actual = slot;
                slot = slot.next = new Slot<>(null, slot);
            }
        } else throw new RuntimeException("Wrong parameters");
    }

    public int getNegativeBound() {
        return negativeBound;
    }

    public int getPositiveBound() {
        return positiveBound;
    }

    public int getPos() {
        return pos;
    }

    public void moveTo(int pos) {
        if (pos > positiveBound || pos < negativeBound) throw new IndexOutOfBoundsException("Moving out of list");
        else while (this.pos != pos) {
            if (pos > this.pos) {
                this.pos++;
                actual = actual.next;
            } else {
                this.pos--;
                actual = actual.prev;
            }
        }
    }

    public void addBottom() {
        if (pos != negativeBound) throw new RuntimeException("Inserting element to bottom but position is higher");
        negativeBound--;
        actual.prev = lower = new Slot<>(actual, null);
    }

    public void addTop() {
        if (pos != positiveBound) throw new RuntimeException("Inserting element to top but position is lower");
        positiveBound++;
        actual.next = new Slot<>(null, actual);
    }

    public void set(A val) {
        actual.value = val;
    }

    public A get() {
        return actual.value;
    }


    public A getAfter() {
        return actual.next.value;
    }

    public void setAfter(A val) {
        actual.next.value = val;
    }

    public A getBefore() {
        return actual.prev.value;
    }

    public void setBefore(A val) {
        actual.prev.value = val;
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        return new LinkedListIterator<A>(this);
    }

    class LinkedListIterator<A> implements Iterator<A> {

        private Slot<A> slot;

        LinkedListIterator(LinkedList list) {
            slot = list.lower;
        }

        @Override
        public boolean hasNext() {
            return slot != null;
        }

        @Override
        public A next() {
            A val = slot.value;
            slot = slot.next;
            return val;
        }


    }

    private class Slot<A> {
        protected A value = null;
        protected Slot<A> next, prev;

        Slot() {

        }

        Slot(Slot next, Slot prev) {
            this.next = next;
            this.prev = prev;
        }
    }
}
