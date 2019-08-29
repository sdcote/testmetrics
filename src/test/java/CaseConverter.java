public class CaseConverter {

  /**
   * Capitalize first letter of every word to upper case assuming words are delimited with spaces.
   */
  String camelCase(String str,boolean removeWhitespace)
  {
    StringBuilder builder = new StringBuilder(str);
    boolean isLastSpace = true;
    for(int i = 0; i < builder.length(); i++)
    {
      char ch = builder.charAt(i);
      if(isLastSpace && ch >= 'a' && ch <='z')
      {
        builder.setCharAt(i, (char)(ch + ('A' - 'a') ));
        isLastSpace = false;
      }
      else if (ch != ' ')
        isLastSpace = false;
      else
        isLastSpace = true;
    }
    if( removeWhitespace) removeBlankSpace(builder);
    return builder.toString();
  }


  static void removeBlankSpace(StringBuilder sb) {
    int j = 0;
    for(int i = 0; i < sb.length(); i++) {
      if (!Character.isWhitespace(sb.charAt(i))) {
        sb.setCharAt(j++, sb.charAt(i));
      }
    }
    sb.delete(j, sb.length());
  }

  public static void main(String[] args) {
    CaseConverter converter = new CaseConverter();
    String str = converter.camelCase("This is the sample string - place me in camelcase.",true);
    System.out.println(str);
  }

}