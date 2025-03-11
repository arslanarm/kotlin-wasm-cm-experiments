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

        return Result.success(Unit)
    }
}

object IncomingHandlerExportsImpl : IncomingHandlerExports {
    override fun handle(request: Types.IncomingRequest, responseOut: Types.ResponseOutparam) {

        val headers = Types.Fields()
        val message = "Hello World!"
//        headers.set("Content-Type", listOf(listOf("plain/text".toUByte())))
//        headers.set("Content-Length", listOf(listOf(message.length.toUByte())))

        var response = Types.OutgoingResponse(headers)

        // Add the HTTP Response Status Code
        response.setStatusCode(400.toUShort()).getOrNull()

        var body = response.body().getOrNull()
        Types.ResponseOutparam.set(responseOut, Result.success(response))

        val out = body!!.write()!!.getOrNull()
        out!!.blockingWriteAndFlush(message.toList()!!.map { ch -> ch.code.toUByte() })

        Types.OutgoingBody.finish(body!!, null).getOrNull()
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