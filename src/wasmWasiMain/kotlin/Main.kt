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
        val response = Types.OutgoingResponse(Types.Fields())
        val body = response.body().getOrThrow()

        val outgoingResult: Types.FutureIncomingResponse = createAndEvaluateOutgoingRequest(request)

        Types.ResponseOutparam.set(responseOut, Result.success(response))

        prepareAndReturnResponse(body, outgoingResult)

        Types.OutgoingBody.finish(body, null).getOrNull()
    }

    private fun prepareAndReturnResponse(body: Types.OutgoingBody, outgoingResult: Types.FutureIncomingResponse) {
        val out = body.write().getOrThrow()
        val requestResponse = outgoingResult.get()?.getOrNull()?.getOrNull()
        if (requestResponse != null) {
            val xxxBody: Types.IncomingBody = requestResponse.consume().getOrThrow()
            val stream: Streams.InputStream = xxxBody.stream().getOrThrow()
            var bytes: Result<ByteArray> = stream.blockingRead(4096.toULong())
            while (bytes.isSuccess) {
                out.blockingWriteAndFlush(bytes.getOrThrow())
                bytes = stream.blockingRead(4096.toULong())
            }
        }
        out.blockingFlush().getOrThrow()
        out.close()
    }

    private fun createAndEvaluateOutgoingRequest(request: Types.IncomingRequest): Types.FutureIncomingResponse {
        // create outgoing request and set auth, query etc.
        val outgoingRequest = Types.OutgoingRequest(Types.Fields()).apply {
            setPathWithQuery("/")
            setAuthority("lenta.ru")
        }
        val outgoingBody = outgoingRequest.body().getOrThrow()
        val outgoingStream = outgoingBody.write().getOrThrow()

        // read content of incoming request
        val requestBody = request.consume().getOrThrow()
        val requestStream = requestBody.stream().getOrThrow()

        // provide the content in outgoing request
        var bytes = requestStream.blockingRead(4096.toULong())
        while (bytes.isSuccess) {
            outgoingStream.blockingWriteAndFlush(bytes.getOrThrow())
            bytes = requestStream.blockingRead(4096.toULong())
        }
        outgoingStream.blockingFlush().getOrNull()
        outgoingStream.close()

        // get and handle response from outgoing request
        val outgoingResult: Types.FutureIncomingResponse = OutgoingHandler.handle(outgoingRequest, null).getOrThrow()
        Types.OutgoingBody.finish(outgoingBody, null).getOrNull()
        outgoingResult.subscribe().block()
        return outgoingResult
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