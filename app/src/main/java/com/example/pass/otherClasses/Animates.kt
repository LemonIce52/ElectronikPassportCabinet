package com.example.pass.otherClasses

import android.view.View

class Animates {
    fun animatesButton(view: View, function: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()

                function()
            }
            .start()
    }

}