package de.service.impl;

import de.model.Trade;
import de.repository.TradeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TradesServiceImplTest {
    private TradeRepository tradeRepository;
    private TradesServiceImpl tradesService;

    @BeforeEach
    void setUp() {
        tradeRepository = Mockito.mock(TradeRepository.class);
        tradesService = new TradesServiceImpl(tradeRepository);
    }

    @Test
    void importTradesFromExcel_importsAndSkipsDuplicates() throws Exception {
        // Prepare existing trades
        List<Trade> existing = new ArrayList<>();
        Trade existingTrade = new Trade();
        existingTrade.setSymbol("AAPL");
        existingTrade.setBuyIn(100.0);
        //TODO: Re-add when date and time are back in Trade entity
//        existingTrade.setDatum(LocalDate.of(2025, 10, 15));
//        existingTrade.setUhrzeit(LocalTime.of(10, 0));
        existing.add(existingTrade);
        when(tradeRepository.findAll()).thenReturn(existing);

        // Prepare Excel file
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Symbol");
        header.createCell(1).setCellValue("BuyInKurs");
        header.createCell(2).setCellValue("TakeProfitKurs");
        header.createCell(3).setCellValue("Menge");
        header.createCell(4).setCellValue("Datum");
        header.createCell(5).setCellValue("Uhrzeit");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("AAPL");
        row1.createCell(1).setCellValue(100.0);
        row1.createCell(2).setCellValue(120.0);
        row1.createCell(3).setCellValue(10.0);
        row1.createCell(4).setCellValue(LocalDateTime.of(2025, 10, 15, 10, 0));
        row1.createCell(5).setCellValue(LocalDateTime.of(2025, 10, 15, 10, 0));

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("MSFT");
        row2.createCell(1).setCellValue(200.0);
        row2.createCell(2).setCellValue(250.0);
        row2.createCell(3).setCellValue(5.0);
        row2.createCell(4).setCellValue(LocalDateTime.of(2025, 10, 16, 11, 0));
        row2.createCell(5).setCellValue(LocalDateTime.of(2025, 10, 16, 11, 0));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        MockMultipartFile file = new MockMultipartFile("file", "trades.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());

        // Run import
        tradesService.importTradesFromExcel(file);

        // Verify only the new trade is saved
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository, times(1)).save(captor.capture());
        Trade saved = captor.getValue();
        assertThat(saved.getSymbol()).isEqualTo("MSFT");
        assertThat(saved.getBuyIn()).isEqualTo(200.0);

        //TODO: Re-add when date and time are back in Trade entity
//        assertThat(saved.getDatum()).isEqualTo(LocalDate.of(2025, 10, 16));
//        assertThat(saved.getUhrzeit()).isEqualTo(LocalTime.of(11, 0));
    }
}

