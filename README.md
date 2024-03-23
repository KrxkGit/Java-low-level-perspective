# 🔍我想看看 Java 库源码

本文档以 **大学计算机本科课程** 为出发点，一窥 Java 库代码的实现，将 **理论与开发实践** 进行紧密结合。文档中源代码源于 **JDK-19**。

​																			——**狂刄星空**

### 🖊线性表

#### 📝动态数组

##### 插入

可以看出，在中间插入涉及到数组的拷贝移动。其中涉及的

> System.arraycopy

是一个 native 函数，用于将数组的某一索引位置开始的元素移动到另一个开始索引。

```java
/**
 * Inserts the specified element at the specified position in this
 * list. Shifts the element currently at that position (if any) and
 * any subsequent elements to the right (adds one to their indices).
 *
 * @param index index at which the specified element is to be inserted
 * @param element element to be inserted
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public void add(int index, E element) {
    rangeCheckForAdd(index);
    modCount++;
    final int s;
    Object[] elementData;
    if ((s = size) == (elementData = this.elementData).length)
        elementData = grow();
    System.arraycopy(elementData, index,
                     elementData, index + 1,
                     s - index);
    elementData[index] = element;
    size = s + 1;
}
```

批量插入，批量插入可以一次性完成数组的扩容与元素移动，更加高效

```java
/**
 * Appends all of the elements in the specified collection to the end of
 * this list, in the order that they are returned by the
 * specified collection's Iterator.  The behavior of this operation is
 * undefined if the specified collection is modified while the operation
 * is in progress.  (This implies that the behavior of this call is
 * undefined if the specified collection is this list, and this
 * list is nonempty.)
 *
 * @param c collection containing elements to be added to this list
 * @return {@code true} if this list changed as a result of the call
 * @throws NullPointerException if the specified collection is null
 */
public boolean addAll(Collection<? extends E> c) {
    Object[] a = c.toArray();
    modCount++;
    int numNew = a.length;
    if (numNew == 0)
        return false;
    Object[] elementData;
    final int s;
    if (numNew > (elementData = this.elementData).length - (s = size))
        elementData = grow(s + numNew);
    System.arraycopy(a, 0, elementData, s, numNew);
    size = s + numNew;
    return true;
}
```



##### 删除

从下列代码可以看出，删除也涉及到数组元素的移动操作。

```java
/**
 * Removes the element at the specified position in this list.
 * Shifts any subsequent elements to the left (subtracts one from their
 * indices).
 *
 * @param index the index of the element to be removed
 * @return the element that was removed from the list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E remove(int index) {
    Objects.checkIndex(index, size);
    final Object[] es = elementData;

    @SuppressWarnings("unchecked") E oldValue = (E) es[index];
    fastRemove(es, index);

    return oldValue;
}

/**
 * Private remove method that skips bounds checking and does not
 * return the value removed.
 */
private void fastRemove(Object[] es, int i) {
    modCount++;
    final int newSize;
    if ((newSize = size - 1) > i)
        System.arraycopy(es, i + 1, es, i, newSize - i);
    es[size = newSize] = null;
}
```

清空

```java
public void clear() {
    modCount++;
    final Object[] es = elementData;
    for (int to = size, i = size = 0; i < to; i++)
        es[i] = null;
}
```



##### 查找

```java
/**
 * Returns the element at the specified position in this list.
 *
 * @param  index index of the element to return
 * @return the element at the specified position in this list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E get(int index) {
    Objects.checkIndex(index, size);
    return elementData(index);
}
```

##### 修改

```java
/**
 * Replaces the element at the specified position in this list with
 * the specified element.
 *
 * @param index index of the element to replace
 * @param element element to be stored at the specified position
 * @return the element previously at the specified position
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E set(int index, E element) {
    Objects.checkIndex(index, size);
    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}
```



##### 扩容

若不指定，数组默认容量为 **10**

```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    @java.io.Serial
    private static final long serialVersionUID = 8683452581122892189L; // 用于序列化

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    // 使用 transient 关键字，以忽略序列化
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;
```

默认扩容为原来的2倍 (拷贝到一个 **新数组** )

```java
/**
 * Increases the capacity to ensure that it can hold at least the
 * number of elements specified by the minimum capacity argument.
 *
 * @param minCapacity the desired minimum capacity
 * @throws OutOfMemoryError if minCapacity is less than zero
 */
private Object[] grow(int minCapacity) {
    int oldCapacity = elementData.length;
    if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        int newCapacity = ArraysSupport.newLength(oldCapacity,
                minCapacity - oldCapacity, /* minimum growth */
                oldCapacity >> 1           /* preferred growth */);
        return elementData = Arrays.copyOf(elementData, newCapacity);
    } else {
        return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
    }
}
```



##### 裁剪

```java
/**
 * Trims the capacity of this {@code ArrayList} instance to be the
 * list's current size.  An application can use this operation to minimize
 * the storage of an {@code ArrayList} instance.
 */
public void trimToSize() {
    modCount++; // 统计此列表在结构上被修改的次数
    if (size < elementData.length) {
        elementData = (size == 0)
          ? EMPTY_ELEMENTDATA
          : Arrays.copyOf(elementData, size);
    }
}
```

#### 📝线程安全的动态数组 —— Vector

主要使用 **synchronized** 关键字保证线程安全

```java
public synchronized void insertElementAt(E obj, int index) {
    if (index > elementCount) {
        throw new ArrayIndexOutOfBoundsException(index
                                                 \+ " > " + elementCount);
    }
    modCount++;
    final int s = elementCount;
    Object[] elementData = this.elementData;
    if (s == elementData.length)
        elementData = grow();
    System.*arraycopy*(elementData, index,
                     elementData, index + 1,
                     s - index);
    elementData[index] = obj;
    elementCount = s + 1;
}

public synchronized boolean removeElement(Object obj) {
    modCount++;
    int i = indexOf(obj);
    if (i >= 0) {
        removeElementAt(i);
        return true;
    }
    return false;
}

public synchronized E get(int index) {
    if (index >= elementCount)
        throw new ArrayIndexOutOfBoundsException(index);

    return elementData(index);
}

public synchronized E set(int index, E element) {
    if (index >= elementCount)
        throw new ArrayIndexOutOfBoundsException(index);

    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}
```

#### 📝基于 Vector 的 Stack

```java
public class Stack<E> extends Vector<E>
```

#### 📝双向链表

##### 结点封装

```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    transient int size = 0;

    /**
     * Pointer to first node.
     */
    transient Node<E> first;

    /**
     * Pointer to last node.
     */
    transient Node<E> last;
    
    //......
}

private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

	Node(Node<E> prev, E element, Node<E> next) {
	    this.item = element;
	    this.next = next;
	    this.prev = prev;
	}
}

/**
 * Returns the (non-null) Node at the specified element index.
 */
Node<E> node(int index) {
    // assert isElementIndex(index);

    if (index < (size >> 1)) {
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}
```

##### 插入

```java
/**
 * Inserts the specified element at the specified position in this list.
 * Shifts the element currently at that position (if any) and any
 * subsequent elements to the right (adds one to their indices).
 *
 * @param index index at which the specified element is to be inserted
 * @param element element to be inserted
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public void add(int index, E element) {
    checkPositionIndex(index);

    if (index == size)
        linkLast(element);
    else
        linkBefore(element, node(index));
}

/**
 * Inserts element e before non-null Node succ.
 */
void linkBefore(E e, Node<E> succ) {
    // assert succ != null;
    final Node<E> pred = succ.prev;
    final Node<E> newNode = new Node<>(pred, e, succ);
    succ.prev = newNode;
    if (pred == null)
        first = newNode;
    else
        pred.next = newNode;
    size++;
    modCount++;
}

/**
 * Links e as last element.
 */
void linkLast(E e) {
    final Node<E> l = last;
    final Node<E> newNode = new Node<>(l, e, null);
    last = newNode;
    if (l == null)
        first = newNode;
    else
        l.next = newNode;
    size++;
    modCount++;
}
```



##### 删除

```java
/**
 * Removes the first occurrence of the specified element from this list,
 * if it is present.  If this list does not contain the element, it is
 * unchanged.  More formally, removes the element with the lowest index
 * {@code i} such that
 * {@code Objects.equals(o, get(i))}
 * (if such an element exists).  Returns {@code true} if this list
 * contained the specified element (or equivalently, if this list
 * changed as a result of the call).
 *
 * @param o element to be removed from this list, if present
 * @return {@code true} if this list contained the specified element
 */
public boolean remove(Object o) {
    if (o == null) {
        for (Node<E> x = first; x != null; x = x.next) {
            if (x.item == null) {
                unlink(x);
                return true;
            }
        }
    } else {
        for (Node<E> x = first; x != null; x = x.next) {
            if (o.equals(x.item)) {
                unlink(x);
                return true;
            }
        }
    }
    return false;
}

/**
 * Unlinks non-null node x.
 */
E unlink(Node<E> x) {
    // assert x != null;
    final E element = x.item;
    final Node<E> next = x.next;
    final Node<E> prev = x.prev;

    if (prev == null) {
        first = next;
    } else {
        prev.next = next;
        x.prev = null;
    }

    if (next == null) {
        last = prev;
    } else {
        next.prev = prev;
        x.next = null;
    }

    x.item = null; // 释放引用
    size--;
    modCount++;
    return element;
}

// 头部 与 尾部 作特殊处理
**
 * Unlinks non-null first node f.
 */
private E unlinkFirst(Node<E> f) {
    // assert f == first && f != null;
    final E element = f.item;
    final Node<E> next = f.next;
    f.item = null;
    f.next = null; // help GC
    first = next;
    if (next == null)
        last = null;
    else
        next.prev = null;
    size--;
    modCount++;
    return element;
}

/**
 * Unlinks non-null last node l.
 */
private E unlinkLast(Node<E> l) {
    // assert l == last && l != null;
    final E element = l.item;
    final Node<E> prev = l.prev;
    l.item = null;
    l.prev = null; // help GC
    last = prev;
    if (prev == null)
        first = null;
    else
        prev.next = null;
    size--;
    modCount++;
    return element;
}
```

##### 查找 与 修改

```java
/**
 * Returns the element at the specified position in this list.
 *
 * @param index index of the element to return
 * @return the element at the specified position in this list
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}

/**
 * Replaces the element at the specified position in this list with the
 * specified element.
 *
 * @param index index of the element to replace
 * @param element element to be stored at the specified position
 * @return the element previously at the specified position
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E set(int index, E element) {
    checkElementIndex(index);
    Node<E> x = node(index);
    E oldVal = x.item;
    x.item = element;
    return oldVal;
}
```

##### 清空

```java
/**
 * Removes all of the elements from this list.
 * The list will be empty after this call returns.
 */
public void clear() {
    // Clearing all of the links between nodes is "unnecessary", but:
    // - helps a generational GC if the discarded nodes inhabit
    //   more than one generation
    // - is sure to free memory even if there is a reachable Iterator
    for (Node<E> x = first; x != null; ) {
        Node<E> next = x.next;
        x.item = null;
        x.next = null;
        x.prev = null;
        x = next;
    }
    first = last = null;
    size = 0;
    modCount++;
}
```

##### toArray

该函数将链表转换为数组，自动类型转换功能涉及**反射机制**（传入的目标数组容量不够时通过反射机制自动创建）。

```java
public <T> T[] toArray(T[] a) {
	if (a.length < size)
	    a = (T[])java.lang.reflect.Array.newInstance(
	                        a.getClass().getComponentType(), size);
	int i = 0;
	Object[] result = a;
	for (Node<E> x = first; x != null; x = x.next)
	    result[i++] = x.item;

	if (a.length > size)
	    a[size] = null;

	return a;
}
```



#### 📝队列

入列 —— **offer**

出列 —— **poll**

查看而不出列 —— **peek**



关于队列，有如下依赖继承关系：

queue -> deque -> { LinkedList,  ArrayDeque }



##### 实现类 —— LinkedList

可以看出，对于双向链表，offer 方法 与 add 方法等同。

```java
public boolean offer(E e) {
    return add(e);
}

public boolean add(E e) {
    linkLast(e);
    return true;

}

public E poll() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}

public E peek() {
    final Node<E> f = first;
    return (f == null) ? null : f.item;
}
```

##### 实现类 —— ArrayDeque

```java
public boolean add(E e) {
    addLast(e);
    return true;
}

public void addLast(E e) {
        if (e == null)
            throw new NullPointerException();
        final Object[] es = elements;
        es[tail] = e;
        if (head == (tail = inc(tail, es.length)))
            grow(1);
}


public boolean offer(E e) {
    return offerLast(e);
}

public boolean offerLast(E e) {
    addLast(e);
    return true;
}

public E poll() {
    return pollFirst();
}
public E pollFirst() {
    final Object[] es;
    final int h;
    E e = elementAt(es = elements, h = head);
    if (e != null) {
        es[h] = null;
        head = inc(h, es.length);
    }
    return e;
}
```

由于 该类底层为数组，故涉及到数组的**动态扩容**

```java
/**
 * The maximum size of array to allocate.
 * Some VMs reserve some header words in an array.
 * Attempts to allocate larger arrays may result in
 * OutOfMemoryError: Requested array size exceeds VM limit
 */
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

/**
 * Increases the capacity of this deque by at least the given amount.
 *
 * @param needed the required minimum extra capacity; must be positive
 */
private void grow(int needed) {
    // overflow-conscious code
    final int oldCapacity = elements.length;
    int newCapacity;
    // Double capacity if small; else grow by 50%
    int jump = (oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1);
    if (jump < needed
        || (newCapacity = (oldCapacity + jump)) - MAX_ARRAY_SIZE > 0)
        newCapacity = newCapacity(needed, jump);
    final Object[] es = elements = Arrays.copyOf(elements, newCapacity);
    // Exceptionally, here tail == head needs to be disambiguated
    if (tail < head || (tail == head && es[head] != null)) {
        // wrap around; slide first leg forward to end of array
        int newSpace = newCapacity - oldCapacity;
        System.arraycopy(es, head,
                         es, head + newSpace,
                         oldCapacity - head);
        for (int i = head, to = (head += newSpace); i < to; i++)
            es[i] = null;
    }
}
```

对于双向队列，支持双向操作：

```java
boolean offerFirst(E e);
boolean offerLast(E e);
E pollFirst();
E pollLast();
E peekFirst();
E peekLast();
```

### 🖊优先队列

优先队列涉及堆算法

```java
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {

    @java.io.Serial
    private static final long serialVersionUID = -7720805057305804111L;

    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n <= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    transient Object[] queue; // non-private to simplify nested class access

    /**
     * The number of elements in the priority queue.
     */
    int size;

    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    @SuppressWarnings("serial") // Conditionally serializable
    private final Comparator<? super E> comparator;

    /**
     * The number of times this priority queue has been
     * <i>structurally modified</i>.  See AbstractList for gory details.
     */
    transient int modCount;     // non-private to simplify nested class access
    
    // ......
}
```

#### 动态扩容

```java
/**
 * Increases the capacity of the array.
 *
 * @param minCapacity the desired minimum capacity
 */
private void grow(int minCapacity) {
    int oldCapacity = queue.length;
    // Double size if small; else grow by 50%
    int newCapacity = ArraysSupport.newLength(oldCapacity,
            minCapacity - oldCapacity, /* minimum growth */
            oldCapacity < 64 ? oldCapacity + 2 : oldCapacity >> 1
                                       /* preferred growth */);
    queue = Arrays.copyOf(queue, newCapacity);
}
```

#### 查找操作

```java
private int indexOf(Object o) {
        if (o != null) {
            final Object[] es = queue;
            for (int i = 0, n = size; i < n; i++)
                if (o.equals(es[i]))
                    return i;
        }
        return -1;
}
```

#### 插入(入列)操作

```java
public boolean offer(E e) {
    if (e == null)
        throw new NullPointerException();
    modCount++;
    int i = size;
    if (i >= queue.length)
        grow(i + 1);
    siftUp(i, e);
    size = i + 1;
    return true;
}

private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x, queue, comparator);
        else
            siftUpComparable(k, x, queue);
}

private static <T> void siftUpUsingComparator(
        int k, T x, Object[] es, Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = es[parent];
            if (cmp.compare(x, (T) e) >= 0)
                break;
            es[k] = e;
            k = parent;
        }
        es[k] = x;
}
```

#### 删除操作

```java
 E removeAt(int i) {
 	// assert i >= 0 && i < size;
 	final Object[] es = queue;
 	modCount++;
 	int s = --size;
 	if (s == i) // removed last element
 	    es[i] = null;
 	else {
 	    E moved = (E) es[s];
 	    es[s] = null;
 	    siftDown(i, moved);
 	    if (es[i] == moved) {
 	        siftUp(i, moved);
 	        if (es[i] != moved)
 	            return moved;
 	    }
 	}
        return null;
}
    
private void siftDown(int k, E x) {
    if (comparator != null)
        siftDownUsingComparator(k, x, queue, size, comparator);
    else
        siftDownComparable(k, x, queue, size);
}

private static <T> void siftDownComparable(int k, T x, Object[] es, int n) {
    // assert n > 0;
    Comparable<? super T> key = (Comparable<? super T>)x;
    int half = n >>> 1;           // loop while a non-leaf
    while (k < half) {
        int child = (k << 1) + 1; // assume left child is least
        Object c = es[child];
        int right = child + 1;
        if (right < n &&
            ((Comparable<? super T>) c).compareTo((T) es[right]) > 0)
            c = es[child = right];
        if (key.compareTo((T) c) <= 0)
            break;
        es[k] = c;
        k = child;
    }
    es[k] = key;
}
```

#### 出列

出列操作是删除操作的一种，也涉及堆的调整操作

```java
public E poll() {
	final Object[] es;
	final E result;

	if ((result = (E) ((es = queue)[0])) != null) {
	    modCount++;
	    final int n;
	    final E x = (E) es[(n = --size)];
	    es[n] = null;
	    if (n > 0) {
	        final Comparator<? super E> cmp;
	        if ((cmp = comparator) == null)
	            siftDownComparable(0, x, es, n);
	        else
	            siftDownUsingComparator(0, x, es, n, cmp);
	    }
	}
	return result;
}
```



### 🖊哈希表

#### 重要属性

从重要属性中可以看出，当拉链法中元素较多时，将拉链的结构(链表)转换为树。

```java
/* ---------------- Fields -------------- */

/**
 * The table, initialized on first use, and resized as
 * necessary. When allocated, length is always a power of two.
 * (We also tolerate length zero in some operations to allow
 * bootstrapping mechanics that are currently not needed.)
 */
transient Node<K,V>[] table;

/**
 * Holds cached entrySet(). Note that AbstractMap fields are used
 * for keySet() and values().
 */
transient Set<Map.Entry<K,V>> entrySet;
    /**
     * The default initial capacity - MUST be a power of two.
     */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

/**
 * The maximum capacity, used if a higher value is implicitly specified
 * by either of the constructors with arguments.
 * MUST be a power of two <= 1<<30.
 */
static final int MAXIMUM_CAPACITY = 1 << 30;

/**
 * The load factor used when none specified in constructor.
 */
static final float DEFAULT_LOAD_FACTOR = 0.75f;

/**
 * The bin count threshold for using a tree rather than list for a
 * bin.  Bins are converted to trees when adding an element to a
 * bin with at least this many nodes. The value must be greater
 * than 2 and should be at least 8 to mesh with assumptions in
 * tree removal about conversion back to plain bins upon
 * shrinkage.
 */
static final int TREEIFY_THRESHOLD = 8;

/**
 * The bin count threshold for untreeifying a (split) bin during a
 * resize operation. Should be less than TREEIFY_THRESHOLD, and at
 * most 6 to mesh with shrinkage detection under removal.
 */
static final int UNTREEIFY_THRESHOLD = 6;

/**
 * The smallest table capacity for which bins may be treeified.
 * (Otherwise the table is resized if too many nodes in a bin.)
 * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
 * between resizing and treeification thresholds.
 */
static final int MIN_TREEIFY_CAPACITY = 64;
```

#### 结点封装

```java
/**
 * Basic hash bin node, used for most entries.  (See below for
 * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
 */
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }

    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }

    public final int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public final V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }

    public final boolean equals(Object o) {
        if (o == this)
            return true;

        return o instanceof Map.Entry<?, ?> e
                && Objects.equals(key, e.getKey())
                && Objects.equals(value, e.getValue());
    }
}

// 下列函数用于创建节点 (next 用于拉链法)
// Create a regular (non-tree) node
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
    return new Node<>(hash, key, value, next);
}
```

其中

> Map.Entry

封装了 **键值、 值、 哈希方法、拷贝方法、比较方法、判等方法**


#### 哈希函数

```java
/**
 * Computes key.hashCode() and spreads (XORs) higher bits of hash
 * to lower.  Because the table uses power-of-two masking, sets of
 * hashes that vary only in bits above the current mask will
 * always collide. (Among known examples are sets of Float keys
 * holding consecutive whole numbers in small tables.)  So we
 * apply a transform that spreads the impact of higher bits
 * downward. There is a tradeoff between speed, utility, and
 * quality of bit-spreading. Because many common sets of hashes
 * are already reasonably distributed (so don't benefit from
 * spreading), and because we use trees to handle large sets of
 * collisions in bins, we just XOR some shifted bits in the
 * cheapest possible way to reduce systematic lossage, as well as
 * to incorporate impact of the highest bits that would otherwise
 * never be used in index calculations because of table bounds.
 */
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

代码中的 *hashcode* 函数由对象实现。Object 类中的 *hashcode* 方法为 native 方法。

碰撞问题

#### 插入

```java
/**
 * Associates the specified value with the specified key in this map.
 * If the map previously contained a mapping for the key, the old
 * value is replaced.
 *
 * @param key key with which the specified value is to be associated
 * @param value value to be associated with the specified key
 * @return the previous value associated with {@code key}, or
 *         {@code null} if there was no mapping for {@code key}.
 *         (A {@code null} return can also indicate that the map
 *         previously associated {@code null} with {@code key}.)
 */
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

/**
 * Implements Map.put and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @param value the value to put
 * @param onlyIfAbsent if true, don't change existing value
 * @param evict if false, the table is in creation mode.
 * @return previous value, or null if none
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);  // 该位置为空，直接插入
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                // 可以观察到，当 hash 值冲突时会利用 equals 方法进一步比较键值是否相同
                if (e.hash == hash && 
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

#### 查找

查找过程与插入的遍历过程类似。

```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(key)) == null ? null : e.value;
}

 /**
 * Implements Map.get and related methods.
 *
 * @param key the key
 * @return the node, or null if none
 */
final Node<K,V> getNode(Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n, hash; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & (hash = hash(key))]) != null) {
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

剩余的 contains 类函数都是基于上述函数实现的。

#### 拉链到树的转换

当拉链长度超过阈值时，链表将转换为树 (**红黑树**) 

```java
/**
 * Replaces all linked nodes in bin at index for given hash unless
 * table is too small, in which case resizes instead.
 */
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}

/**
 * Forms tree of the nodes linked from this node.
 */
final void treeify(Node<K,V>[] tab) {
    TreeNode<K,V> root = null;
    for (TreeNode<K,V> x = this, next; x != null; x = next) {
        next = (TreeNode<K,V>)x.next;
        x.left = x.right = null;
        if (root == null) {
            x.parent = null;
            x.red = false;
            root = x;
        }
        else {
            K k = x.key;
            int h = x.hash;
            Class<?> kc = null;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph;
                K pk = p.key;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0)
                    dir = tieBreakOrder(k, pk);

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    x.parent = xp;
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    root = balanceInsertion(root, x);
                    break;
                }
            }
        }
    }
    moveRootToFront(tab, root);
}
```

以下是 **红黑树** 部分的主要调整代码

```java
static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
    x.red = true;
    for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
        if ((xp = x.parent) == null) {
            x.red = false;
            return x;
        }
        else if (!xp.red || (xpp = xp.parent) == null)
            return root;
        if (xp == (xppl = xpp.left)) {
            if ((xppr = xpp.right) != null && xppr.red) {
                xppr.red = false;
                xp.red = false;
                xpp.red = true;
                x = xpp;
            }
            else {
                if (x == xp.right) {
                    root = rotateLeft(root, x = xp);
                    xpp = (xp = x.parent) == null ? null : xp.parent;
                }
                if (xp != null) {
                    xp.red = false;
                    if (xpp != null) {
                        xpp.red = true;
                        root = rotateRight(root, xpp);
                    }
                }
            }
        }
        else {
            if (xppl != null && xppl.red) {
                xppl.red = false;
                xp.red = false;
                xpp.red = true;
                x = xpp;
            }
            else {
                if (x == xp.left) {
                    root = rotateRight(root, x = xp);
                    xpp = (xp = x.parent) == null ? null : xp.parent;
                }
                if (xp != null) {
                    xp.red = false;
                    if (xpp != null) {
                        xpp.red = true;
                        root = rotateLeft(root, xpp);
                    }
                }
            }
        }
    }
}
```

```java
/**
 * Ensures that the given root is the first node of its bin.
 */
static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
    int n;
    if (root != null && tab != null && (n = tab.length) > 0) {
        int index = (n - 1) & root.hash;
        TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
        if (root != first) {
            Node<K,V> rn;
            tab[index] = root;
            TreeNode<K,V> rp = root.prev;
            if ((rn = root.next) != null)
                ((TreeNode<K,V>)rn).prev = rp;
            if (rp != null)
                rp.next = rn;
            if (first != null)
                first.prev = root;
            root.next = first;
            root.prev = null;
        }
        assert checkInvariants(root);
    }
}
```

```java
/**
 * Tie-breaking utility for ordering insertions when equal
 * hashCodes and non-comparable. We don't require a total
 * order, just a consistent insertion rule to maintain
 * equivalence across rebalancings. Tie-breaking further than
 * necessary simplifies testing a bit.
 */
static int tieBreakOrder(Object a, Object b) {
    int d;
    if (a == null || b == null ||
        (d = a.getClass().getName().
         compareTo(b.getClass().getName())) == 0)
        d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
             -1 : 1);
    return d;
}
```



#### 删除

```java
/**
 * Removes the mapping for the specified key from this map if present.
 *
 * @param  key key whose mapping is to be removed from the map
 * @return the previous value associated with {@code key}, or
 *         {@code null} if there was no mapping for {@code key}.
 *         (A {@code null} return can also indicate that the map
 *         previously associated {@code null} with {@code key}.)
 */
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}

/**
 * Implements Map.remove and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @param value the value to match if matchValue, else ignored
 * @param matchValue if true only remove if value is equal
 * @param movable if false do not move other nodes while removing
 * @return the node, or null if none
 */
final Node<K,V> removeNode(int hash, Object key, Object value,
                           boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
        else if ((e = p.next) != null) {
            if (p instanceof TreeNode)
                node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
            else {
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key ||
                         (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
            }
        }
        if (node != null && (!matchValue || (v = node.value) == value ||
                             (value != null && value.equals(v)))) {
            if (node instanceof TreeNode)
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
            else if (node == p)
                tab[index] = node.next; // 拉链法：直接取 next 指向的节点来填补当前桶位置
            else
                p.next = node.next;
            ++modCount;
            --size;
            afterNodeRemoval(node);
            return node;
        }
    }
    return null;
}
```

### 🖊复合结构：LinkedHashMap

LinkedHashMap 同时实现了 HashMap 与 双向链表的功能，在 HashMap 的功能基础上，还可依照插入顺序访问节点。顺序访问可基于维护的双向链表进行实现。

从下列代码 ( 结合 HashMap 的插入、查找源码 ) 可以看出，**写入操作** 涉及的元素都会维护在一个双向链表 (通过重写 *newNode* 函数 ) 中。也可以观察到，改写

> removeEldestEntry

方法可用于判断是否移除最老的元素。

```java
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    if (evict && (first = head) != null && removeEldestEntry(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}

public V get(Object key) {
    Node<K,V> e;
    if ((e = getNode(key)) == null)
        return null;
    if (accessOrder)
        afterNodeAccess(e);
    return e.value;
}

public void forEach(BiConsumer<? super K, ? super V> action) {
    if (action == null)
        throw new NullPointerException();
    int mc = modCount;
    for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
        action.accept(e.key, e.value);
    if (modCount != mc)
        throw new ConcurrentModificationException();
}
```

双向链表的维护是通过重写 *newNode* 函数实现的。其中下列的 

> Entry

结构是 *Node* 的子类，含有前后指针，可用于构建双向链表节点。

```java
/**
 * HashMap.Node subclass for normal LinkedHashMap entries.
 */
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}
    
Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
    LinkedHashMap.Entry<K,V> p =
        new LinkedHashMap.Entry<>(hash, key, value, e);
    linkNodeLast(p);
    return p;
}
```



## 🖊多线程

### 多线程的使用

多线程基于 **Thread** 类实现。常用的方案是在自己的类中实现 **Runnable** 接口。

```java
/**
 * Initializes a new platform {@code Thread}. This constructor has the same
 * effect as {@linkplain #Thread(ThreadGroup,Runnable,String) Thread}
 * {@code (null, task, gname)}, where {@code gname} is a newly generated
 * name. Automatically generated names are of the form
 * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
 *
 * <p> For a non-null task, invoking this constructor directly is equivalent to:
 * <pre>{@code Thread.ofPlatform().unstarted(task); }</pre>
 *
 * @param  task
 *         the object whose {@code run} method is invoked when this thread
 *         is started. If {@code null}, this classes {@code run} method does
 *         nothing.
 *
 * @see <a href="#inheritance">Inheritance when creating threads</a>
 */
public Thread(Runnable task) {
    this(null, null, 0, task, 0, null);
}
```

在 Thread 类中，将封装我们类重写的

> run() 

方法。

```java
/**
 * This method is run by the thread when it executes. Subclasses of {@code
 * Thread} may override this method.
 *
 * <p> This method is not intended to be invoked directly. If this thread is a
 * platform thread created with a {@link Runnable} task then invoking this method
 * will invoke the task's {@code run} method. If this thread is a virtual thread
 * then invoking this method directly does nothing.
 *
 * @implSpec The default implementation executes the {@link Runnable} task that
 * the {@code Thread} was created with. If the thread was created without a task
 * then this method does nothing.
 */
@Override
public void run() {
    Runnable task = holder.task;
    if (task != null) {
        task.run();
    }
}
```

#### Thread 类重要属性

```java
public class Thread implements Runnable {
    /* Make sure registerNatives is the first thing <clinit> does. */
    private static native void registerNatives();
    static {
        registerNatives();
    }

    /* Reserved for exclusive use by the JVM, maybe move to FieldHolder */
    private long eetop;

    // thread id
    private final long tid;

    // thread name
    private volatile String name;

    // interrupt status (read/written by VM)
    volatile boolean interrupted;

    // context ClassLoader
    private volatile ClassLoader contextClassLoader;

    // inherited AccessControlContext, this could be moved to FieldHolder
    @SuppressWarnings("removal")
    private AccessControlContext inheritedAccessControlContext;

    // Additional fields for platform threads.
    // All fields, except task, are accessed directly by the VM.
    private static class FieldHolder {
        final ThreadGroup group;
        final Runnable task;
        final long stackSize;
        volatile int priority;
        volatile boolean daemon;
        volatile int threadStatus;
        boolean stillborn;

        FieldHolder(ThreadGroup group,
                    Runnable task,
                    long stackSize,
                    int priority,
                    boolean daemon) {
            this.group = group;
            this.task = task;
            this.stackSize = stackSize;
            this.priority = priority;
            if (daemon)
                this.daemon = true;
        }
    }
    private final FieldHolder holder;

    /*
     * ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals;

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
    ThreadLocal.ThreadLocalMap inheritableThreadLocals;

    /*
     * Extent locals binding are maintained by the ExtentLocal class.
     */
    private Object extentLocalBindings;
```

#### 线程的启动

```java
/**
 * Schedules this thread to begin execution. The thread will execute
 * independently of the current thread.
 *
 * <p> A thread can be started at most once. In particular, a thread can not
 * be restarted after it has terminated.
 *
 * @throws IllegalThreadStateException if the thread was already started
 */
public void start() {
    synchronized (this) {
        // zero status corresponds to state "NEW".
        if (holder.threadStatus != 0)
            throw new IllegalThreadStateException();
        start0();
    }
}

private native void start0(); // native 方法
```

#### 线程的中断

```java
public void interrupt() {
    if (this != Thread.currentThread()) {
        checkAccess();

        // thread may be blocked in an I/O operation
        synchronized (interruptLock) {
            Interruptible b = nioBlocker;
            if (b != null) {
                interrupted = true;
                interrupt0();  // inform VM of interrupt
                b.interrupt(this);
                return;
            }
        }
    }
    interrupted = true;
    interrupt0();  // inform VM of interrupt
}
```

#### 等待线程终止

```java
/**
 * Waits at most {@code millis} milliseconds for this thread to terminate.
 * A timeout of {@code 0} means to wait forever.
 * This method returns immediately, without waiting, if the thread has not
 * been {@link #start() started}.
 *
 * @implNote
 * For platform threads, the implementation uses a loop of {@code this.wait}
 * calls conditioned on {@code this.isAlive}. As a thread terminates the
 * {@code this.notifyAll} method is invoked. It is recommended that
 * applications not use {@code wait}, {@code notify}, or
 * {@code notifyAll} on {@code Thread} instances.
 *
 * @param  millis
 *         the time to wait in milliseconds
 *
 * @throws  IllegalArgumentException
 *          if the value of {@code millis} is negative
 *
 * @throws  InterruptedException
 *          if any thread has interrupted the current thread. The
 *          <i>interrupted status</i> of the current thread is
 *          cleared when this exception is thrown.
 */
public final void join(long millis) throws InterruptedException {
    if (millis < 0)
        throw new IllegalArgumentException("timeout value is negative");

    if (this instanceof VirtualThread vthread) {
        if (isAlive()) {
            long nanos = MILLISECONDS.toNanos(millis);
            vthread.joinNanos(nanos);
        }
        return;
    }

    synchronized (this) {
        if (millis > 0) {
            if (isAlive()) {
                final long startTime = System.nanoTime();
                long delay = millis;
                do {
                    wait(delay);
                } while (isAlive() && (delay = millis -
                         NANOSECONDS.toMillis(System.nanoTime() - startTime)) > 0);
            }
        } else {
            while (isAlive()) {
                wait(0);
            }
        }
    }
}
```

#### 调整线程优先级

```java
public final void setPriority(int newPriority) {
    checkAccess();
    if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
        throw new IllegalArgumentException();
    }
    if (!isVirtual()) {
        priority(newPriority);
    }
}

void priority(int newPriority) {
    ThreadGroup g = holder.group;
    if (g != null) {
        int maxPriority = g.getMaxPriority();
        if (newPriority > maxPriority) {
            newPriority = maxPriority;
        }
        setPriority0(holder.priority = newPriority);
    }
}
```

#### Yield 操作

```java
public static void yield() {
    if (currentThread() instanceof VirtualThread vthread) {
        vthread.tryYield();
    } else {
        yield0();
    }
}

private static native void yield0();

 /**
 * Attempts to yield the current virtual thread (Thread.yield).
 */
void tryYield() {
    assert Thread.currentThread() == this;
    setState(YIELDING);
    try {
        yieldContinuation();
    } finally {
        assert Thread.currentThread() == this;
        if (state() != RUNNING) {
            assert state() == YIELDING;
            setState(RUNNING);
        }
    }
}

/**
 * Unmounts this virtual thread, invokes Continuation.yield, and re-mounts the
 * thread when continued. When enabled, JVMTI must be notified from this method.
 * @return true if the yield was successful
 */
@ChangesCurrentThread
private boolean yieldContinuation() {
    boolean notifyJvmti = notifyJvmtiEvents;

    // unmount
    if (notifyJvmti) notifyJvmtiUnmountBegin(false);
    unmount();
    try {
        return Continuation.yield(VTHREAD_SCOPE);
    } finally {
        // re-mount
        mount();
        if (notifyJvmti) notifyJvmtiMountEnd(false);
    }
}
```

#### 线程的睡眠

```java
/**
 * Causes the currently executing thread to sleep (temporarily cease
 * execution) for the specified number of milliseconds, subject to
 * the precision and accuracy of system timers and schedulers. The thread
 * does not lose ownership of any monitors.
 *
 * @param  millis
 *         the length of time to sleep in milliseconds
 *
 * @throws  IllegalArgumentException
 *          if the value of {@code millis} is negative
 *
 * @throws  InterruptedException
 *          if any thread has interrupted the current thread. The
 *          <i>interrupted status</i> of the current thread is
 *          cleared when this exception is thrown.
 */
public static void sleep(long millis) throws InterruptedException {
    if (millis < 0) {
        throw new IllegalArgumentException("timeout value is negative");
    }

    if (currentThread() instanceof VirtualThread vthread) {
        long nanos = MILLISECONDS.toNanos(millis);
        vthread.sleepNanos(nanos);
        return;
    }

    if (ThreadSleepEvent.isTurnedOn()) {
        ThreadSleepEvent event = new ThreadSleepEvent();
        try {
            event.time = MILLISECONDS.toNanos(millis);
            event.begin();
            sleep0(millis);
        } finally {
            event.commit();
        }
    } else {
        sleep0(millis);
    }
}

private static native void sleep0(long millis) throws InterruptedException;
```



#### 平台线程 & 虚拟线程

由上述代码可以发现，Java 线程可分为 **平台线程** 与 **虚拟线程**。它们的特点如下：（摘自源代码文档）

> **Platform threads**
> Thread supports the creation of platform threads that are typically mapped 1:1 to kernel threads scheduled by the operating system. Platform threads will usually have a large stack and other resources that are maintained by the operating system. Platforms threads are suitable for executing all types of tasks but may be a limited resource.
> Platform threads get an automatically generated thread name by default.
> Platform threads are designated daemon or non-daemon threads. When the Java virtual machine starts up, there is usually one non-daemon thread (the thread that typically calls the application's main method). The Java virtual machine terminates when all started non-daemon threads have terminated. Unstarted non-daemon threads do not prevent the Java virtual machine from terminating. The Java virtual machine can also be terminated by invoking the Runtime.exit(int) method, in which case it will terminate even if there are non-daemon threads still running.
> In addition to the daemon status, platform threads have a thread priority and are members of a thread group.



> **Virtual threads**
> Thread also supports the creation of virtual threads. Virtual threads are typically user-mode threads scheduled by the Java runtime rather than the operating system. Virtual threads will typically require few resources and a single Java virtual machine may support millions of virtual threads. Virtual threads are suitable for executing tasks that spend most of the time blocked, often waiting for I/O operations to complete. Virtual threads are not intended for long running CPU intensive operations.
> Virtual threads typically employ a small set of platform threads used as carrier threads. Locking and I/O operations are examples of operations where a carrier thread may be re-scheduled from one virtual thread to another. Code executing in a virtual thread is not aware of the underlying carrier thread. The currentThread() method, used to obtain a reference to the current thread, will always return the Thread object for the virtual thread.
> Virtual threads do not have a thread name by default. The getName method returns the empty string if a thread name is not set.
> Virtual threads are daemon threads and so do not prevent the Java virtual machine from terminating. Virtual threads have a fixed thread priority that cannot be changed.

可使用下列方法创建虚拟线程

```java
Thread thread1 = Thread.ofVirtual().start(runnable);
```

其中，对于部分 jdk 版本，虚拟线程为预览版功能，为使用预览功能，需要在 Idea IDE 同时 修改

1. Java 编译器
2. 运行 VM 参数

并为上述增加:

> --enable-preview

#### 线程同步

线程同步的常用方法：

 1. synchronized 关键字 （可修饰整个方法 或 代码段）
 2. volatile 关键字 (修饰变量)
 3. 创建线程本地副本变量 (隔离变量)
 4. 使用 **ReentrantLock**
5. **阻塞队列**

##### 阻塞队列

###### 实现类

|        实现类         |                             简介                             |
| :-------------------: | :----------------------------------------------------------: |
|  ArrayBlockingQueue   |               一个由数组结构组成的有界阻塞队列               |
|  LinkedBlockingQueue  |               一个由链表结构组成的有界阻塞队列               |
| PriorityBlockingQueue |               一个支持优先级排序的无界阻塞队列               |
|      DelayQueue       |             一个使用优先级队列实现的无界阻塞队列             |
|   SynchronousQueue    |                   一个不存储元素的阻塞队列                   |
|  LinkedTransferQueue  | 一个由链表结构组成的无界阻塞队列（实现了继承于 BlockingQueue 的 TransferQueue） |
|  LinkedBlockingDeque  |               一个由链表结构组成的双向阻塞队列               |



*下述为核心操作的核心代码 ( 以 **ArrayBlockingQueue** 为例 )*

###### 入列

由下述源码可观察到，底层使用了 **ReentrantLock** 可重入锁。

```java
/**
 * Inserts the specified element at the tail of this queue if it is
 * possible to do so immediately without exceeding the queue's capacity,
 * returning {@code true} upon success and {@code false} if this queue
 * is full.  This method is generally preferable to method {@link #add},
 * which can fail to insert an element only by throwing an exception.
 *
 * @throws NullPointerException if the specified element is null
 */
public boolean offer(E e) {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        if (count == items.length)
            return false;
        else {
            enqueue(e);
            return true;
        }
    } finally {
        lock.unlock();
    }
}

/**
 * Inserts the specified element at the tail of this queue, waiting
 * up to the specified wait time for space to become available if
 * the queue is full.
 *
 * @throws InterruptedException {@inheritDoc}
 * @throws NullPointerException {@inheritDoc}
 */
public boolean offer(E e, long timeout, TimeUnit unit)
    throws InterruptedException {

    Objects.requireNonNull(e);
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length) {
            if (nanos <= 0L)
                return false;
            nanos = notFull.awaitNanos(nanos); // 等待超时
        }
        enqueue(e);
        return true;
    } finally {
        lock.unlock();
    }
}
```

###### 出列

```java
public E poll() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return (count == 0) ? null : dequeue();
    } finally {
        lock.unlock();
    }
}

public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == 0) {
            if (nanos <= 0L)
                return null;
            nanos = notEmpty.awaitNanos(nanos);
        }
        return dequeue();
    } finally {
        lock.unlock();
    }
}

 /**
 * Extracts element at current take position, advances, and signals.
 * Call only when holding lock.
 */
private E dequeue() {
    // assert lock.isHeldByCurrentThread();
    // assert lock.getHoldCount() == 1;
    // assert items[takeIndex] != null;
    final Object[] items = this.items;
    @SuppressWarnings("unchecked")
    E e = (E) items[takeIndex];
    items[takeIndex] = null;
    if (++takeIndex == items.length) takeIndex = 0;
    count--;
    if (itrs != null)
        itrs.elementDequeued();
    notFull.signal();
    return e;
}
```

###### 检查

```java
public E peek() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return itemAt(takeIndex); // null when queue is empty
    } finally {
        lock.unlock();
    }
}
```

##### 线程等待与唤醒

主要方法如下 ( **Object** 类方法 ) ：

```java
/**
 * Causes the current thread to wait until it is awakened, typically
 * by being <em>notified</em> or <em>interrupted</em>.
 * <p>
 * In all respects, this method behaves as if {@code wait(0L, 0)}
 * had been called. See the specification of the {@link #wait(long, int)} method
 * for details.
 *
 * @throws IllegalMonitorStateException if the current thread is not
 *         the owner of the object's monitor
 * @throws InterruptedException if any thread interrupted the current thread before or
 *         while the current thread was waiting. The <em>interrupted status</em> of the
 *         current thread is cleared when this exception is thrown.
 * @see    #notify()
 * @see    #notifyAll()
 * @see    #wait(long)
 * @see    #wait(long, int)
 */
public final void wait() throws InterruptedException {
    wait(0L);
}

/**
 * Causes the current thread to wait until it is awakened, typically
 * by being <em>notified</em> or <em>interrupted</em>, or until a
 * certain amount of real time has elapsed.
 * <p>
 * In all respects, this method behaves as if {@code wait(timeoutMillis, 0)}
 * had been called. See the specification of the {@link #wait(long, int)} method
 * for details.
 *
 * @param  timeoutMillis the maximum time to wait, in milliseconds
 * @throws IllegalArgumentException if {@code timeoutMillis} is negative
 * @throws IllegalMonitorStateException if the current thread is not
 *         the owner of the object's monitor
 * @throws InterruptedException if any thread interrupted the current thread before or
 *         while the current thread was waiting. The <em>interrupted status</em> of the
 *         current thread is cleared when this exception is thrown.
 * @see    #notify()
 * @see    #notifyAll()
 * @see    #wait()
 * @see    #wait(long, int)
 */
public final void wait(long timeoutMillis) throws InterruptedException {
    long comp = Blocker.begin();
    try {
        wait0(timeoutMillis);
    } catch (InterruptedException e) {
        Thread thread = Thread.currentThread();
        if (thread.isVirtual())
            thread.getAndClearInterrupt();
        throw e;
    } finally {
        Blocker.end(comp);
    }
}

// final modifier so method not in vtable
private final native void wait0(long timeoutMillis) throws InterruptedException;
```

```java
/**
 * Wakes up a single thread that is waiting on this object's
 * monitor. If any threads are waiting on this object, one of them
 * is chosen to be awakened. The choice is arbitrary and occurs at
 * the discretion of the implementation. A thread waits on an object's
 * monitor by calling one of the {@code wait} methods.
 * <p>
 * The awakened thread will not be able to proceed until the current
 * thread relinquishes the lock on this object. The awakened thread will
 * compete in the usual manner with any other threads that might be
 * actively competing to synchronize on this object; for example, the
 * awakened thread enjoys no reliable privilege or disadvantage in being
 * the next thread to lock this object.
 * <p>
 * This method should only be called by a thread that is the owner
 * of this object's monitor. A thread becomes the owner of the
 * object's monitor in one of three ways:
 * <ul>
 * <li>By executing a synchronized instance method of that object.
 * <li>By executing the body of a {@code synchronized} statement
 *     that synchronizes on the object.
 * <li>For objects of type {@code Class,} by executing a
 *     static synchronized method of that class.
 * </ul>
 * <p>
 * Only one thread at a time can own an object's monitor.
 *
 * @throws  IllegalMonitorStateException  if the current thread is not
 *               the owner of this object's monitor.
 * @see        java.lang.Object#notifyAll()
 * @see        java.lang.Object#wait()
 */
@IntrinsicCandidate
public final native void notify();

/**
 * Wakes up all threads that are waiting on this object's monitor. A
 * thread waits on an object's monitor by calling one of the
 * {@code wait} methods.
 * <p>
 * The awakened threads will not be able to proceed until the current
 * thread relinquishes the lock on this object. The awakened threads
 * will compete in the usual manner with any other threads that might
 * be actively competing to synchronize on this object; for example,
 * the awakened threads enjoy no reliable privilege or disadvantage in
 * being the next thread to lock this object.
 * <p>
 * This method should only be called by a thread that is the owner
 * of this object's monitor. See the {@code notify} method for a
 * description of the ways in which a thread can become the owner of
 * a monitor.
 *
 * @throws  IllegalMonitorStateException  if the current thread is not
 *               the owner of this object's monitor.
 * @see        java.lang.Object#notify()
 * @see        java.lang.Object#wait()
 */
@IntrinsicCandidate
public final native void notifyAll();
```

> *值得注意的是：必须首先获取 wait 与 notify 用于同步的对象的所有权，即采用 **synchronized** 关键字获取对象所有权。*

### ⚡线程池

#### 📝ThreadPoolExecutor

##### 官方简介

> An ExecutorService that executes each submitted task using one of possibly several pooled threads, normally configured using Executors factory methods.
> Thread pools address two different problems: they usually provide improved performance when executing large numbers of asynchronous tasks, due to reduced per-task invocation overhead, and they provide a means of bounding and managing the resources, including threads, consumed when executing a collection of tasks. Each ThreadPoolExecutor also maintains some basic statistics, such as the number of completed tasks.
> To be useful across a wide range of contexts, this class provides many adjustable parameters and extensibility hooks. However, programmers are urged to use the more convenient Executors factory methods Executors.newCachedThreadPool (unbounded thread pool, with automatic thread reclamation), Executors.newFixedThreadPool (fixed size thread pool) and Executors.newSingleThreadExecutor (single background thread), that preconfigure settings for the most common usage scenarios. Otherwise, use the following guide when manually configuring and tuning this class:
>
> **Core and maximum pool sizes**
>
> A ThreadPoolExecutor will automatically adjust the pool size (see getPoolSize) according to the bounds set by corePoolSize (see getCorePoolSize) and maximumPoolSize (see getMaximumPoolSize). When a new task is submitted in method execute(Runnable), if fewer than corePoolSize threads are running, a new thread is created to handle the request, even if other worker threads are idle. Else if fewer than maximumPoolSize threads are running, a new thread will be created to handle the request only if the queue is full. By setting corePoolSize and maximumPoolSize the same, you create a fixed-size thread pool. By setting maximumPoolSize to an essentially unbounded value such as Integer.MAX_VALUE, you allow the pool to accommodate an arbitrary number of concurrent tasks. Most typically, core and maximum pool sizes are set only upon construction, but they may also be changed dynamically using setCorePoolSize and setMaximumPoolSize.
>
> **On-demand construction**
> By default, even core threads are initially created and started only when new tasks arrive, but this can be overridden dynamically using method prestartCoreThread or prestartAllCoreThreads. You probably want to prestart threads if you construct the pool with a non-empty queue.
>
> **Creating new threads**
> New threads are created using a ThreadFactory. If not otherwise specified, a Executors.defaultThreadFactory is used, that creates threads to all be in the same ThreadGroup and with the same NORM_PRIORITY priority and non-daemon status. By supplying a different ThreadFactory, you can alter the thread's name, thread group, priority, daemon status, etc. If a ThreadFactory fails to create a thread when asked by returning null from newThread, the executor will continue, but might not be able to execute any tasks. Threads should possess the "modifyThread" RuntimePermission. If worker threads or other threads using the pool do not possess this permission, service may be degraded: configuration changes may not take effect in a timely manner, and a shutdown pool may remain in a state in which termination is possible but not completed.
>
> **Keep-alive times**
> If the pool currently has more than corePoolSize threads, excess threads will be terminated if they have been idle for more than the keepAliveTime (see getKeepAliveTime(TimeUnit)). This provides a means of reducing resource consumption when the pool is not being actively used. If the pool becomes more active later, new threads will be constructed. This parameter can also be changed dynamically using method setKeepAliveTime(long, TimeUnit). Using a value of Long.MAX_VALUE TimeUnit.NANOSECONDS effectively disables idle threads from ever terminating prior to shut down. By default, the keep-alive policy applies only when there are more than corePoolSize threads, but method allowCoreThreadTimeOut(boolean) can be used to apply this time-out policy to core threads as well, so long as the keepAliveTime value is non-zero.
>
> **Queuing**
> Any BlockingQueue may be used to transfer and hold submitted tasks. The use of this queue interacts with pool sizing:
>
> 1. If fewer than corePoolSize threads are running, the Executor always prefers adding a new thread rather than queuing.
>
> 2. If corePoolSize or more threads are running, the Executor always prefers queuing a request rather than adding a new thread.
>
> 3. If a request cannot be queued, a new thread is created unless this would exceed maximumPoolSize, in which case, the task will be rejected.
>
> 
>
> There are three general strategies for queuing:
>
> 1. Direct handoffs. A good default choice for a work queue is a SynchronousQueue that hands off tasks to threads without otherwise holding them. Here, an attempt to queue a task will fail if no threads are immediately available to run it, so a new thread will be constructed. This policy avoids lockups when handling sets of requests that might have internal dependencies. Direct handoffs generally require unbounded maximumPoolSizes to avoid rejection of new submitted tasks. This in turn admits the possibility of unbounded thread growth when commands continue to arrive on average faster than they can be processed.
>
> 2. Unbounded queues. Using an unbounded queue (for example a LinkedBlockingQueue without a predefined capacity) will cause new tasks to wait in the queue when all corePoolSize threads are busy. Thus, no more than corePoolSize threads will ever be created. (And the value of the maximumPoolSize therefore doesn't have any effect.) This may be appropriate when each task is completely independent of others, so tasks cannot affect each others execution; for example, in a web page server. While this style of queuing can be useful in smoothing out transient bursts of requests, it admits the possibility of unbounded work queue growth when commands continue to arrive on average faster than they can be processed.
>
> 3. Bounded queues. A bounded queue (for example, an ArrayBlockingQueue) helps prevent resource exhaustion when used with finite maximumPoolSizes, but can be more difficult to tune and control. Queue sizes and maximum pool sizes may be traded off for each other: Using large queues and small pools minimizes CPU usage, OS resources, and context-switching overhead, but can lead to artificially low throughput. If tasks frequently block (for example if they are I/O bound), a system may be able to schedule time for more threads than you otherwise allow. Use of small queues generally requires larger pool sizes, which keeps CPUs busier but may encounter unacceptable scheduling overhead, which also decreases throughput.
>

> **Rejected tasks**
> New tasks submitted in method execute(Runnable) will be rejected when the Executor has been shut down, and also when the Executor uses finite bounds for both maximum threads and work queue capacity, and is saturated. In either case, the execute method invokes the RejectedExecutionHandler.rejectedExecution(Runnable, ThreadPoolExecutor) method of its RejectedExecutionHandler. Four predefined handler policies are provided:
>
> 1. In the default ThreadPoolExecutor.AbortPolicy, the handler throws a runtime RejectedExecutionException upon rejection.
> 2. In ThreadPoolExecutor.CallerRunsPolicy, the thread that invokes execute itself runs the task. This provides a simple feedback control mechanism that will slow down the rate that new tasks are submitted.
> 3. In ThreadPoolExecutor.DiscardPolicy, a task that cannot be executed is simply dropped. This policy is designed only for those rare cases in which task completion is never relied upon.
> 4. In ThreadPoolExecutor.DiscardOldestPolicy, if the executor is not shut down, the task at the head of the work queue is dropped, and then execution is retried (which can fail again, causing this to be repeated.) This po licy is rarely acceptable. In nearly all cases, you should also cancel the task to cause an exception in any component waiting for its completion, and/or log the failure, as illustrated in ThreadPoolExecutor.DiscardOldestPolicy documentation.
>
> It is possible to define and use other kinds of RejectedExecutionHandler classes. Doing so requires some care especially when policies are designed to work only under particular capacity or queuing policies.
>
> **Hook methods**
> This class provides protected overridable beforeExecute(Thread, Runnable) and afterExecute(Runnable, Throwable) methods that are called before and after execution of each task. These can be used to manipulate the execution environment; for example, reinitializing ThreadLocals, gathering statistics, or adding log entries. Additionally, method terminated can be overridden to perform any special processing that needs to be done once the Executor has fully terminated.
> If hook, callback, or BlockingQueue methods throw exceptions, internal worker threads may in turn fail, abruptly terminate, and possibly be replaced.
>
> **Queue maintenance**
> Method getQueue() allows access to the work queue for purposes of monitoring and debugging. Use of this method for any other purpose is strongly discouraged. Two supplied methods, remove(Runnable) and purge are available to assist in storage reclamation when large numbers of queued tasks become cancelled.
>
> **Reclamation**
> A pool that is no longer referenced in a program AND has no remaining threads may be reclaimed (garbage collected) without being explicitly shutdown. You can configure a pool to allow all unused threads to eventually die by setting appropriate keep-alive times, using a lower bound of zero core threads and/or setting allowCoreThreadTimeOut(boolean).
>
> **Extension example**. 
>
> Most extensions of this class override one or more of the protected hook methods. For example, here is a subclass that adds a simple pause/resume feature:
>
> ```java
> class PausableThreadPoolExecutor extends ThreadPoolExecutor {
> 	private boolean isPaused;
> 	private ReentrantLock pauseLock = new ReentrantLock();
> 	private Condition unpaused = pauseLock.newCondition();
> 	
> 	public PausableThreadPoolExecutor(...) { super(...); }
> 	
> 	protected void beforeExecute(Thread t, Runnable r) {
> 	  super.beforeExecute(t, r);
> 	  pauseLock.lock();
> 	  try {
> 	    while (isPaused) unpaused.await();
> 	  } catch (InterruptedException ie) {
> 	    t.interrupt();
> 	  } finally {
> 	    pauseLock.unlock();
> 	  }
> 	}
> 	
> 	public void pause() {
> 	  pauseLock.lock();
> 	  try {
> 	    isPaused = true;
> 	  } finally {
> 	    pauseLock.unlock();
> 	  }
> 	}
> 	
> 	public void resume() {
> 	  pauseLock.lock();
> 	  try {
> 	    isPaused = false;
> 	    unpaused.signalAll();
> 	  } finally {
> 	    pauseLock.unlock();
> 	  }
> 	}
> }
> ```

##### 核心构造函数

```java
/**
 * Creates a new {@code ThreadPoolExecutor} with the given initial
 * parameters.
 *
 * @param corePoolSize the number of threads to keep in the pool, even
 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
 * @param maximumPoolSize the maximum number of threads to allow in the
 *        pool
 * @param keepAliveTime when the number of threads is greater than
 *        the core, this is the maximum time that excess idle threads
 *        will wait for new tasks before terminating.
 * @param unit the time unit for the {@code keepAliveTime} argument
 * @param workQueue the queue to use for holding tasks before they are
 *        executed.  This queue will hold only the {@code Runnable}
 *        tasks submitted by the {@code execute} method.
 * @param threadFactory the factory to use when the executor
 *        creates a new thread
 * @param handler the handler to use when execution is blocked
 *        because the thread bounds and queue capacities are reached
 * @throws IllegalArgumentException if one of the following holds:<br>
 *         {@code corePoolSize < 0}<br>
 *         {@code keepAliveTime < 0}<br>
 *         {@code maximumPoolSize <= 0}<br>
 *         {@code maximumPoolSize < corePoolSize}
 * @throws NullPointerException if {@code workQueue}
 *         or {@code threadFactory} or {@code handler} is null
 */
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;

    String name = Objects.toIdentityString(this);
    this.container = SharedThreadContainer.create(name);
}
```

其中前 4 个参数 分别 指定 线程池 的 *核心线程数，最大下线程数，线程存活时间与时间单位*。

###### BlockingQueue

该参数指定一个阻塞队列类用于管理线程池，不同的实现类代表着不同的策略，官方提供以下实现类：

|        实现类         |                             简介                             |
| :-------------------: | :----------------------------------------------------------: |
|  ArrayBlockingQueue   |              一个由数组结构组成的有界阻塞队列。              |
|  LinkedBlockingQueue  |              一个由链表结构组成的有界阻塞队列。              |
|   SynchronousQueue    |    一个不存储元素的阻塞队列，即直接提交给线程不保持它们。    |
| PriorityBlockingQueue |              一个支持优先级排序的无界阻塞队列。              |
|      DelayQueue       | 一个使用优先级队列实现的无界阻塞队列，只有在延迟期满时才能从中提取元素。 |
|  LinkedTransferQueue  | 一个由链表结构组成的无界阻塞队列。与 SynchronousQueue 类似，还含有非阻塞方法。 |
|  LinkedBlockingDeque  |              一个由链表结构组成的双向阻塞队列。              |

###### ThreadFactory

此参数用于指定一个线程工厂，主要用于创建线程，默认及正常优先级、守护线程。

ThreadFactory 是一个接口，只要实现了以下方法即为一个线程工厂。

```java
public interface ThreadFactory {

    /**
     * Constructs a new unstarted {@code Thread} to run the given runnable.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     *         create a thread is rejected
     *
     * @see <a href="../../lang/Thread.html#inheritance">Inheritance when
     * creating threads</a>
     */
    Thread newThread(Runnable r);
}
```

可通过

> Executors.*defaultThreadFactory*()

返回一个**默认工厂**，该工厂 默认所有线程优先级为默认优先级，且为非守护线程

```java
/**
 * Returns a default thread factory used to create new threads.
 * This factory creates all new threads used by an Executor in the
 * same {@link ThreadGroup}. If there is a {@link
 * java.lang.SecurityManager}, it uses the group of {@link
 * System#getSecurityManager}, else the group of the thread
 * invoking this {@code defaultThreadFactory} method. Each new
 * thread is created as a non-daemon thread with priority set to
 * the smaller of {@code Thread.NORM_PRIORITY} and the maximum
 * priority permitted in the thread group.  New threads have names
 * accessible via {@link Thread#getName} of
 * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
 * number of this factory, and <em>M</em> is the sequence number
 * of the thread created by this factory.
 * @return a thread factory
 */
public static ThreadFactory defaultThreadFactory() {
    return new DefaultThreadFactory();
}


/**
 * The default thread factory.
 */
private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
        @SuppressWarnings("removal")
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                      poolNumber.getAndIncrement() +
                     "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
```

###### Executors 类 提供的其它线程工厂

以下类 支持修改线程的上下文与类加载器

```java
/**
 * Thread factory capturing access control context and class loader.
 */
private static class PrivilegedThreadFactory extends DefaultThreadFactory {
    @SuppressWarnings("removal")
    final AccessControlContext acc;
    final ClassLoader ccl;

    @SuppressWarnings("removal")
    PrivilegedThreadFactory() {
        super();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Calls to getContextClassLoader from this class
            // never trigger a security check, but we check
            // whether our callers have this permission anyways.
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

            // Fail fast
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        this.acc = AccessController.getContext();
        this.ccl = Thread.currentThread().getContextClassLoader();
    }

    public Thread newThread(final Runnable r) {
        return super.newThread(new Runnable() {
            @SuppressWarnings("removal")
            public void run() {
                AccessController.doPrivileged(new PrivilegedAction<>() {
                    public Void run() {
                        Thread.currentThread().setContextClassLoader(ccl);
                        r.run();
                        return null;
                    }
                }, acc);
            }
        });
    }
}
```

###### RejectedExecutionHandler

RejectedExecutionHandler 为一个接口，指定线程池的拒绝行为

```java
/**
 * A handler for tasks that cannot be executed by a {@link ThreadPoolExecutor}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface RejectedExecutionHandler {

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when
     * {@link ThreadPoolExecutor#execute execute} cannot accept a
     * task.  This may occur when no more threads or queue slots are
     * available because their bounds would be exceeded, or upon
     * shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be
     * propagated to the caller of {@code execute}.
     *
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     * @throws RejectedExecutionException if there is no remedy
     */
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
```

在 任务提交 阶段 (详见 *任务提交* 节) 的以下函数被调用

```java
/**
 * Invokes the rejected execution handler for the given command.
 * Package-protected for use by ScheduledThreadPoolExecutor.
 */
final void reject(Runnable command) {
    handler.rejectedExecution(command, this);
}
```

该接口目前含4个实现类：

**AbortPolicy** 直接抛出异常

```java
/**
 * A handler for rejected tasks that throws a
 * {@link RejectedExecutionException}.
 *
 * This is the default handler for {@link ThreadPoolExecutor} and
 * {@link ScheduledThreadPoolExecutor}.
 */
public static class AbortPolicy implements RejectedExecutionHandler {
    /**
     * Creates an {@code AbortPolicy}.
     */
    public AbortPolicy() { }

    /**
     * Always throws RejectedExecutionException.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     * @throws RejectedExecutionException always
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                             " rejected from " +
                                             e.toString());
    }
}
```

**CallerRunsPolicy** 直接执行该任务，除非线程池被关闭

```java
/**
 * A handler for rejected tasks that runs the rejected task
 * directly in the calling thread of the {@code execute} method,
 * unless the executor has been shut down, in which case the task
 * is discarded.
 */
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    /**
     * Creates a {@code CallerRunsPolicy}.
     */
    public CallerRunsPolicy() { }

    /**
     * Executes task r in the caller's thread, unless the executor
     * has been shut down, in which case the task is discarded.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            r.run();
        }
    }
}
```

**DiscardOldestPolicy** 将队列下一个的未执行任务出列，然后将最新的任务加入并尝试重新提交

```java
public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardOldestPolicy} for the given executor.
         */
        public DiscardOldestPolicy() { }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
```

DiscardPolicy 直接抛弃该任务，不做任何操作

```java
/**
 * A handler for rejected tasks that silently discards the
 * rejected task.
 */
public static class DiscardPolicy implements RejectedExecutionHandler {
    /**
     * Creates a {@code DiscardPolicy}.
     */
    public DiscardPolicy() { }

    /**
     * Does nothing, which has the effect of discarding task r.
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    }
}
```

##### 属性字段

```java
public class ThreadPoolExecutor extends AbstractExecutorService {
    /**
     * The main pool control state, ctl, is an atomic integer packing
     * two conceptual fields
     *   workerCount, indicating the effective number of threads
     *   runState,    indicating whether running, shutting down etc
     *
     * In order to pack them into one int, we limit workerCount to
     * (2^29)-1 (about 500 million) threads rather than (2^31)-1 (2
     * billion) otherwise representable. If this is ever an issue in
     * the future, the variable can be changed to be an AtomicLong,
     * and the shift/mask constants below adjusted. But until the need
     * arises, this code is a bit faster and simpler using an int.
     *
     * The workerCount is the number of workers that have been
     * permitted to start and not permitted to stop.  The value may be
     * transiently different from the actual number of live threads,
     * for example when a ThreadFactory fails to create a thread when
     * asked, and when exiting threads are still performing
     * bookkeeping before terminating. The user-visible pool size is
     * reported as the current size of the workers set.
     *
     * The runState provides the main lifecycle control, taking on values:
     *
     *   RUNNING:  Accept new tasks and process queued tasks
     *   SHUTDOWN: Don't accept new tasks, but process queued tasks
     *   STOP:     Don't accept new tasks, don't process queued tasks,
     *             and interrupt in-progress tasks
     *   TIDYING:  All tasks have terminated, workerCount is zero,
     *             the thread transitioning to state TIDYING
     *             will run the terminated() hook method
     *   TERMINATED: terminated() has completed
     *
     * The numerical order among these values matters, to allow
     * ordered comparisons. The runState monotonically increases over
     * time, but need not hit each state. The transitions are:
     *
     * RUNNING -> SHUTDOWN
     *    On invocation of shutdown()
     * (RUNNING or SHUTDOWN) -> STOP
     *    On invocation of shutdownNow()
     * SHUTDOWN -> TIDYING
     *    When both queue and pool are empty
     * STOP -> TIDYING
     *    When pool is empty
     * TIDYING -> TERMINATED
     *    When the terminated() hook method has completed
     *
     * Threads waiting in awaitTermination() will return when the
     * state reaches TERMINATED.
     *
     * Detecting the transition from SHUTDOWN to TIDYING is less
     * straightforward than you'd like because the queue may become
     * empty after non-empty and vice versa during SHUTDOWN state, but
     * we can only terminate if, after seeing that it is empty, we see
     * that workerCount is 0 (which sometimes entails a recheck -- see
     * below).
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int COUNT_MASK = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c)     { return c & ~COUNT_MASK; }
    private static int workerCountOf(int c)  { return c & COUNT_MASK; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }
    
    // ...
}
```

由属性字段可以观察知道线程池的5种状态。

##### 任务提交

```java
/**
 * Executes the given task sometime in the future.  The task
 * may execute in a new thread or in an existing pooled thread.
 *
 * If the task cannot be submitted for execution, either because this
 * executor has been shutdown or because its capacity has been reached,
 * the task is handled by the current {@link RejectedExecutionHandler}.
 *
 * @param command the task to execute
 * @throws RejectedExecutionException at discretion of
 *         {@code RejectedExecutionHandler}, if the task
 *         cannot be accepted for execution
 * @throws NullPointerException if {@code command} is null
 */
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    /*
     * Proceed in 3 steps:
     *
     * 1. If fewer than corePoolSize threads are running, try to
     * start a new thread with the given command as its first
     * task.  The call to addWorker atomically checks runState and
     * workerCount, and so prevents false alarms that would add
     * threads when it shouldn't, by returning false.
     *
     * 2. If a task can be successfully queued, then we still need
     * to double-check whether we should have added a thread
     * (because existing ones died since last checking) or that
     * the pool shut down since entry into this method. So we
     * recheck state and if necessary roll back the enqueuing if
     * stopped, or start a new thread if there are none.
     *
     * 3. If we cannot queue task, then we try to add a new
     * thread.  If it fails, we know we are shut down or saturated
     * and so reject the task.
     */
    int c = ctl.get();
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        if (! isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    else if (!addWorker(command, false))
        reject(command);
}
```

从源代码中可以看出，任务提交涉及阻塞队列的 **offer** 方法。

同时也可观察到任务提交的规则：

> 1. Running 任务数 < 核心线程数：创建新线程
> 2. 否则 尝试入列
> 3. 入列失败，则拒绝新任务。

##### 任务移除

```java
/**
 * Removes this task from the executor's internal queue if it is
 * present, thus causing it not to be run if it has not already
 * started.
 *
 * <p>This method may be useful as one part of a cancellation
 * scheme.  It may fail to remove tasks that have been converted
 * into other forms before being placed on the internal queue.
 * For example, a task entered using {@code submit} might be
 * converted into a form that maintains {@code Future} status.
 * However, in such cases, method {@link #purge} may be used to
 * remove those Futures that have been cancelled.
 *
 * @param task the task to remove
 * @return {@code true} if the task was removed
 */
public boolean remove(Runnable task) {
    boolean removed = workQueue.remove(task);
    tryTerminate(); // In case SHUTDOWN and now empty
    return removed;
}
```

##### 线程池的关闭

```java
/**
 * Initiates an orderly shutdown in which previously submitted
 * tasks are executed, but no new tasks will be accepted.
 * Invocation has no additional effect if already shut down.
 *
 * <p>This method does not wait for previously submitted tasks to
 * complete execution.  Use {@link #awaitTermination awaitTermination}
 * to do that.
 *
 * @throws SecurityException {@inheritDoc}
 */
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        advanceRunState(SHUTDOWN);
        interruptIdleWorkers();
        onShutdown(); // hook for ScheduledThreadPoolExecutor
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
}
```

#### 📝ThreadPoolExecutor 的封装类

```java
/**
 * Creates a thread pool that reuses a fixed number of threads
 * operating off a shared unbounded queue.  At any point, at most
 * {@code nThreads} threads will be active processing tasks.
 * If additional tasks are submitted when all threads are active,
 * they will wait in the queue until a thread is available.
 * If any thread terminates due to a failure during execution
 * prior to shutdown, a new one will take its place if needed to
 * execute subsequent tasks.  The threads in the pool will exist
 * until it is explicitly {@link ExecutorService#shutdown shutdown}.
 *
 * @param nThreads the number of threads in the pool
 * @return the newly created thread pool
 * @throws IllegalArgumentException if {@code nThreads <= 0}
 */
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}

 /**
 * Creates a thread pool that reuses a fixed number of threads
 * operating off a shared unbounded queue, using the provided
 * ThreadFactory to create new threads when needed.  At any point,
 * at most {@code nThreads} threads will be active processing
 * tasks.  If additional tasks are submitted when all threads are
 * active, they will wait in the queue until a thread is
 * available.  If any thread terminates due to a failure during
 * execution prior to shutdown, a new one will take its place if
 * needed to execute subsequent tasks.  The threads in the pool will
 * exist until it is explicitly {@link ExecutorService#shutdown
 * shutdown}.
 *
 * @param nThreads the number of threads in the pool
 * @param threadFactory the factory to use when creating new threads
 * @return the newly created thread pool
 * @throws NullPointerException if threadFactory is null
 * @throws IllegalArgumentException if {@code nThreads <= 0}
 */
public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>(),
                                  threadFactory);
}
```

```java
 /**
 * Creates a thread pool that creates new threads as needed, but
 * will reuse previously constructed threads when they are
 * available.  These pools will typically improve the performance
 * of programs that execute many short-lived asynchronous tasks.
 * Calls to {@code execute} will reuse previously constructed
 * threads if available. If no existing thread is available, a new
 * thread will be created and added to the pool. Threads that have
 * not been used for sixty seconds are terminated and removed from
 * the cache. Thus, a pool that remains idle for long enough will
 * not consume any resources. Note that pools with similar
 * properties but different details (for example, timeout parameters)
 * may be created using {@link ThreadPoolExecutor} constructors.
 *
 * @return the newly created thread pool
 */
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}

 /**
 * Creates a thread pool that creates new threads as needed, but
 * will reuse previously constructed threads when they are
 * available, and uses the provided
 * ThreadFactory to create new threads when needed.
 *
 * @param threadFactory the factory to use when creating new threads
 * @return the newly created thread pool
 * @throws NullPointerException if threadFactory is null
 */
public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>(),
                                  threadFactory);
}
```

```java
/**
 * Creates an Executor that uses a single worker thread operating
 * off an unbounded queue. (Note however that if this single
 * thread terminates due to a failure during execution prior to
 * shutdown, a new one will take its place if needed to execute
 * subsequent tasks.)  Tasks are guaranteed to execute
 * sequentially, and no more than one task will be active at any
 * given time. Unlike the otherwise equivalent
 * {@code newFixedThreadPool(1)} the returned executor is
 * guaranteed not to be reconfigurable to use additional threads.
 *
 * @return the newly created single-threaded Executor
 */
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}

/**
 * Creates an Executor that uses a single worker thread operating
 * off an unbounded queue, and uses the provided ThreadFactory to
 * create a new thread when needed. Unlike the otherwise
 * equivalent {@code newFixedThreadPool(1, threadFactory)} the
 * returned executor is guaranteed not to be reconfigurable to use
 * additional threads.
 *
 * @param threadFactory the factory to use when creating new threads
 * @return the newly created single-threaded Executor
 * @throws NullPointerException if threadFactory is null
 */
public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>(),
                                threadFactory));
}
```

#### 📝ThreadPoolExecutor 的继承类

```java
/**
A ThreadPoolExecutor that can additionally schedule commands to run after a given delay, or to execute periodically. This class is preferable to java.util.Timer when multiple worker threads are needed, or when the additional flexibility or capabilities of ThreadPoolExecutor (which this class extends) are required.

Delayed tasks execute no sooner than they are enabled, but without any real-time guarantees about when, after they are enabled, they will commence. Tasks scheduled for exactly the same execution time are enabled in first-in-first-out (FIFO) order of submission.

When a submitted task is cancelled before it is run, execution is suppressed. By default, such a cancelled task is not automatically removed from the work queue until its delay elapses. While this enables further inspection and monitoring, it may also cause unbounded retention of cancelled tasks. To avoid this, use setRemoveOnCancelPolicy to cause tasks to be immediately removed from the work queue at time of cancellation.
Successive executions of a periodic task scheduled via scheduleAtFixedRate or scheduleWithFixedDelay do not overlap. While different executions may be performed by different threads, the effects of prior executions happen-before those of subsequent ones.

While this class inherits from ThreadPoolExecutor, a few of the inherited tuning methods are not useful for it. In particular, because it acts as a fixed-sized pool using corePoolSize threads and an unbounded queue, adjustments to maximumPoolSize have no useful effect. Additionally, it is almost never a good idea to set corePoolSize to zero or use allowCoreThreadTimeOut because this may leave the pool without threads to handle tasks once they become eligible to run.

As with ThreadPoolExecutor, if not otherwise specified, this class uses Executors.defaultThreadFactory as the default thread factory, and ThreadPoolExecutor.AbortPolicy as the default rejected execution handler.
*/

public class ScheduledThreadPoolExecutor
        extends ThreadPoolExecutor
        implements ScheduledExecutorService {
    // ...
}


/**
 * Creates a thread pool that can schedule commands to run after a
 * given delay, or to execute periodically.
 * @param corePoolSize the number of threads to keep in the pool,
 * even if they are idle
 * @param threadFactory the factory to use when the executor
 * creates a new thread
 * @return the newly created scheduled thread pool
 * @throws IllegalArgumentException if {@code corePoolSize < 0}
 * @throws NullPointerException if threadFactory is null
 */
public static ScheduledExecutorService newScheduledThreadPool(
        int corePoolSize, ThreadFactory threadFactory) {
    return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
}
```

其构造函数基于基类 ThreadPoolExecutor 的构造函数，且均**未提供指定自定义任务队列的功能**。

```java
/**
 * Creates a new {@code ScheduledThreadPoolExecutor} with the
 * given core pool size.
 *
 * @param corePoolSize the number of threads to keep in the pool, even
 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
 * @throws IllegalArgumentException if {@code corePoolSize < 0}
 */
public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE,
          DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
          new DelayedWorkQueue());
}

/**
 * Creates a new {@code ScheduledThreadPoolExecutor} with the
 * given initial parameters.
 *
 * @param corePoolSize the number of threads to keep in the pool, even
 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
 * @param threadFactory the factory to use when the executor
 *        creates a new thread
 * @throws IllegalArgumentException if {@code corePoolSize < 0}
 * @throws NullPointerException if {@code threadFactory} is null
 */
public ScheduledThreadPoolExecutor(int corePoolSize,
                                   ThreadFactory threadFactory) {
    super(corePoolSize, Integer.MAX_VALUE,
          DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
          new DelayedWorkQueue(), threadFactory);
}

/**
 * Creates a new {@code ScheduledThreadPoolExecutor} with the
 * given initial parameters.
 *
 * @param corePoolSize the number of threads to keep in the pool, even
 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
 * @param handler the handler to use when execution is blocked
 *        because the thread bounds and queue capacities are reached
 * @throws IllegalArgumentException if {@code corePoolSize < 0}
 * @throws NullPointerException if {@code handler} is null
 */
public ScheduledThreadPoolExecutor(int corePoolSize,
                                   RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE,
          DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
          new DelayedWorkQueue(), handler);
}

/**
 * Creates a new {@code ScheduledThreadPoolExecutor} with the
 * given initial parameters.
 *
 * @param corePoolSize the number of threads to keep in the pool, even
 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
 * @param threadFactory the factory to use when the executor
 *        creates a new thread
 * @param handler the handler to use when execution is blocked
 *        because the thread bounds and queue capacities are reached
 * @throws IllegalArgumentException if {@code corePoolSize < 0}
 * @throws NullPointerException if {@code threadFactory} or
 *         {@code handler} is null
 */
public ScheduledThreadPoolExecutor(int corePoolSize,
                                   ThreadFactory threadFactory,
                                   RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE,
          DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
          new DelayedWorkQueue(), threadFactory, handler);
}
```

### 🖊多线程安全数据结构

#### 多线程安全容器类

Java 提供了一些容器类，能确保数据在多线程场景下的安全

1. Hashtable

2. ConcurrentHashMap

3. Vector

4. CopyOnWriteArrayList

5. StringBuffer

#### ✈ConcurrentHashMap 的并行原理

从下列代码中可以观察到，**读取是不加锁的**；而从关键代码

> synchronized (f)

可观察到**写入是只针对要读写的桶加锁**。

```java
/** Implementation for put and putIfAbsent */
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh; K fk; V fv;
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        else if (onlyIfAbsent // check first node without acquiring lock
                 && fh == hash
                 && ((fk = f.key) == key || (fk != null && key.equals(fk)))
                 && (fv = f.val) != null)
            return fv;
        else {
            V oldVal = null;
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key, value);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                    else if (f instanceof ReservationNode)
                        throw new IllegalStateException("Recursive update");
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}

public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;
        }
        else if (eh < 0)
            return (p = e.find(h, key)) != null ? p.val : null;
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

而下列是 **HashTable** 的写入操作代码，可以看出写入是针对整个写入操作加锁，这是低效的。

```java
public synchronized V put(K key, V value) {
    // Make sure the value is not null
    if (value == null) {
        throw new NullPointerException();
    }

    // Makes sure the key is not already in the hashtable.
    Entry<?,?> tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    @SuppressWarnings("unchecked")
    Entry<K,V> entry = (Entry<K,V>)tab[index];
    for(; entry != null ; entry = entry.next) {
        if ((entry.hash == hash) && entry.key.equals(key)) {
            V old = entry.value;
            entry.value = value;
            return old;
        }
    }

    addEntry(hash, key, value, index);
    return null;
}
```

这里有个问题，既然读是不加锁的，会不会出问题呢？其实不会，观察下列代码可知，ConcurrentHashMap 重写了 Node 封装类，使用 **volatile** 修饰 **val、next** ，这就保证了读写一致性。

```java
/* ---------------- Nodes -------------- */

/**
 * Key-value entry.  This class is never exported out as a
 * user-mutable Map.Entry (i.e., one supporting setValue; see
 * MapEntry below), but can be used for read-only traversals used
 * in bulk tasks.  Subclasses of Node with a negative hash field
 * are special, and contain null keys and values (but are never
 * exported).  Otherwise, keys and vals are never null.
 */
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K,V> next;

    Node(int hash, K key, V val) {
        this.hash = hash;
        this.key = key;
        this.val = val;
    }

    Node(int hash, K key, V val, Node<K,V> next) {
        this(hash, key, val);
        this.next = next;
    }

    public final K getKey()     { return key; }
    public final V getValue()   { return val; }
    public final int hashCode() { return key.hashCode() ^ val.hashCode(); }
    public final String toString() {
        return Helpers.mapEntryToString(key, val);
    }
    public final V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    public final boolean equals(Object o) {
        Object k, v, u; Map.Entry<?,?> e;
        return ((o instanceof Map.Entry) &&
                (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                (v = e.getValue()) != null &&
                (k == key || k.equals(key)) &&
                (v == (u = val) || v.equals(u)));
    }

    /**
     * Virtualized support for map.get(); overridden in subclasses.
     */
    Node<K,V> find(int h, Object k) {
        Node<K,V> e = this;
        if (k != null) {
            do {
                K ek;
                if (e.hash == h &&
                    ((ek = e.key) == k || (ek != null && k.equals(ek))))
                    return e;
            } while ((e = e.next) != null);
        }
        return null;
    }
}
```

## 🖊内存

### 📝garbage collect 机制

Java 内存回收机制包含静态内存回收机制与动态内存回收机制，前者在编译期即可确定，故回收策略较简单，分配的内存在**栈**上；后者较为复杂，内存一般在**堆**上分配，需要在运行时期确定内存回收时期。

#### 垃圾的检测

Java 是基于引用依赖关系来确定内存对象是否被视为垃圾的，在 Java 运行期，如果沿着以 **根对象集合** 为起点的引用链搜索，且最终无法被搜索到的内存对象将被视为垃圾。

**根对象集合 **主要包含下列元素：

- 在方法中局部变量区的对象的引用
- 在 Java 操作栈中的对象引用
- 在常量池的对象引用
- 在本地方法中持有的对象引用
- 类的 Class 对象

#### 基于分代的垃圾回收算法

*Hotspot* 主要使用的是基于分代的垃圾回收算法。

本算法的设计思路是：

> 按对象的寿命长短来分组，分为年轻代和年老代。新创建的对象被分为年轻代，如果对象经过几次回收后仍然存活，那么再把这个对象划分到年老代，且降低年老代的收集频率，以此减少每次垃圾收集所需要扫描的对象数量，从而提高垃圾回收效率。

故可以把堆分成若干个**子堆**，每个子堆对应一个年龄代。

> 1. Young 区分为： Eden 区 + Survivor 区。 而 Survivor 区 分为 1：1 的 From 区 和 To 区。
> 2. Old 区
> 3. Perm 区。此区主要存放类的 Class 对象。

**回收策略**如下：

> 1. 当 Eden 区满后触发 **Minor GC** ，将 Eden 区的存活对象复制到 Survivor 区的其中一个子分区，且将另一个子分区的对象也移到该分区中，保证 From 区 和 To 区 始终有一个为空。
> 2. 当 Survivor 区存不下 Minor gc 后仍存活的对象，将其移动到 Old 区；或者 Survivor 中足够老的对象，也移动到 Old 区
> 3. Old 区满后，触发 Full gc，回收整个堆的内存。
> 4. Perm 区 的垃圾回收也是由 Full gc 触发的。

## 🖊类加载器

#### 等级加载机制

类加载遵循以下责任顺序 ( **委派原则** )：ExtClassLoader ( *PlatformClassLoader* ) -> AppClassLoader -> 自定义 类加载器

对于 JVM 自身工作需要的类，这些加载的类一般不暴露给 Java 开发者，通过 Bootstrap ClassLoader 进行加载。值得注意的是，*Bootstrap ClassLoader* 没有子类，也不遵循委派原则。

#### 实现自定义 ClassLoader

以下情况一般需要实现自定义的 ClassLoader:

1. 加载自定义路径下的 class 类文件
2. 对要加载的类做特殊处理，如保证网络传输的类的安全性，可以加密后再传输，在加载到 JVM 前需要对字节码进行解密
3. 自定义类的实现机制。

以下为 ClassLoader 的加载类时关键函数的调用顺序：

> loadClass - > findClass -> defineClass ( final 方法 )

可通过修改 findClass 加载自定义路径下的类 (需要遵循委派原则)，手动调用 **defineClass** 根据字节流创建一个 Class 对象。

#### 线程上下文类加载器

可通过

> Thread.*currentThread*().getContextClassLoader()
>
> Thread.*currentThread*().setContextClassLoader(ClassLoader)

来获得/设置线程上下文类加载器。若无特别指定，默认值为 App ClassLoader

##### ✈SPI ( Service Provider Interface )

在SPI 的实现中，线程上下文类加载器 发挥着重要作用。

###### SPI 规范

> 1. 先编写好服务接口的实现类，即 服务提供类。
> 2. 然后在 classpath 的 **META-INF/services** 目录下创建一个以 **接口全限定名** 命名的 UTF-8 文本文件，并在该文件中写入**实现类的全限定名**（如果有多个实现类，以换行符分隔）
> 3. 最后调用 JDK 中的 java.util.ServiceLoader 组件中的 **load()** 方法，就会根据上述文件来发现并加载具体的服务实现。

###### SPI 实现原理

接下来以 Mysql JDBC 为例简介 SPI 的实现原理。

由 

> DriverManager 节

知 **getConnection** 函数将调用下列函数：

```java
/*
 * Load the initial JDBC drivers by checking the System property
 * jdbc.drivers and then use the {@code ServiceLoader} mechanism
 */
@SuppressWarnings("removal")
private static void ensureDriversInitialized() {
    if (driversInitialized) {
        return;
    }

    synchronized (lockForInitDrivers) {
        if (driversInitialized) {
            return;
        }
        String drivers;
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(JDBC_DRIVERS_PROPERTY);
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        // If the driver is packaged as a Service Provider, load it.
        // Get all the drivers through the classloader
        // exposed as a java.sql.Driver.class service.
        // ServiceLoader.load() replaces the sun.misc.Providers()

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                /* Load these drivers, so that they can be instantiated.
                 * It may be the case that the driver class may not be there
                 * i.e. there may be a packaged driver with the service class
                 * as implementation of java.sql.Driver but the actual class
                 * may be missing. In that case a java.util.ServiceConfigurationError
                 * will be thrown at runtime by the VM trying to locate
                 * and load the service.
                 *
                 * Adding a try catch block to catch those runtime errors
                 * if driver not available in classpath but it's
                 * packaged as service and that service is there in classpath.
                 */
                try {
                    while (driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch (Throwable t) {
                    // Do nothing
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);

        if (drivers != null && !drivers.isEmpty()) {
            String[] driversList = drivers.split(":");
            println("number of Drivers:" + driversList.length);
            for (String aDriver : driversList) {
                try {
                    println("DriverManager.Initialize: loading " + aDriver);
                    Class.forName(aDriver, true,
                            ClassLoader.getSystemClassLoader());
                } catch (Exception ex) {
                    println("DriverManager.Initialize: load failed: " + ex);
                }
            }
        }

        driversInitialized = true;
        println("JDBC DriverManager initialized");
    }
}
```
与 SPI 机制相关的核心代码为

```java
ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
Iterator<Driver> driversIterator = loadedDrivers.iterator();

try {
    while (driversIterator.hasNext()) {
        driversIterator.next();
    }
} catch (Throwable t) {
    // Do nothing
}
```

其中 load 方法返回一个 **ServiceLoader** 对象，其中属性值 指定了 要实现了服务接口类与 类加载器(线程上下文加载器)。

```java
/**
 * Creates a new service loader for the given service type, using the
 * current thread's {@linkplain java.lang.Thread#getContextClassLoader
 * context class loader}.
 *
 * <p> An invocation of this convenience method of the form
 * <pre>{@code
 *     ServiceLoader.load(service)
 * }</pre>
 *
 * is equivalent to
 *
 * <pre>{@code
 *     ServiceLoader.load(service, Thread.currentThread().getContextClassLoader())
 * }</pre>
 *
 * @apiNote Service loader objects obtained with this method should not be
 * cached VM-wide. For example, different applications in the same VM may
 * have different thread context class loaders. A lookup by one application
 * may locate a service provider that is only visible via its thread
 * context class loader and so is not suitable to be located by the other
 * application. Memory leaks can also arise. A thread local may be suited
 * to some applications.
 *
 * @param  <S> the class of the service type
 *
 * @param  service
 *         The interface or abstract class representing the service
 *
 * @return A new service loader
 *
 * @throws ServiceConfigurationError
 *         if the service type is not accessible to the caller or the
 *         caller is in an explicit module and its module descriptor does
 *         not declare that it uses {@code service}
 *
 * @revised 9
 */
@CallerSensitive
public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return new ServiceLoader<>(Reflection.getCallerClass(), service, cl);
}
```

```java
/**
 * Initializes a new instance of this class for locating service providers
 * via a class loader.
 *
 * @throws ServiceConfigurationError
 *         If {@code svc} is not accessible to {@code caller} or the caller
 *         module does not use the service type.
 */
@SuppressWarnings("removal")
private ServiceLoader(Class<?> caller, Class<S> svc, ClassLoader cl) {
    Objects.requireNonNull(svc);

    if (VM.isBooted()) {
        checkCaller(caller, svc);
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
    } else {

        // if we get here then it means that ServiceLoader is being used
        // before the VM initialization has completed. At this point then
        // only code in the java.base should be executing.
        Module callerModule = caller.getModule();
        Module base = Object.class.getModule();
        Module svcModule = svc.getModule();
        if (callerModule != base || svcModule != base) {
            fail(svc, "not accessible to " + callerModule + " during VM init");
        }

        // restricted to boot loader during startup
        cl = null;
    }

    this.service = svc;
    this.serviceName = svc.getName();
    this.layer = null;
    this.loader = cl;
    this.acc = (System.getSecurityManager() != null)
            ? AccessController.getContext()
            : null;
}
```

接下来的

> while (driversIterator.hasNext()) {
>         driversIterator.next();
> }

方法将进行多次迭代，其中 迭代方法 在 ServiceLoader 中被重写如下：

```java
public Iterator<S> iterator() {

        // create lookup iterator if needed
        if (lookupIterator1 == null) {
            lookupIterator1 = newLookupIterator();
        }

        return new Iterator<S>() {

            // record reload count
            final int expectedReloadCount = ServiceLoader.this.reloadCount;

            // index into the cached providers list
            int index;

            /**
             * Throws ConcurrentModificationException if the list of cached
             * providers has been cleared by reload.
             */
            private void checkReloadCount() {
                if (ServiceLoader.this.reloadCount != expectedReloadCount)
                    throw new ConcurrentModificationException();
            }

            @Override
            public boolean hasNext() {
                checkReloadCount();
                if (index < instantiatedProviders.size())
                    return true;
                return lookupIterator1.hasNext();
            }

            @Override
            public S next() {
                checkReloadCount();
                S next;
                if (index < instantiatedProviders.size()) {
                    next = instantiatedProviders.get(index);
                } else {
                    next = lookupIterator1.next().get();
                    instantiatedProviders.add(next);
                }
                index++;
                return next;
            }

        };
    }
```

从上述代码可以观察到，当

> instantiatedProviders

未缓存时，将通过 

> lookupIterator1

获取 ServiceLoader 实例，实例的 get 方法可创建 *服务类实例*

```java
// The lazy-lookup iterator for iterator operations
private Iterator<Provider<S>> lookupIterator1;
private final List<S> instantiatedProviders = new ArrayList<>();
```

```java
ProviderImpl(Class<S> service,
             Class<? extends S> type,
             Constructor<? extends S> ctor,
             @SuppressWarnings("removal") AccessControlContext acc) {
    this.service = service;
    this.type = type;
    this.factoryMethod = null;
    this.ctor = ctor;
    this.acc = acc;
}

@Override
public Class<? extends S> type() {
    return type;
}

@Override
public S get() {
    if (factoryMethod != null) {
        return invokeFactoryMethod();
    } else {
        return newInstance();
    }
}

/**
 * Invokes the provider's "provider" method to instantiate a provider.
 * When running with a security manager then the method runs with
 * permissions that are restricted by the security context of whatever
 * created this loader.
 */
@SuppressWarnings("removal")
private S invokeFactoryMethod() {
    Object result = null;
    Throwable exc = null;
    if (acc == null) {
        try {
            result = factoryMethod.invoke(null);
        } catch (Throwable x) {
            exc = x;
        }
    } else {
        PrivilegedExceptionAction<?> pa = new PrivilegedExceptionAction<>() {
            @Override
            public Object run() throws Exception {
                return factoryMethod.invoke(null);
            }
        };
        // invoke factory method with permissions restricted by acc
        try {
            result = AccessController.doPrivileged(pa, acc);
        } catch (Throwable x) {
            if (x instanceof PrivilegedActionException)
                x = x.getCause();
            exc = x;
        }
    }
    if (exc != null) {
        if (exc instanceof InvocationTargetException)
            exc = exc.getCause();
        fail(service, factoryMethod + " failed", exc);
    }
    if (result == null) {
        fail(service, factoryMethod + " returned null");
    }
    @SuppressWarnings("unchecked")
    S p = (S) result;
    return p;
}

/**
 * Invokes Constructor::newInstance to instantiate a provider. When running
 * with a security manager then the constructor runs with permissions that
 * are restricted by the security context of whatever created this loader.
 */
@SuppressWarnings("removal")
private S newInstance() {
    S p = null;
    Throwable exc = null;
    if (acc == null) {
        try {
            p = ctor.newInstance();
        } catch (Throwable x) {
            exc = x;
        }
    } else {
        PrivilegedExceptionAction<S> pa = new PrivilegedExceptionAction<>() {
            @Override
            public S run() throws Exception {
                return ctor.newInstance();
            }
        };
        // invoke constructor with permissions restricted by acc
        try {
            p = AccessController.doPrivileged(pa, acc);
        } catch (Throwable x) {
            if (x instanceof PrivilegedActionException)
                x = x.getCause();
            exc = x;
        }
    }
    if (exc != null) {
        if (exc instanceof InvocationTargetException)
            exc = exc.getCause();
        String cn = ctor.getDeclaringClass().getName();
        fail(service,
             "Provider " + cn + " could not be instantiated", exc);
    }
    return p;
}
```

当然，由于所有驱动都由 DriverManager 管理，故此 load 方法调用将遍历所有实现了

> java.sql.Driver

的第三方扩展类。

## 🖊网络

### 套接字 —— 传输层

| 协议 |            套接字            |
| :--: | :--------------------------: |
| TCP  |            Socket            |
| UDP  |        DatagramSocket        |
|  IP  | Java 标准库不支持 原始套接字 |

### Http 协议库 —— 应用层

|   端   |   实现类   |
| :----: | :--------: |
| 客户端 | HttpClient |
| 服务端 | HttpServer |

当然，一般 Web 程序采取 Servlet 规范，故可基于 Servlet 容器框架 (如 *Tomcat、Jetty* 等) 进行开发。

## 🖊数据库

### JDBC

JDBC 全称 Java Database Connectivity，是提供给 Java 程序 的数据库驱动，可采用其他语言编写。基于 JDBC，可以让 Java 程序开发 不需要过分关注数据库底层细节。

接下来以 *MySQL* 的 JDBC 驱动为例，分析 JDBC 对数据库连接的管理。

#### 初始化

当类被加载 (如通过 *反射机制* )时，驱动类的静态方法将被调用以完成驱动的初始化。

当然，在目前新版 JDBC 中，通过 SPI 机制完成了初始化，已经不需要通过反射手动初始化了。

```java
public class TestJDBC {
    @Test
    public void test() {
        String JDBC_Driver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(JDBC_Driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

```java
public class Driver extends com.mysql.cj.jdbc.Driver {
    public Driver() throws SQLException {
    }

    static {
        System.err.println("Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.");
    }
}

public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```

```java
/**
 * Registers the given driver with the {@code DriverManager}.
 * A newly-loaded driver class should call
 * the method {@code registerDriver} to make itself
 * known to the {@code DriverManager}. If the driver is currently
 * registered, no action is taken.
 *
 * @param driver the new JDBC Driver that is to be registered with the
 *               {@code DriverManager}
 * @throws SQLException if a database access error occurs
 * @throws NullPointerException if {@code driver} is null
 */
public static void registerDriver(java.sql.Driver driver)
    throws SQLException {

    registerDriver(driver, null);
}

/**
 * Registers the given driver with the {@code DriverManager}.
 * A newly-loaded driver class should call
 * the method {@code registerDriver} to make itself
 * known to the {@code DriverManager}. If the driver is currently
 * registered, no action is taken.
 *
 * @param driver the new JDBC Driver that is to be registered with the
 *               {@code DriverManager}
 * @param da     the {@code DriverAction} implementation to be used when
 *               {@code DriverManager#deregisterDriver} is called
 * @throws SQLException if a database access error occurs
 * @throws NullPointerException if {@code driver} is null
 * @since 1.8
 */
public static void registerDriver(java.sql.Driver driver,
        DriverAction da)
    throws SQLException {

    /* Register the driver if it has not already been added to our list */
    if (driver != null) {
        registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
    } else {
        // This is for compatibility with the original DriverManager
        throw new NullPointerException();
    }

    println("registerDriver: " + driver);

}
```

从上述代码可以看出，初始化的过程注册了相关驱动，并保存在 **DriverManager** 中，DriverManager 定义了一系列静态方法，用于**管理连接**。

#### DriverManager

```java
/**
 * The basic service for managing a set of JDBC drivers.
 * <p>
 * <strong>NOTE:</strong> The {@link javax.sql.DataSource} interface, provides
 * another way to connect to a data source.
 * The use of a {@code DataSource} object is the preferred means of
 * connecting to a data source.
 * <P>
 * As part of its initialization, the {@code DriverManager} class will
 * attempt to load available JDBC drivers by using:
 * <ul>
 * <li>The {@code jdbc.drivers} system property which contains a
 * colon separated list of fully qualified class names of JDBC drivers. Each
 * driver is loaded using the {@linkplain ClassLoader#getSystemClassLoader
 * system class loader}:
 * <ul>
 * <li>{@code jdbc.drivers=foo.bah.Driver:wombat.sql.Driver:bad.taste.ourDriver}
 * </ul>
 *
 * <li>Service providers of the {@code java.sql.Driver} class, that are loaded
 * via the {@linkplain ServiceLoader#load service-provider loading} mechanism.
 *</ul>
 *
 * @implNote
 * {@code DriverManager} initialization is done lazily and looks up service
 * providers using the thread context class loader.  The drivers loaded and
 * available to an application will depend on the thread context class loader of
 * the thread that triggers driver initialization by {@code DriverManager}.
 *
 * <P>When the method {@code getConnection} is called,
 * the {@code DriverManager} will attempt to
 * locate a suitable driver from amongst those loaded at
 * initialization and those loaded explicitly using the same class loader
 * as the current application.
 *
 * @see Driver
 * @see Connection
 * @since 1.1
 */
public class DriverManager {


    // List of registered JDBC drivers
    private static final CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
    private static volatile int loginTimeout = 0;
    private static volatile java.io.PrintWriter logWriter = null;
    private static volatile java.io.PrintStream logStream = null;
    // Used in println() to synchronize logWriter
    private static final Object logSync = new Object();
    // Used in ensureDriversInitialized() to synchronize driversInitialized
    private static final Object lockForInitDrivers = new Object();
    private static volatile boolean driversInitialized;
    private static final String JDBC_DRIVERS_PROPERTY = "jdbc.drivers";

    /* Prevent the DriverManager class from being instantiated. */
    private DriverManager(){}
    
    /**
     * The {@code SQLPermission} constant that allows the
     * setting of the logging stream.
     * @since 1.3
     */
    static final SQLPermission SET_LOG_PERMISSION =
        new SQLPermission("setLog");

    /**
     * The {@code SQLPermission} constant that allows the
     * un-register a registered JDBC driver.
     * @since 1.8
     */
    static final SQLPermission DEREGISTER_DRIVER_PERMISSION =
        new SQLPermission("deregisterDriver");
```

通过 DriverManager 可以获取 Connection，核心代码如下：

```java
//  Worker method called by the public getConnection() methods.
@CallerSensitiveAdapter
private static Connection getConnection(
    String url, java.util.Properties info, Class<?> caller) throws SQLException {
    /*
     * When callerCl is null, we should check the application's
     * (which is invoking this class indirectly)
     * classloader, so that the JDBC driver class outside rt.jar
     * can be loaded from here.
     */
    ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
    if (callerCL == null || callerCL == ClassLoader.getPlatformClassLoader()) {
        callerCL = Thread.currentThread().getContextClassLoader();
    }

    if (url == null) {
        throw new SQLException("The url cannot be null", "08001");
    }

    println("DriverManager.getConnection(\"" + url + "\")");

    ensureDriversInitialized();

    // Walk through the loaded registeredDrivers attempting to make a connection.
    // Remember the first exception that gets raised so we can reraise it.
    SQLException reason = null;

    for (DriverInfo aDriver : registeredDrivers) {
        // If the caller does not have permission to load the driver then
        // skip it.
        if (isDriverAllowed(aDriver.driver, callerCL)) {
            try {
                println("    trying " + aDriver.driver.getClass().getName());
                Connection con = aDriver.driver.connect(url, info);
                if (con != null) {
                    // Success!
                    println("getConnection returning " + aDriver.driver.getClass().getName());
                    return (con);
                }
            } catch (SQLException ex) {
                if (reason == null) {
                    reason = ex;
                }
            }

        } else {
            println("    skipping: " + aDriver.driver.getClass().getName());
        }

    }

    // if we got here nobody could connect.
    if (reason != null)    {
        println("getConnection failed: " + reason);
        throw reason;
    }

    println("getConnection: no suitable driver found for "+ url);
    throw new SQLException("No suitable driver found for "+ url, "08001");
}
```

可以观察到，上述代码利用了反射机制匹配合适的数据库驱动，同时使用了线程上下文 ClassLoader，这是实现 **SPI(Service Provider Interface)** 机制的重要手段，这是因为 数据库引导类 (如 **Connection**) 是定义在标准库的，而接口的实现由第三方实现，如果不使用线程上下文加载器，标准库开发者无法确定第三方实现者使用什么类加载器，而面向对象设计可能意味着标准库开发者定义的模板方法中需要引用实现类的实例对象，此时如果标准库代码与第三方库相关代码使用类加载器不同将会产生问题。

所以，使用上下文加载器就是为了使用同一个加载器来加载两者。第三方实现者可以将需要使用的加载器保存到线程上下文加载器，让标准库开发者使用该加载器来加载引导类。

#### 数据库操作

获取了一个

> Connection

对象后，便可利用该接口方法操作数据库了。

### 事务

### Sql 优化