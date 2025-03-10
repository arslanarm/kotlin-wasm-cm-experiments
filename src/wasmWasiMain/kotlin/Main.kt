import Ui.Color
import Ui.Modifiers
import Ui.Rgb
import Ui.Shape
import Ui.createButton
import Ui.createHtmlTextArea

object RunExportsImpl : RunExports {
    override fun run() : Result<Unit> {
        createButton(
            label = "example",
            shape = Shape.ROUNDED,
            color = Color.Rgb(Rgb(r = 177u, g = 37u, b = 234u)),
            modifiers = Modifiers(
                outlined = true,
                shadow = false,
            )
        )

        val markdown = """
            # Kotlin/Wasm and Component Model
            - [Learn more about Kotlin/Wasm](https://kotl.in/wasm)
            - [Learn more about Wasm Component Model](https://component-model.bytecodealliance.org/introduction.html)
        """.trimIndent()

        val html = Markdown.convertMarkdownToHtml(markdown)

        createHtmlTextArea(html)

//        val initialCwd = Environment.initialCwd()
//        println(initialCwd)
        return Result.success(Unit)
    }
}

object IncomingHandlerExportsImpl : IncomingHandlerExports {
    override fun handle(request: Types.IncomingRequest, responseOut: Types.ResponseOutparam) {

    }

}

object EnvironmentExportsImpl : EnvironmentExports {
    override fun getEnvironment(): List<Pair<String, String>> {
        return emptyList<Pair<String, String>>()
    }

    override fun getArguments(): List<String> {
        return emptyList<String>()
    }

    override fun initialCwd(): String? {
        return ""
    }
}