package com.suming.plugin.utils;

import com.suming.plugin.bean.BasePropType;
import com.suming.plugin.bean.PropTypeBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropTypesHelper {
  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  public static <T> Comparator<T> sortByKey(Set<Integer> reverseSet, Function<? super T, ?>... keyExtractors) {
    return (o1, o2) -> {
      for (int i = 0; i < keyExtractors.length; i++) {
        Function<? super T, ?> keyExtractor = keyExtractors[i];
        String value1 = keyExtractor.apply(o1) == null ? "" : keyExtractor.apply(o1).toString();
        String value2 = keyExtractor.apply(o2) == null ? "" : keyExtractor.apply(o2).toString();
        int result = reverseSet.contains(i) ? value2.compareTo(value1) : value1.compareTo(value2);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    };
  }

  public static boolean isBool(String str) {
    Pattern pattern = Pattern.compile("true|false");
    Matcher isNum = pattern.matcher(str);
    if (!isNum.matches()) {
      return false;
    }
    return true;
  }

  public static boolean isNumeric(String str) {
    Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
    Matcher isNum = pattern.matcher(str);
    if (!isNum.matches()) {
      return false;
    }
    return true;
  }

  public static String getPropTypeByValue(String value) {
    String type = "any";
    if (value.startsWith("\"") && value.endsWith("\"")) {
      type = "string";
    } else if (value.startsWith("\'") && value.endsWith("\'")) {
      type = "string";
    } else if (value.startsWith("{") && value.endsWith("}")) {
      type = "object";
    } else if (value.startsWith("[") && value.endsWith("]")) {
      type = "array";
    } else if (PropTypesHelper.isBool(value)) {
      type = "bool";
    } else if (PropTypesHelper.isNumeric(value)) {
      type = "number";
    }
    return type;
  }

  public static void updatePropTypeFromCode(PropTypeBean bean, String codeStr) {
    // 去除换行符
    codeStr = codeStr.replaceAll("\n", "");
    Pattern p = Pattern.compile("(React)?\\s*\\.?\\s*PropTypes\\s*\\." +
            "\\s*(any|string|object|bool|func|number|array|symbol|shape)" +
            "\\s*(\\(\\{(.*)}\\))?" +
            "\\s*\\.?\\s*(isRequired)?");
    Matcher m = p.matcher(codeStr);
    if (m.matches()) {
      bean.setType(m.group(2) == null ? "any" : m.group(2));
      bean.setShapePropTypeList(matchShapePropType(m.group(4)));
      bean.setRequired(m.group(5) != null);
    }
  }

  private static List<BasePropType> matchShapePropType(String codeStr) {
    if (codeStr == null) return null;
    List<BasePropType> shapePropList = new ArrayList<>();
    String[] propsCode = codeStr.trim().split(",");
    for (String childCode : propsCode) {
      Pattern p = Pattern.compile("\\s*(.*):\\s*" + "(React)?\\s*\\.?\\s*PropTypes\\s*\\." +
              "\\s*(any|string|object|bool|func|number|array|symbol)" +
              "\\s*\\.?\\s*(isRequired)?");
      Matcher m = p.matcher(childCode);
      if (m.matches() && m.group(1) != null) {
        shapePropList.add(new BasePropType(m.group(1), m.group(3) == null ? "any" : m.group(3),
                m.group(4) != null));

      }
    }
    return shapePropList;
  }

  public static String getBlank(int count) {
    StringBuilder st = new StringBuilder();
    if (count < 0) {
      count = 0;
    }
    for (int i = 0; i < count; i++) {
      st.append(" ");
    }
    return st.toString();
  }
}
