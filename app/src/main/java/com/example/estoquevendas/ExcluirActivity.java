package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class ExcluirActivity extends AppCompatActivity {

    ListView listViewProdutos;
    Button btnExcluirProduto, btnVoltarMenu;
    List<String> produtos = new ArrayList<>();
    int produtoSelecionadoPosicao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluir);

        listViewProdutos = findViewById(R.id.listViewProdutos);
        btnExcluirProduto = findViewById(R.id.btnExcluirProduto);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);

        carregarProdutosDaPlanilha();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtos);
        listViewProdutos.setAdapter(adapter);

        listViewProdutos.setOnItemClickListener((parent, view, position, id) -> produtoSelecionadoPosicao = position);

        // Botão para excluir um produto
        btnExcluirProduto.setOnClickListener(v -> {
            if (produtoSelecionadoPosicao != -1) {
                excluirProdutoDaPlanilha(produtoSelecionadoPosicao);
            } else {
                Toast.makeText(this, "Selecione um produto para excluir.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão para voltar ao menu
        btnVoltarMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ExcluirActivity.this, MainActivity.class);
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

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Pula o cabeçalho
                String nomeProduto = row.getCell(0).getStringCellValue();
                produtos.add(nomeProduto);
            }

            fis.close();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao carregar produtos da planilha!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void excluirProdutoDaPlanilha(int posicao) {
        File file = new File(getExternalFilesDir(null), "estoquevendas/produtos.xlsx");
        if (!file.exists()) {
            Toast.makeText(this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            fis.close();

            int ultimaLinha = sheet.getLastRowNum();

            if (posicao >= 0 && posicao <= ultimaLinha) {
                sheet.removeRow(sheet.getRow(posicao + 1)); // +1 para ignorar cabeçalho

                // Se a linha removida não for a última, deslocamos as outras para cima
                if (posicao + 1 < ultimaLinha) {
                    sheet.shiftRows(posicao + 2, ultimaLinha, -1);
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show();

            // Atualizar a lista de produtos na tela
            carregarProdutosDaPlanilha();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao excluir o produto.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
