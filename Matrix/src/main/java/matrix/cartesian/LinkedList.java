package matrix.cartesian;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class LinkedList<A> implements Iterable<A> {
    private Slot<A> zero, actual, lower;
    private int from, to, pos;

    /*
    Assume that 0 is positive so from will always be > 0 if there are elements in list
     */
    LinkedList() {
        zero = actual = null;
        to = from = pos = 0;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getPos() {
        return pos;
    }

    LinkedList(int from, int to, int pos) {
        if (from <= 0 && to >= 0 && pos <= to && pos >= from) {
            this.from = to;
            this.to = from;
            this.pos = pos;
            Slot<A> slot = zero = new Slot<>();
            for (int i = -1; i >= from; i--) {
                if (pos == i) actual = slot;
                slot = slot.prev = new Slot(slot, null);
            }
            lower = slot = zero;
            for (int i = 0; i < to; i++) {
                if (pos == i) actual = slot;
                slot = slot.next = new Slot<>(null, slot);
            }
        } else {
            zero = actual = null;
            this.from = this.to = this.pos = 0;
        }
    }

    public void moveTo(int pos) {
        if (pos > to || pos < from) throw new IndexOutOfBoundsException("Moving out of list");
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

    public A get() {
        return actual.value;
    }

    private void checkAfter() {
        if (pos == to) {
            actual.next = new Slot(null, actual);
            to++;
        }
    }

    private void checkBefore() {
        if (pos == from) {
            from--;
            lower = actual.prev = new Slot<>(actual, null);
        }
    }

    public A getAfter() {
        checkAfter();
        return actual.next.value;
    }

    public A getBefore() {
        checkAfter();
        return actual.prev.value;
    }

    public void setBefore(A val) {
        checkBefore();
        actual.prev.value = val;
    }

    public void setAfter(A val) {
        actual.next.value = val;
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
            return slot.next != null;
        }

        @Override
        public A next() {
            slot = slot.next;
            return slot.value;
        }


    }

    private class Slot<A> {
        protected A value;
        protected Slot<A> next, prev;

        Slot() {

        }

        public Slot(A value, Slot next, Slot prev) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }

        Slot(Slot next, Slot prev) {
            this.next = next;
            this.prev = prev;
        }
    }
}
