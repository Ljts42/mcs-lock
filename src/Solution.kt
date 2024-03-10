import java.util.concurrent.atomic.AtomicReference

/**
 * @author Sentemov Lev
 */
class Solution(val env: Environment) : Lock<Solution.Node> {
    private val tail = AtomicReference<Node?>(null)

    override fun lock(): Node {
        val my = Node() // сделали узел
        my.locked.set(true)
        val pred = tail.getAndSet(my)
        if (pred != null) {
            pred.next.set(my)
            while (my.locked.get()) {
                env.park()
            }
        }
        return my // вернули узел
    }

    override fun unlock(node: Node) {
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) return
            while (node.next.get() == null) {
                continue
            }
        }
        val next = node.next.get()
        next!!.locked.set(false)
        env.unpark(next.thread)
    }

    class Node {
        val thread: Thread = Thread.currentThread() // запоминаем поток, которые создал узел
        val locked = AtomicReference<Boolean>(false)
        val next = AtomicReference<Node?>(null)
    }
}