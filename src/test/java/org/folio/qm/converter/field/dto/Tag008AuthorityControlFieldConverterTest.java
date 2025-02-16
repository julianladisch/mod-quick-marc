package org.folio.qm.converter.field.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.support.utils.testdata.Tag008FieldTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;

@UnitTest
class Tag008AuthorityControlFieldConverterTest {

  private final Tag008AuthorityControlFieldConverter converter = new Tag008AuthorityControlFieldConverter();

  private static Stream<Arguments> cannotProcessFields() {
    return Stream.of(
      arguments("007", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("006", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("035", "a".repeat(40), MarcFormat.AUTHORITY),
      arguments("008", "a".repeat(40), MarcFormat.HOLDINGS),
      arguments("008", "a".repeat(40), MarcFormat.BIBLIOGRAPHIC)
    );
  }

  @ParameterizedTest
  @EnumSource(value = Tag008FieldTestData.class,
              names = {"AUTHORITY", "AUTHORITY_WITH_GT_LEN", "AUTHORITY_WITH_LT_LEN"},
              mode = EnumSource.Mode.INCLUDE)
  void testConvertField(Tag008FieldTestData tag008FieldTestData) {
    var field = new ControlFieldImpl("008", tag008FieldTestData.getDtoData());
    var actualQmField = converter.convert(field, new LeaderImpl(tag008FieldTestData.getLeader()));
    assertEquals(tag008FieldTestData.getQmContent(), actualQmField.getContent());
  }

  @Test
  void testCanProcessField() {
    var field = new ControlFieldImpl("008", "a".repeat(40));
    assertTrue(converter.canProcess(field, MarcFormat.AUTHORITY));
  }

  @ParameterizedTest
  @MethodSource("cannotProcessFields")
  void testCannotProcessField(String tag, String content, MarcFormat format) {
    assertFalse(converter.canProcess(new ControlFieldImpl(tag, content), format));
  }
}
