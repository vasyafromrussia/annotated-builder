# annotated-builder

Step-by-step annotation-based builder generator.

These annotations allow you to generate builders for your classes, which has only one property to set on each step.
Properties are set in order they were declared.

For example, to create builder like this:
```
val coffee = CoffeeBuilder
            .withBeans("brazil")
            .withMilk("soy")
            .withSyrup("caramel")
            .withSugar(false)
            .build()
```

Write class like this:
```
@Builder
class Coffee {

    @BuilderProperty lateinit var beans: String
    @BuilderProperty lateinit var milk: String
    @BuilderProperty lateinit var syrup: String
    @BuilderProperty var sugar: Boolean = false

}
```
Then run build, and builder like this will be generated:
```
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
```
