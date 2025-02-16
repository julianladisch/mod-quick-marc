package org.folio.qm.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapperImpl;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcQmConverterTest {

  private MarcQmEditConverter converter;
  private MarcFieldsConverter fieldsConverter;

  @BeforeEach
  void setUp() {
    fieldsConverter = mock(MarcFieldsConverter.class);
    converter = new MarcQmEditConverter(new MarcFactoryImpl(), new ObjectMapper(), new MarcTypeMapperImpl(),
      fieldsConverter);
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_AUTHORITY_DTO_PATH + "," + QM_RECORD_EDIT_AUTHORITY_PATH + "," + "01725cz  a2200433n  4500",
    PARSED_RECORD_HOLDINGS_DTO_PATH + "," + QM_RECORD_EDIT_HOLDINGS_PATH + "," + "01717cx  a2200433zn 4500",
    PARSED_RECORD_BIB_DTO_PATH + "," + QM_RECORD_EDIT_BIB_PATH + "," + "01750ccm a2200421   4500"
  })
  void testConvertDtoRecord(String parsedRecordDtoPath, String quickMarcJsonPath, String expectedLeader) {
    var expected = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    var qmRecord = getMockAsObject(quickMarcJsonPath, QuickMarcEdit.class);
    when(fieldsConverter.convertQmFields(any(), any())).thenReturn(extractMarcRecord(expected.getParsedRecord())
      .getVariableFields());

    ParsedRecordDto actual = converter.convert(qmRecord);

    assertThat(actual)
      .isNotNull()
      .hasFieldOrPropertyWithValue("id", expected.getId())
      .hasFieldOrPropertyWithValue("recordType", expected.getRecordType())
      .hasFieldOrPropertyWithValue("relatedRecordVersion", "1")
      .hasFieldOrPropertyWithValue("parsedRecord.id", expected.getParsedRecord().getId())
      .hasFieldOrPropertyWithValue("additionalInfo.suppressDiscovery",
        expected.getAdditionalInfo().getSuppressDiscovery())
      .extracting(parsedRecordDto -> parsedRecordDto.getParsedRecord().getContent().toString())
      .matches(content -> content.contains(expectedLeader), "contains valid leader");
  }

  private Record extractMarcRecord(ParsedRecord parsedRecord) {
    try (var input = IOUtils.toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), UTF_8)) {
      return new MarcJsonReader(input).next();
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

}
