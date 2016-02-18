package org.roda.rodain.inspection;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

public class XMLEditor {

  private static final Pattern XML_TAG = Pattern
    .compile("(?<ELEMENT>(</?\\h*)(\\w+:)?(\\w+)([^<>]*)(\\h*/?>))" + "|(?<COMMENT><!--[^<>]+-->)");

  private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

  private static final int GROUP_OPEN_BRACKET = 2;
  private static final int GROUP_NAMESPACE = 3;
  private static final int GROUP_ELEMENT_NAME = 4;
  private static final int GROUP_ATTRIBUTES_SECTION = 5;
  private static final int GROUP_CLOSE_BRACKET = 6;
  private static final int GROUP_ATTRIBUTE_NAME = 1;
  private static final int GROUP_EQUAL_SYMBOL = 2;
  private static final int GROUP_ATTRIBUTE_VALUE = 3;

  public static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = XML_TAG.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {

      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      if (matcher.group("COMMENT") != null) {
        spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
      } else {
        if (matcher.group("ELEMENT") != null) {
          String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

          spansBuilder.add(Collections.singleton("tagmark"),
            matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
          if (matcher.start(GROUP_NAMESPACE) != -1 && matcher.end(GROUP_NAMESPACE) != -1) {
            spansBuilder.add(Collections.singleton("namespace"),
              matcher.end(GROUP_NAMESPACE) - matcher.end(GROUP_OPEN_BRACKET));
            spansBuilder.add(Collections.singleton("anytag"),
              matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_NAMESPACE));
          } else {
            spansBuilder.add(Collections.singleton("anytag"),
              matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));
          }

          if (!attributesText.isEmpty()) {

            lastKwEnd = 0;

            Matcher amatcher = ATTRIBUTES.matcher(attributesText);
            while (amatcher.find()) {
              spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
              spansBuilder.add(Collections.singleton("attribute"),
                amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
              spansBuilder.add(Collections.singleton("tagmark"),
                amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
              spansBuilder.add(Collections.singleton("avalue"),
                amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
              lastKwEnd = amatcher.end();
            }
            if (attributesText.length() > lastKwEnd)
              spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
          }

          lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

          spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
        }
      }
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }
}
