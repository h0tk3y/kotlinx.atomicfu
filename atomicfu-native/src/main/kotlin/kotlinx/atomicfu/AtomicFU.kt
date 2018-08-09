/*
 * Copyright 2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE", "RedundantVisibilityModifier", "CanBePrimaryConstructorProperty")

package kotlinx.atomicfu

import konan.worker.AtomicInt as KAtomicInt
import konan.worker.AtomicLong as KAtomicLong
import konan.worker.AtomicReference as KAtomicRef
import konan.worker.freeze

public actual fun <T> atomic(initial: T): AtomicRef<T> = AtomicRef<T>(KAtomicRef(initial.freeze()))
public actual fun atomic(initial: Int): AtomicInt = AtomicInt(KAtomicInt(initial))
public actual fun atomic(initial: Long): AtomicLong = AtomicLong(KAtomicLong(initial))
public actual fun atomic(initial: Boolean): AtomicBoolean = AtomicBoolean(KAtomicInt(if (initial) 1 else 0))

// ==================================== AtomicRef ====================================

// todo: make it inline class
public actual class AtomicRef<T> internal constructor(private val a: KAtomicRef<T>) {
    public actual var value: T
        get() = a.get() as T
        set(value) {
            value.freeze()
            while (true) {
                val cur = a.get()
                if (cur === value) break
                if (a.compareAndSwap(cur, value) === cur) break
            }
        }

    public actual inline fun lazySet(value: T) { this.value = value }

    public actual fun compareAndSet(expect: T, update: T): Boolean {
        update.freeze()
        while (true) {
            val cur = a.get()
            if (cur !== expect) return false
            if (a.compareAndSwap(cur, update) === cur) return true
        }
    }

    public actual fun getAndSet(value: T): T {
        value.freeze()
        while (true) {
            val cur = a.get() as T
            if (cur === value) return cur
            if (a.compareAndSwap(cur, value) === cur) return cur
        }
    }

    override fun toString(): String = value.toString()
}

// ==================================== AtomicBoolean ====================================

public actual class AtomicBoolean internal constructor(private val a: KAtomicInt) {
    public actual var value: Boolean
        get() = a.get() != 0
        set(value) {
            val iValue = if (value) 1 else 0
            while (true) {
                val cur = a.get()
                if (cur == iValue) return
                if (a.compareAndSwap(cur, iValue) == cur) return
            }
        }

    public actual fun lazySet(value: Boolean) { this.value = value }

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        val iExpect = if (expect) 1 else 0
        val iUpdate = if (update) 1 else 0
        while (true) {
            val cur = a.get()
            if (cur != iExpect) return false
            if (a.compareAndSwap(cur, iUpdate) == cur) return true
        }
    }

    public actual fun getAndSet(value: Boolean): Boolean {
        val iValue = if (value) 1 else 0
        while (true) {
            val cur = a.get()
            if (cur == iValue) return value
            if (a.compareAndSwap(cur, iValue) == cur) return cur != 0
        }
    }

    override fun toString(): String = value.toString()
}

// ==================================== AtomicInt ====================================

public actual class AtomicInt internal constructor(private val a: KAtomicInt) {
    public actual var value: Int
        get() = a.get()
        set(value) {
            while (true) {
                val cur = a.get()
                if (cur == value) return
                if (a.compareAndSwap(cur, value) == cur) return
            }
        }

    public actual inline fun lazySet(value: Int) { this.value = value }

    public actual fun compareAndSet(expect: Int, update: Int): Boolean {
        while (true) {
            val cur = a.get()
            if (cur != expect) return false
            if (a.compareAndSwap(cur, update) == cur) return true
        }
    }

    public actual fun getAndSet(value: Int): Int {
        while (true) {
            val cur = a.get()
            if (cur == value) return cur
            if (a.compareAndSwap(cur, value) == cur) return cur
        }
    }

    public actual fun getAndIncrement(): Int = a.addAndGet(1) - 1
    public actual fun getAndDecrement(): Int = a.addAndGet(-1) + 1
    public actual fun getAndAdd(delta: Int): Int = a.addAndGet(delta) - delta
    public actual fun addAndGet(delta: Int): Int = a.addAndGet(delta)
    public actual fun incrementAndGet(): Int = a.addAndGet(1)
    public actual fun decrementAndGet(): Int = a.addAndGet(-1)

    public actual inline operator fun plusAssign(delta: Int) { getAndAdd(delta) }
    public actual inline operator fun minusAssign(delta: Int) { getAndAdd(-delta) }

    override fun toString(): String = value.toString()
}

// ==================================== AtomicLong ====================================

public actual class AtomicLong internal constructor(private val a: KAtomicLong) {
    public actual var value: Long
        get() = a.get()
        set(value) {
            while (true) {
                val cur = a.get()
                if (cur == value) return
                if (a.compareAndSwap(cur, value) == cur) return
            }
        }

    public actual inline fun lazySet(value: Long) { this.value = value }

    public actual fun compareAndSet(expect: Long, update: Long): Boolean {
        while (true) {
            val cur = a.get()
            if (cur != expect) return false
            if (a.compareAndSwap(cur, update) == cur) return true
        }
    }

    public actual fun getAndSet(value: Long): Long {
        while (true) {
            val cur = a.get()
            if (cur == value) return cur
            if (a.compareAndSwap(cur, value) == cur) return cur
        }
    }

    public actual fun getAndIncrement(): Long = a.addAndGet(1) - 1
    public actual fun getAndDecrement(): Long = a.addAndGet(-1) + 1
    public actual fun getAndAdd(delta: Long): Long = a.addAndGet(delta) - delta
    public actual fun addAndGet(delta: Long): Long = a.addAndGet(delta)
    public actual fun incrementAndGet(): Long = a.addAndGet(1)
    public actual fun decrementAndGet(): Long = a.addAndGet(-1)

    public actual inline operator fun plusAssign(delta: Long) { getAndAdd(delta) }
    public actual inline operator fun minusAssign(delta: Long) { getAndAdd(-delta) }

    override fun toString(): String = value.toString()
}
