package com.tabnine.plugin.completionPostProcess

import com.tabnine.binary.requests.autocomplete.postprocess
import org.junit.Test

class Spaces {
    @Test
    fun shouldReindentAndTrimCorrectlyWhereLastLineHasText() {
        val request = request("def a():\n  i")
        val response = snippetResponse("if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }
    @Test
    fun shouldTrimCorrectlyWhereIndentationIsReseeding() {
        val request = request("def a():\n  ")
        val response = snippetResponse("if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None")
    }

    @Test
    fun shouldNotTrimWhereIndentationIsNotReseeding() {
        val request = request("def a():\n\t")
        val response = snippetResponse("if x > 2:\n    return x")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x")
    }

    @Test
    fun shouldNotTrimWhereRequestIndentationIsZero() {
        val request = request("def a():\n")
        val response = snippetResponse("if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "if x > 2:\n    return x\n  .return None\ndef b():\n  return 3")
    }

    @Test
    fun shouldTrimWhereIndentationIsZeroOnTheFirstLine() {
        val request = request("def a():\n  ")
        val response = snippetResponse("\ndef b():\n  return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "")
    }

    @Test
    fun shouldDoNothingWhereResponseIsOneLine() {
        val request = request("def a():\n  ")
        val response = snippetResponse("return 3")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, "return 3")
    }

    @Test
    fun shouldDoNothingIfResponseIsNotSnippet() {
        val request = request("def a():\n  ")
        val newPrefix = "if x > 2:\n    return x\n  .return None\ndef b():\n  return 3"
        val response = nonSnippetResponse(newPrefix)
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(response, newPrefix)
    }

    @Test
    fun bracketsFixture() {
        val request = request("fn a() {\n  ")
        val response = snippetResponse("if a {\n    return true\n  }\n  return false\n  }\n}")
        postprocess(request, response, TAB_SIZE)

        assertNewPrefix(
            response,
            "if a {\n    return true\n  }\n  return false\n  }"
        )
    }
}
