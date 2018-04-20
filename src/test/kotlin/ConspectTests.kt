
/**
 * Created by fresheed on 17.04.18.
 */

open class ConspectTests {

    protected fun parseContent(content: String) = getParserBuilder().setInput(content).build().parse();

    protected fun parseNote(content: String) = parseContent(content).notes[0]

}