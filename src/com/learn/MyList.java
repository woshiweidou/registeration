package com.learn;

public class MyList {
    Node head = null;
    // 头节点
    /**
     * 链表中的节点，data代表节点的值，next是指向下一个节点的引用
     *
     * @author zjn
     *
     */
    class Node {
        Node next = null;
        // 节点的引用，指向下一个节点
        int data;
        // 节点的对象，即内容
        public Node(int data) {
            this.data = data;
        }
        public Node(){}
    }
    /**
     * 向链表中插入数据
     *
     * @param d
     */
    public void add(int d) {
//        Node newNode = new Node(d);
//
//        Node temp = head;
//        // 实例化一个节点
//        if (head == null) {
//            head = newNode;
//            return;
//        }
//        while(temp.next != null){
//            temp = temp.next;
//        }
//        temp.next = newNode;
        Node temp = new Node();
        if(head == null){
            head = new Node();
            head.data = d;
            return;
        }
        temp = head;
        while(temp.next != null){
            temp = temp.next;
        }
        Node node = new Node();
        node.data = d;
        node.next = null;
        temp.next = node;

    }


    public int length() {
        int length = 0;
        Node tmp = head;
        while (tmp != null) {
            length++;
            tmp = tmp.next;
        }
        return length;
    }


    public void printList() {
        Node tmp = head;
        while (tmp != null) {
            System.out.println(tmp.data);
            tmp = tmp.next;
        }
    }

}