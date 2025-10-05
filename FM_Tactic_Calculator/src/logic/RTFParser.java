// logic/RTFParser.java
package logic;

import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class RTFParser
{
	
	public String savedText = "";

    // Try RTF first; if not RTF or yields empty text, treat file as plain text.
    public static String parseRtfOrPlain(File file) throws IOException
    {
        byte[] bytes = Files.readAllBytes(file.toPath());

        // Quick sniff for true RTF
        String head = new String(bytes, 0, Math.min(bytes.length, 8), StandardCharsets.ISO_8859_1);
        boolean looksLikeRtf = head.startsWith("{\\rtf");

        if (looksLikeRtf)
        	{
            try (InputStream is = new ByteArrayInputStream(bytes))
            {
                RTFEditorKit kit = new RTFEditorKit();
                Document doc = kit.createDefaultDocument();
                kit.read(is, doc, 0);
                String text = doc.getText(0, doc.getLength());
                if (text != null && !text.isBlank()) return text;
            }
            catch (BadLocationException ignored)
            {
                // fall through to plain
            }
        }

        // Plain-text fallback (try UTF-8, then Windows-1252 if needed)
        String s = new String(bytes, StandardCharsets.UTF_8);
        if (s.isBlank()) s = new String(bytes, Charset.forName("windows-1252"));
        return s;
    }

    public static List<String> extractAlphaNumericTokens(String text)
    {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("[\\p{L}\\p{N}]+").matcher(text);
        while (m.find()) tokens.add(m.group());
        return tokens;
    }
    
    // keep letters, numbers, pipes, and line breaks; drop everything else
    public static String keepAlnumPipesAndLines(String text)
    {
        // \p{L} letters, \p{N} numbers, '|' pipes, \n\r line breaks
        return text.replaceAll("[^\\p{L}\\p{N}\\.\\|\\n\\r]+", "");
    }

	public String getSavedText()
	{
		return savedText;
	}
}
