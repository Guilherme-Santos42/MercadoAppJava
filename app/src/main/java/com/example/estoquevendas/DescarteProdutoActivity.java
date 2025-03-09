package com.example.estoquevendas;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DescarteProdutoActivity extends AppCompatActivity {

    Spinner spinnerProdutosDescarte;
    EditText edtQuantidadeDescarte;
    Button btnDescarteProduto, btnVoltar;
    List<Produto> listaProdutos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarte_produto);

        spinnerProdutosDescarte = findViewById(R.id.spinnerProdutosDescarte);
        edtQuantidadeDescarte = findViewById(R.id.edtQuantidadeDescarte);
        btnDescarteProduto = findViewById(R.id.btnDescarteProduto);
        btnVoltar = findViewById(R.id.btnVoltar);

        carregarProdutosDaPlanilha();

        btnDescarteProduto.setOnClickListener(v -> descartarProduto());

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarProdutosDaPlanilha() {
        try {
            File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
            if (!file.exists()) {
                Toast.makeText(this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            List<String> nomesProdutos = new ArrayList<>();
            listaProdutos.clear();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null) {
                    String nomeProduto = row.getCell(0).getStringCellValue();
                    int quantidadeProduto = (int) row.getCell(1).getNumericCellValue();
                    double valorProduto = row.getCell(2).getNumericCellValue();

                    listaProdutos.add(new Produto(nomeProduto, quantidadeProduto, valorProduto));
                    nomesProdutos.add(nomeProduto);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomesProdutos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProdutosDescarte.setAdapter(adapter);

            fis.close();
        } catch (IOException | NumberFormatException e) {
            Toast.makeText(this, "Erro ao carregar produtos!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void descartarProduto() {
        String produtoSelecionado = spinnerProdutosDescarte.getSelectedItem().toString();
        String quantidadeStr = edtQuantidadeDescarte.getText().toString();

        if (quantidadeStr.isEmpty()) {
            Toast.makeText(this, "Digite a quantidade para descartar!", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantidadeDescarte = Integer.parseInt(quantidadeStr);

        for (Produto produto : listaProdutos) {
            if (produto.getNome().equals(produtoSelecionado)) {
                if (produto.getQuantidade() >= quantidadeDescarte) {
                    produto.setQuantidade(produto.getQuantidade() - quantidadeDescarte);
                    atualizarQuantidadeNoExcel(produto);
                    registrarDescarteNoExcel(produtoSelecionado, quantidadeDescarte);
                    Toast.makeText(this, "Produto descartado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Quantidade insuficiente para descarte!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void atualizarQuantidadeNoExcel(Produto produtoAtualizado) {
        try {
            File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
            if (!file.exists()) {
                Toast.makeText(this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getCell(0) != null) {
                    String nomeProduto = row.getCell(0).getStringCellValue();
                    if (nomeProduto.equals(produtoAtualizado.getNome())) {
                        row.getCell(1).setCellValue(produtoAtualizado.getQuantidade());
                        break;
                    }
                }
            }

            fis.close();

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao atualizar o estoque!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void registrarDescarteNoExcel(String nomeProduto, int quantidadeDescarte) {
        try {
            File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
            XSSFWorkbook workbook;
            Sheet sheet;

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("registro_descarte");
                if (sheet == null) {
                    sheet = workbook.createSheet("registro_descarte");
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("Produto");
                    header.createCell(1).setCellValue("Quantidade");
                    header.createCell(2).setCellValue("Data");
                    header.createCell(3).setCellValue("Hora");
                }
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("registro_descarte");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Produto");
                header.createCell(1).setCellValue("Quantidade");
                header.createCell(2).setCellValue("Data");
                header.createCell(3).setCellValue("Hora");
            }

            int lastRow = sheet.getLastRowNum() + 1;
            Row newRow = sheet.createRow(lastRow);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String dataAtual = dateFormat.format(new Date());
            String horaAtual = timeFormat.format(new Date());

            newRow.createCell(0).setCellValue(nomeProduto);
            newRow.createCell(1).setCellValue(quantidadeDescarte);
            newRow.createCell(2).setCellValue(dataAtual);
            newRow.createCell(3).setCellValue(horaAtual);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao registrar descarte!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
