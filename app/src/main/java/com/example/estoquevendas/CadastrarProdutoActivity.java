package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CadastrarProdutoActivity extends AppCompatActivity {

    AutoCompleteTextView edtNomeProduto;
    EditText edtQuantidade, edtValor;
    Button btnCadastrarProduto, btnVoltarMenu;
    List<String> listaProdutos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_produto);

        edtNomeProduto = findViewById(R.id.edtNomeProduto);
        edtQuantidade = findViewById(R.id.edtQuantidade);
        edtValor = findViewById(R.id.edtValor);
        btnCadastrarProduto = findViewById(R.id.btnCadastrarProduto);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);

        carregarNomesProdutos();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listaProdutos);
        edtNomeProduto.setAdapter(adapter);

        edtNomeProduto.setOnItemClickListener((parent, view, position, id) -> {
            String nomeSelecionado = parent.getItemAtPosition(position).toString();
            carregarDadosProduto(nomeSelecionado);
        });

        btnCadastrarProduto.setOnClickListener(v -> cadastrarProduto(adapter));

        btnVoltarMenu.setOnClickListener(v -> {
            Intent intent = new Intent(CadastrarProdutoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void carregarNomesProdutos() {
        File arquivoExcel = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
        if (!arquivoExcel.exists()) return;

        try (FileInputStream fis = new FileInputStream(arquivoExcel); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            listaProdutos.clear();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    listaProdutos.add(row.getCell(0).getStringCellValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarDadosProduto(String nomeProduto) {
        File arquivoExcel = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
        if (!arquivoExcel.exists()) return;

        try (FileInputStream fis = new FileInputStream(arquivoExcel); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().equalsIgnoreCase(nomeProduto)) {
                    edtQuantidade.setText(String.valueOf((int) row.getCell(1).getNumericCellValue()));
                    edtValor.setText(String.valueOf(row.getCell(2).getNumericCellValue()));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cadastrarProduto(ArrayAdapter<String> adapter) {
        String nomeProduto = edtNomeProduto.getText().toString();
        String quantidadeStr = edtQuantidade.getText().toString();
        String valorStr = edtValor.getText().toString();

        if (nomeProduto.isEmpty() || quantidadeStr.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantidade = Integer.parseInt(quantidadeStr);
            double valor = Double.parseDouble(valorStr);
            salvarProdutoEmPlanilha(nomeProduto, quantidade, valor);
            carregarNomesProdutos();
            adapter.notifyDataSetChanged();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Erro ao ler os valores.", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarProdutoEmPlanilha(String nomeProduto, int quantidade, double valor) {
        File pasta = new File(getExternalFilesDir(null), "estoquevendas");
        if (!pasta.exists()) pasta.mkdir();

        File arquivoExcel = new File(pasta, "produtos.xlsx");
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        boolean produtoExistente = false;

        try {
            if (arquivoExcel.exists()) {
                FileInputStream fis = new FileInputStream(arquivoExcel);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Produtos");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Nome Produto");
                header.createCell(1).setCellValue("Quantidade");
                header.createCell(2).setCellValue("Valor");
                header.createCell(3).setCellValue("Data e Hora de Cadastro");
            }

            sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().equalsIgnoreCase(nomeProduto)) {
                    row.getCell(1).setCellValue(quantidade);
                    row.getCell(2).setCellValue(valor);
                    row.getCell(3).setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                    produtoExistente = true;
                    break;
                }
            }

            if (!produtoExistente) {
                int novaLinha = sheet.getPhysicalNumberOfRows();
                Row novaRow = sheet.createRow(novaLinha);
                novaRow.createCell(0).setCellValue(nomeProduto);
                novaRow.createCell(1).setCellValue(quantidade);
                novaRow.createCell(2).setCellValue(valor);
                novaRow.createCell(3).setCellValue(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            }

            FileOutputStream fos = new FileOutputStream(arquivoExcel);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, produtoExistente ? "Produto atualizado!" : "Produto cadastrado!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao salvar produto.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
