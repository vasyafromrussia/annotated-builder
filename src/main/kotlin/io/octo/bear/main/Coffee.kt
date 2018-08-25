package io.octo.bear.main

import io.octo.bear.annotation.Builder
import io.octo.bear.annotation.BuilderProperty

@Builder
class Coffee {

    @BuilderProperty lateinit var beans: String
    @BuilderProperty lateinit var milk: String
    @BuilderProperty lateinit var syrup: String
    @BuilderProperty var sugar: Boolean = false

}

