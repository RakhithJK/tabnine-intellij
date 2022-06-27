package com.tabnine.plugin.completionPostProcess

import com.tabnine.binary.requests.autocomplete.postprocess
import org.junit.Test

class Tabs {
    @Test
    fun shouldReindentAndTrimCorrectlyWhereLastLineHasText() {
        val request = request("def a():\n\ti")
        val response = snippetResponse("if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }
    @Test
    fun shouldReindentAndTrimCorrectlyWhereIndentationIsReseeding() {
        val request = request("def a():\n\t")
        val response = snippetResponse("if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }

    @Test
    fun shouldReindentAndNotTrimWhereIndentationIsNotReseeding() {
        val request = request("def a():\n\t")
        val response = snippetResponse("if x > 2:\n\t\treturn x")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x")
    }

    @Test
    fun shouldReindentAndNotTrimWhereRequestIndentationIsZero() {
        val request = request("def a():\n")
        val response = snippetResponse("if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
    }

    @Test
    fun shouldReindentAndTrimWhereIndentationIsZeroOnTheFirstLine() {
        val request = request("def a():\n\t")
        val response = snippetResponse("\ndef b():\n\treturn 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "")
    }

    @Test
    fun shouldDoNothingWhereResponseIsOneLine() {
        val request = request("def a():\n\t")
        val newPrefix = "return 3"
        val response = snippetResponse(newPrefix)
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, newPrefix)
    }

    @Test
    fun shouldDoNothingIfResponseIsNotSnippet() {
        val request = request("def a():\n\t")
        val newPrefix = "if x > 2:\n\t\treturn x\n\t.return None\ndef b():\n\treturn 3"
        val response = nonSnippetResponse(newPrefix)
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, newPrefix)
    }

    @Test
    fun bracketsFixture() {
        val request = request("fn a() {\n\t")
        val response = snippetResponse("if a {\n\t\treturn true\n\t}\n\treturn false\n\t}\n}")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(
            response,
            "if a {\n    return true\n  }\n  return false\n  }"
        )
    }
}
