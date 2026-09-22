package `in`.androidengineers.cookbook

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.models.Model
import com.google.adk.kt.models.LlmRequest
import com.google.adk.kt.models.LlmResponse
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.tools.BaseTool
import com.google.adk.kt.tools.ToolContext
import com.google.adk.kt.types.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout

/** A single exact approval is supplied by the app, never by model-generated arguments. */
class ApprovalGate(private val approvedMinutes: Int?) {
    var writes: Int = 0
        private set
    fun execute(args: Map<String, Any?>): Map<String, Any> {
        if (args.keys != setOf("minutes")) return mapOf("status" to "invalid_arguments")
        val number = args["minutes"] as? Number ?: return mapOf("status" to "invalid_arguments")
        val value = number.toDouble()
        if (!value.isFinite() || value != value.toInt().toDouble() || value !in 1.0..120.0)
            return mapOf("status" to "invalid_arguments")
        if (approvedMinutes != value.toInt()) return mapOf("status" to "approval_required")
        if (writes != 0) return mapOf("status" to "already_used")
        writes++
        return mapOf("status" to "recorded_in_memory", "minutes" to value.toInt())
    }
}

class PracticeTool(private val gate: ApprovalGate) : BaseTool(
    name = "record_practice", description = "Record approved practice minutes in this demo's memory only."
) {
    override fun declaration() = FunctionDeclaration(
        name = name, description = description,
        parameters = Schema(type = Type.OBJECT, properties = mapOf("minutes" to Schema(type = Type.INTEGER)), required = listOf("minutes"))
    )
    override suspend fun run(context: ToolContext, args: Map<String, Any>): Any = gate.execute(args)
}

/** Deterministic ADK Model implementation. This is a simulation, not Gemini inference. */
class ScriptedToolModel(private val minutes: Int, private val repeatForever: Boolean = false) : Model {
    override val name = "cookbook-scripted-tool-model"
    private var calls = 0
    override fun generateContent(request: LlmRequest, stream: Boolean): Flow<LlmResponse> = flow {
        check(++calls <= 3) { "Model call budget exceeded" }
        val response = request.contents.flatMap { it.parts }.lastOrNull { it.functionResponse != null }
        if (response == null || repeatForever) {
            emit(LlmResponse(content = Content(role = Role.MODEL, parts = listOf(
                Part(functionCall = FunctionCall(name = "record_practice", args = mapOf("minutes" to minutes), id = "practice-call"))
            ))))
        } else {
            emit(LlmResponse(content = Content(role = Role.MODEL, parts = listOf(Part(text = "Fixture tool result: ${response.functionResponse?.response}")))))
        }
    }
}

data class AgentResult(val transcript: String, val writes: Int)

suspend fun runSafeAgent(approvedMinutes: Int?, proposedMinutes: Int = 25, repeatForever: Boolean = false): AgentResult {
    val gate = ApprovalGate(approvedMinutes)
    val agent = LlmAgent(
        name = "practice_coach",
        model = ScriptedToolModel(proposedMinutes, repeatForever),
        instruction = Instruction("Record practice only through the provided tool. The tool enforces approval."),
        tools = listOf(PracticeTool(gate)),
    )
    val runner = InMemoryRunner(agent = agent)
    val events = withTimeout(5000) {
        runner.runAsync(
            userId = "fixture-user", sessionId = "fixture-session",
            newMessage = Content(role = Role.USER, parts = listOf(Part(text = "Record 25 minutes of practice"))),
        ).toList()
    }
    return AgentResult(events.flatMap { it.content?.parts.orEmpty() }.mapNotNull { it.text }.joinToString("\n"), gate.writes)
}
