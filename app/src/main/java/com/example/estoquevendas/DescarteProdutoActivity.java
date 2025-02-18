package com.example.estoquevendas;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                if (row.getRowNum() == 0) continue; // Pular cabeçalho

                if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null) {
                    String nomeProduto = row.getCell(0).getStringCellValue();

                    // Verifica o tipo da célula antes de ler a quantidade
                    int quantidadeProduto;
                    if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        quantidadeProduto = (int) row.getCell(1).getNumericCellValue();
                    } else {
                        quantidadeProduto = Integer.parseInt(row.getCell(1).getStringCellValue());
                    }

                    // Verifica o tipo da célula antes de ler o valor
                    double valorProduto;
                    if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        valorProduto = row.getCell(2).getNumericCellValue();
                    } else {
                        valorProduto = Double.parseDouble(row.getCell(2).getStringCellValue().replace(",", "."));
                    }

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
}
