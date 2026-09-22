package com.chevstrap.rbx.ui.viewModels

import kotlin.reflect.KMutableProperty1

class MethodPair<T, V>(
    private val instance: T,
    private val property: KMutableProperty1<T, V>
) {
    private val listeners = mutableListOf<(V) -> Unit>()

    fun get(): V {
        return property.get(instance)
    }

    fun set(value: V) {
        if (property.get(instance) == value) return

        property.set(instance, value)

        listeners.forEach { it(value) }
    }

    fun addListener(listener: (V) -> Unit) {
        listeners += listener
    }

    fun removeListener(listener: (V) -> Unit) {
        listeners -= listener
    }
}