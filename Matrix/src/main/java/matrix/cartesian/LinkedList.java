package matrix.cartesian;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class LinkedList<A> implements Iterable {
    private Slot<A> first, actual;
    private int size;

    LinkedList() {
        first = actual = null;
        size = 0;
    }

    LinkedList(int size, int pos) {
        if (size > 0 && pos >= 0 && pos < size) {
            this.size = size;
            Slot<A> slot = first = new Slot<>();
            for (int i = 0; i < size - 1; i++) {
                if (pos == i) actual = slot;
                slot = slot.next = new Slot<>(null, slot);
            }
        } else {
            first = actual = null;
            this.size = 0;
        }
    }

    public int getSize() {
        return size;
    }

    public A get() {
        return actual.value;
    }

    public void move(boolean forward) {
        if (forward) {
            if (actual.next == null) throw new IndexOutOfBoundsException("Out of List");
            else actual = actual.next;
        } else {
            if (actual.prev == null) throw new IndexOutOfBoundsException("Out of List");
            else actual = actual.prev;
        }
    }

    public void addH() {
        size++;
        actual.next = new Slot(null, actual);
    }

    public void addQ() {
        size--;
    }

    @NotNull
    @Override
    public LinkedListIterator<A> iterator() {
        return new LinkedListIterator<A>(this);
    }

    class LinkedListIterator<A> implements Iterator {

        private Slot<A> slot;

        LinkedListIterator(LinkedList list) {
            slot = list.first;
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
        protected Slot next, prev;

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
