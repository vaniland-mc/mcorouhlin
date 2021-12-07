package land.vani.plugin.mcorouhlin.util

class ObservableList<T>(
    private val inner: MutableList<T>,
    private val onChange: (MutableList<T>) -> Unit,
) : MutableList<T> by inner {
    override fun add(element: T): Boolean {
        return inner.add(element).also {
            onChange(this)
        }
    }

    override fun add(index: Int, element: T) {
        inner.add(index, element).also {
            onChange(this)
        }
    }

    override fun remove(element: T): Boolean {
        return inner.remove(element).also {
            onChange(this)
        }
    }

    override fun removeAt(index: Int): T {
        return inner.removeAt(index).also {
            onChange(this)
        }
    }
}
