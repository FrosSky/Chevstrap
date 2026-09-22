package com.chevstrap.rbx.ui.viewModels

class MethodAction(
    private val action: () -> Unit
) {
    fun invoke() {
        action()
    }
}