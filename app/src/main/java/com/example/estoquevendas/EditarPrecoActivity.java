package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditarPrecoActivity extends AppCompatActivity {

    ListView listViewProdutos;
    EditText edtNovoPreco;
    Button btnAtualizarPreco, btnVoltarMenu;
    List<String> produtos = new ArrayList<>();
    List<Double> precos = new ArrayList<>();
    int produtoSelecionadoPosicao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_preco);

        listViewProdutos = findViewById(R.id.listViewProdutos);
        edtNovoPreco = findViewById(R.id.edtNovoPreco);
        btnAtualizarPreco = findViewById(R.id.btnAtualizarPreco);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);

        // Carregar os produtos da planilha
        carregarProdutosDaPlanilha();

        // Configurar o ListView para seleção de produtos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtos);
        listViewProdutos.setAdapter(adapter);

        listViewProdutos.setOnItemClickListener((parent, view, position, id) -> {
            produtoSelecionadoPosicao = position;
            edtNovoPreco.setText(String.valueOf(precos.get(position))); // Exibe o preço atual no EditText
        });

        // Botão para atualizar o preço
        btnAtualizarPreco.setOnClickListener(v -> {
            if (produtoSelecionadoPosicao != -1) {
                String novoPrecoStr = edtNovoPreco.getText().toString();
                if (!novoPrecoStr.isEmpty()) {
                    try {
                        double novoPreco = Double.parseDouble(novoPrecoStr);
                        atualizarPrecoNaPlanilha(produtoSelecionadoPosicao, novoPreco);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Preço inválido.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Insira um novo preço.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Selecione um produto.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão para voltar ao menu
        btnVoltarMenu.setOnClickListener(v -> {
            Intent intent = new Intent(EditarPrecoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void carregarProdutosDaPlanilha() {
        File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
        if (!file.exists()) {
            Toast.makeText(this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            produtos.clear();
            precos.clear();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho

                String nomeProduto = row.getCell(0).getStringCellValue();
                double valorProduto = row.getCell(2).getNumericCellValue();

                produtos.add(nomeProduto);
                precos.add(valorProduto);
            }

            fis.close();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao carregar produtos da planilha!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void atualizarPrecoNaPlanilha(int posicao, double novoPreco) {
        File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
        if (!file.exists()) {
            Toast.makeText(this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Atualiza o preço na linha correspondente ao produto selecionado
            Row row = sheet.getRow(posicao + 1); // +1 para pular o cabeçalho
            row.getCell(2).setCellValue(novoPreco);

            // Salva as alterações no arquivo
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Preço atualizado com sucesso!", Toast.LENGTH_SHORT).show();

            // Atualiza a lista de preços na memória
            precos.set(posicao, novoPreco);
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao atualizar o preço.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}