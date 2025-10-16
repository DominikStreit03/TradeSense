package de.service.impl;

import de.model.trade.Trade;
import de.repository.TradeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        // Prepare existing trades (duplicate AAPL row)
        List<Trade> existing = new ArrayList<>();
        Trade existingTrade = new Trade();
        existingTrade.setSymbol("AAPL");
        existingTrade.setEntryPrice(100.0);
        existingTrade.setTimestamp(LocalDateTime.of(2025, 10, 15, 10, 0));
        existing.add(existingTrade);
        when(tradeRepository.findAll()).thenReturn(existing);

        // Prepare Excel file
        Workbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Symbol");
        header.createCell(1).setCellValue("EntryPrice");
        header.createCell(2).setCellValue("ExitPrice");
        header.createCell(3).setCellValue("Quantity");
        header.createCell(4).setCellValue("Timestamp");

        // Row 1: duplicate AAPL
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("AAPL");
        row1.createCell(1).setCellValue(100.0);
        row1.createCell(2).setCellValue(120.0);
        row1.createCell(3).setCellValue(10.0);
        Cell ts1 = row1.createCell(4);
        LocalDateTime ldt1 = LocalDateTime.of(2025, 10, 15, 10, 0);
        Date d1 = Date.from(ldt1.atZone(ZoneId.systemDefault()).toInstant());
        ts1.setCellValue(d1);
        CellStyle dateStyle = workbook.createCellStyle();
        short df = createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss");
        dateStyle.setDataFormat(df);
        ts1.setCellStyle(dateStyle);

        // Row 2: new MSFT
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("MSFT");
        row2.createCell(1).setCellValue(200.0);
        row2.createCell(2).setCellValue(250.0);
        row2.createCell(3).setCellValue(5.0);
        Cell ts2 = row2.createCell(4);
        LocalDateTime ldt2 = LocalDateTime.of(2025, 10, 16, 11, 0);
        Date d2 = Date.from(ldt2.atZone(ZoneId.systemDefault()).toInstant());
        ts2.setCellValue(d2);
        ts2.setCellStyle(dateStyle);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        MockMultipartFile file = new MockMultipartFile("file", "trades.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());

        // Run import
        tradesService.importTradesFromExcel(file);

        // ✅ Only the new trade should be saved
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository, times(1)).save(captor.capture());

        Trade saved = captor.getValue();
        assertThat(saved.getSymbol()).isEqualTo("MSFT");
        assertThat(saved.getEntryPrice()).isEqualTo(200.0);
        assertThat(saved.getProfitLoss()).isEqualTo((250.0 - 200.0) * 5.0);
    }


    @Test
    void importTradesFromCsv_importsAndSkipsDuplicates() throws Exception {
        // Prepare existing trades (duplicate AAPL)
        List<Trade> existing = new ArrayList<>();
        Trade existingTrade = new Trade();
        existingTrade.setSymbol("AAPL");
        existingTrade.setEntryPrice(100.0);
        existingTrade.setTimestamp(LocalDateTime.of(2025, 10, 15, 10, 0));
        existing.add(existingTrade);
        when(tradeRepository.findAll()).thenReturn(existing);

        // Prepare CSV content (header + duplicate AAPL + new MSFT)
        StringBuilder sb = new StringBuilder();
        sb.append("symbol,entryPrice,exitPrice,quantity,profitLoss,timestamp,tags,notes\n");
        sb.append("AAPL,100.0,120.0,10.0,,2025-10-15T10:00:00,\"tech;bluechip\",\"existing trade\"\n");
        sb.append("MSFT,200.0,250.0,5.0,,2025-10-16T11:00:00,\"tech\",\"new trade\"\n");

        MockMultipartFile file = new MockMultipartFile("file", "trades.csv", "text/csv", sb.toString().getBytes());

        // Run import
        tradesService.importTradesFromCsv(file);

        // Verify only the new trade is saved
        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository, times(1)).save(captor.capture());
        Trade saved = captor.getValue();
        assertThat(saved.getSymbol()).isEqualTo("MSFT");
        assertThat(saved.getEntryPrice()).isEqualTo(200.0);
        // profitLoss computed: (250 - 200) * 5 = 250.0
        assertThat(saved.getProfitLoss()).isEqualTo((250.0 - 200.0) * 5.0);
        assertThat(saved.getTags()).contains("tech");
        assertThat(saved.getNotes()).contains("new trade");
    }
}