/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.spi.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility to convert human readable datsize strings like '4567G', '128M'
 * to bytes. Note that this follows the convention of unix utilities like du and
 * ls with -h option.
 */
public class DataSize {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataSize.class);

  static final Pattern STORAGE_VAL_PATTERN = Pattern.compile("([\\d.]+)([TGMK])?$", Pattern.CASE_INSENSITIVE);
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

  static final Map<String, Long> MULTIPLIER;

  static {
    MULTIPLIER = new HashMap<>(4);
    MULTIPLIER.put("T", 1024L * 1024 * 1024 * 1024L);
    MULTIPLIER.put("G", 1024L * 1024 * 1024L);
    MULTIPLIER.put("M", 1024 * 1024L);
    MULTIPLIER.put("K", 1024L);
    MULTIPLIER.put("B", 1L);
  }

  /**
   * Convert human readable datasize to bytes
   * @param val string to parse
   * @return returns -1 in case of invalid value
   */
  public static long toBytes(@Nullable String val) {
    if (val == null) {
      return -1;
    }

    Matcher matcher = STORAGE_VAL_PATTERN.matcher(val);
    if (!matcher.matches()) {
      return -1;
    }
    String number = matcher.group(1);
    String unit = matcher.group(2);
    if (unit == null) {
      unit = "B";
    }
    long multiplier = MULTIPLIER.get(unit.toUpperCase());
    BigDecimal bytes = new BigDecimal(number);
    return bytes.multiply(BigDecimal.valueOf(multiplier)).longValue();
  }

  public static String fromBytes(long bytes) {
    if (bytes < MULTIPLIER.get("K")) {
      return String.valueOf(bytes) + "B";
    } else if (bytes < MULTIPLIER.get("M")) {
      return DECIMAL_FORMAT.format(((double) bytes) / MULTIPLIER.get("K")) + "KB";
    } else if (bytes < MULTIPLIER.get("G")) {
      return DECIMAL_FORMAT.format(((double) bytes) / MULTIPLIER.get("M")) + "MB";
    } else if (bytes < MULTIPLIER.get("T")) {
      return DECIMAL_FORMAT.format(((double) bytes) / MULTIPLIER.get("G")) + "GB";
    } else {
      return DECIMAL_FORMAT.format(((double) bytes) / MULTIPLIER.get("T")) + "TB";
    }
  }
}
