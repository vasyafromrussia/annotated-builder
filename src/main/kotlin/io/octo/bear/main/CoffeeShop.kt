package io.octo.bear.main

fun main(args: Array<String>) {
    println("wanna some coffee?")

    val coffee = CoffeeBuilder
            .withBeans("brazil")
            .withMilk("soy")
            .withSyrup("caramel")
            .withSugar(false)
            .build()

    println("coffee: beans = ${coffee.beans}, milk = ${coffee.milk}, sugar = ${coffee.sugar},")

}