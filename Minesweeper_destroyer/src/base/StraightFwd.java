import java.util.Iterator;
import java.util.NoSuchElementException;
import base.Base;

public class StraightFwd{

}


class Node<T>{
    T value;
    Node<T> next;
    Node<T> prev;

    Node(T value){
        this.value = value;
    }
}

class DoublyLinkedList<T>{
    private Node<T> head;
    private Node<T> tail;

    void append(Node<T> node){
        if(this.head == null){
            this.head = node;
            this.tail = node;
            this.linkTailandHead();
        }else{
            this.tail.next = node;
            node.prev = this.tail;
            this.tail = node;
            this.linkTailandHead();
        }
    }
    void remove(Node<T> node){
        //handling the last item in the list
        if (node == this.tail && node == this.head) {
            this.tail.next = null;
            this.tail.prev = null;
            this.tail = null;
            this.head = null;
        }
        final Node<T> prevNode = node.prev;
        final Node<T> nextNode = node.next;
    
        if (prevNode != null) {
            prevNode.next = nextNode;
        }
        if (nextNode != null) {
            nextNode.prev = prevNode;
        }
        if (this.tail == node) {
            this.tail = prevNode;
        }
        if (this.head == node) {
            this.head = nextNode;
        }
    }

    
    private class DoubleListIterator implements Iterator<Node<T>> {
        // instance variable
        private Node<T> curr = head;
        
        public boolean hasNext() {
            return curr != null;
          }
        public Node<T> next(){
            if(!hasNext()) throw new NoSuchElementException();
            Node<T> tmp = curr;
            curr = curr.next;
            return tmp;
        }
    }
    private void linkTailandHead(){
        this.tail.next = this.head;
        this.head.prev = this.tail;
    }
}

