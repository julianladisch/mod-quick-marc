package org.folio.qm.util;

import static org.folio.qm.converter.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public final class MarcUtils {
  public static final BiMap<ParsedRecordDto.RecordTypeEnum, MarcFormat> TYPE_MAP = ImmutableBiMap.of(
    ParsedRecordDto.RecordTypeEnum.BIB, MarcFormat.BIBLIOGRAPHIC,
    ParsedRecordDto.RecordTypeEnum.AUTHORITY, MarcFormat.AUTHORITY,
    ParsedRecordDto.RecordTypeEnum.HOLDING, MarcFormat.HOLDINGS
  );
  private static final DateTimeFormatter DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S");
  private static final Pattern UUID_REGEX =
    Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private MarcUtils() {
  }

  /**
   * This method decode MARC date-time representation for.
   * Date and Time of Latest Transaction Field (005)
   * in the Java {@link LocalDateTime} value
   *
   * @param representation MARC date-time representation
   * @return Java {@link LocalDateTime} for MARC date-time value
   */
  public static LocalDateTime decodeFromMarcDateTime(String representation) {
    return LocalDateTime.parse(representation, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  public static QuickMarc updateRecordTimestamp(QuickMarc quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(
          new FieldItem().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
    return quickMarc;
  }

  /**
   * This method encode Java {@link java.time.LocalDateTime} value in the MARC date-time format for
   * <a href="https://www.loc.gov/marc/bibliographic/bd005.html">Date and Time of Latest Transaction Field (005)</a>.
   *
   * @param localDateTime {@link java.time.LocalDateTime} value
   * @return string with MARC date-time representation
   */
  public static String encodeToMarcDateTime(LocalDateTime localDateTime) {
    return localDateTime.format(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  public static Optional<FieldItem> getFieldByTag(QuickMarc quickMarc, String tag) {
    return quickMarc.getFields().stream()
      .filter(field -> tag.equals(field.getTag()))
      .findFirst();
  }

  public static String restoreBlanks(String sourceString) {
    return sourceString.replace('\\', ' ');
  }

  public static String masqueradeBlanks(String sourceString) {
    return sourceString.replace(' ', '\\');
  }

  /**
   * Returns string with provided length and replaced spaces by '\\'. Trims sourceString to length or append '\\'.
   */
  public static String normalizeFixedLengthString(String sourceString, int length) {
    if (length <= 0 || length == Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Length must be > 0 and < 2^31-1");
    }
    var source = masqueradeBlanks(StringUtils.trimToEmpty(sourceString));
    var sourceLength = source.length();
    if (sourceLength == length) {
      return source;
    } else if (sourceLength > length) {
      return source.substring(0, length);
    } else {
      return source + StringUtils.repeat('\\', length - sourceLength);
    }
  }

  public static boolean isValidUuid(String id) {
    return UUID_REGEX.matcher(id).matches();
  }
}
