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
        val response = Types.OutgoingResponse(headers)
        val body = response.body().getOrNull()!!

        // println("IncomingHandlerExportsImpl_01")
        val outgoingRequest = Types.OutgoingRequest(Types.Fields())
        outgoingRequest.setPathWithQuery("/").getOrNull()!!
        outgoingRequest.setAuthority("localhost:32768").getOrNull()!!
        val outgoingBody = outgoingRequest.body().getOrNull()!!
        val outgoingStream = outgoingBody.write().getOrNull()!!

        // println("IncomingHandlerExportsImpl_02")
        val requestBody = request.consume().getOrNull()!!
        // println("IncomingHandlerExportsImpl_021")
        val requestStream = requestBody.stream().getOrNull()!!
        // println("IncomingHandlerExportsImpl_022")
        // println("IncomingHandlerExportsImpl_023")
//        outgoingStream.blockingWriteAndFlush(bytes.getOrNull()!!)
        // println("IncomingHandlerExportsImpl_03")

        var bytes = requestStream.blockingRead(16.toULong())
//        // println("bytes: ${(bytes.exceptionOrNull() as ComponentException).value}")
        while (bytes.isSuccess) {
            outgoingStream.blockingWriteAndFlush(bytes.getOrNull()!!)
            bytes = requestStream.blockingRead(16.toULong())
            // println("bytes: ${bytes.toString()}")
        }
        outgoingStream.blockingFlush().getOrNull()
        outgoingStream.close()
        // println("IncomingHandlerExportsImpl_04")

        val outgoingResult = OutgoingHandler.handle(outgoingRequest, null).getOrNull()!!
        Types.OutgoingBody.finish(outgoingBody, null).getOrNull()
        outgoingResult.subscribe().block()
        // println("IncomingHandlerExportsImpl_05")

        Types.ResponseOutparam.set(responseOut, Result.success(response))
        val out = body.write().getOrNull()!!
        val requestResponse = outgoingResult.get()?.getOrNull()?.getOrNull()
        if (requestResponse != null) {
            val xxxBody: Types.IncomingBody = requestResponse.consume().getOrNull()!!
            val stream: Streams.InputStream = xxxBody.stream().getOrNull()!!
            // println("IncomingHandlerExportsImpl_051")
            var bytes: Result<ByteArray> = stream.blockingRead(64.toULong())
            // println("IncomingHandlerExportsImpl_052")
//                out.blockingWriteAndFlush(bytes.getOrNull()!!)
            while (bytes.isSuccess) {
                // println("IncomingHandlerExportsImpl_053")
//                // println("bytes: ${bytes.getOrNull()!!}")
                out.blockingWriteAndFlush(bytes.getOrNull()!!)
                // println("IncomingHandlerExportsImpl_054")
                bytes = stream.blockingRead(64.toULong())
                // println("IncomingHandlerExportsImpl_055")
            }
        }
        // println("IncomingHandlerExportsImpl_06")
        out.blockingFlush().getOrNull()!!
        out.close()
        Types.OutgoingBody.finish(body, null).getOrNull()
        // println("IncomingHandlerExportsImpl_07")
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