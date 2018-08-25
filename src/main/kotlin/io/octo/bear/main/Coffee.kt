package io.octo.bear.main

import io.octo.bear.annotation.Builder
import io.octo.bear.annotation.BuilderProperty

@Builder
class Coffee {

    @BuilderProperty internal lateinit var beans: Beans
    @BuilderProperty internal lateinit var milk: String
    @BuilderProperty internal lateinit var syrup: String
    @BuilderProperty internal var sugar: Boolean = false

    enum class Beans { ETHIOPIA, KENYA, BRAZIL }
}

