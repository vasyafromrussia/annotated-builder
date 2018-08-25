package io.octo.bear.main

import kotlin.String
import kotlin.Boolean

object CoffeeBuilder {
    fun withBeans(beans: String): CoffeeBuilderMilkStep = CoffeeBuilderMilkStep(Coffee().apply {
        this.beans = beans
    })

    class CoffeeBuilderMilkStep(private val coffee: Coffee) {
        fun withMilk(milk: String): CoffeeBuilderSyrupStep = CoffeeBuilderSyrupStep(coffee.apply {
            this.milk = milk
        })
    }

    class CoffeeBuilderSyrupStep(private val coffee: Coffee) {
        fun withSyrup(syrup: String): CoffeeBuilderSugarStep = CoffeeBuilderSugarStep(coffee.apply {
            this.syrup = syrup
        })
    }

    class CoffeeBuilderSugarStep(private val coffee: Coffee) {
        fun withSugar(sugar: Boolean): CoffeeBuilderFinal = CoffeeBuilderFinal(coffee.apply {
            this.sugar = sugar
        })
    }

    class CoffeeBuilderFinal(private val coffee: Coffee) {
        fun build(): Coffee = coffee
    }
}
