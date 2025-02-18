package com.example.estoquevendas;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;

public class ListaProdutosActivity extends AppCompatActivity {

    ListView listViewProdutos;
    Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);

        Log.d("ListaProdutosActivity", "onCreate() chamado!");

        listViewProdutos = findViewById(R.id.listViewProdutos);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Configurando o botão de voltar
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quando o botão for pressionado, a atividade será finalizada e o usuário voltará para a tela anterior
                finish();
            }
        });

        // Lógica para carregar a lista de produtos da planilha
        try {
            carregarProdutos();
        } catch (Exception e) {
            Toast.makeText(ListaProdutosActivity.this, "Erro ao carregar produtos!", Toast.LENGTH_SHORT).show();
            Log.e("ListaProdutosActivity", "Erro ao carregar produtos", e);
        }
    }

    private void carregarProdutos() {
        try {
            File pasta = new File(getExternalFilesDir(null), "estoquevendas");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File file = new File(pasta, "produtos.xlsx");

            if (!file.exists()) {
                Toast.makeText(ListaProdutosActivity.this, "Arquivo não encontrado!", Toast.LENGTH_LONG).show();
                Log.d("ListaProdutosActivity", "Arquivo não encontrado no caminho: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> produtosStrings = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getCell(0) != null && row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String nomeProduto = row.getCell(0).getStringCellValue();

                    double quantidade = 0;
                    if (row.getCell(1) != null) {
                        if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            quantidade = row.getCell(1).getNumericCellValue();
                        } else if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            try {
                                quantidade = Double.parseDouble(row.getCell(1).getStringCellValue());
                            } catch (NumberFormatException e) {
                                quantidade = 0;
                            }
                        }
                    }

                    double valor = 0;
                    if (row.getCell(2) != null) {
                        if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            valor = row.getCell(2).getNumericCellValue();
                        } else if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            try {
                                valor = Double.parseDouble(row.getCell(2).getStringCellValue());
                            } catch (NumberFormatException e) {
                                valor = 0;
                            }
                        }
                    }

                    // Verifica se a quantidade é maior que 0 antes de adicionar o produto à lista
                    if (quantidade > 0 && nomeProduto != null && !nomeProduto.isEmpty()) {
                        String produto = "Nome: " + nomeProduto + " | Quantidade: " + quantidade + " | Valor: R$ " + valor;
                        produtosStrings.add(produto);
                    }
                }
            }

            // Se houver produtos válidos, os exibe, caso contrário exibe uma mensagem.
            if (produtosStrings.isEmpty()) {
                produtosStrings.add("Nenhum produto cadastrado.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosStrings);
            listViewProdutos.setAdapter(adapter);

            fis.close();

        } catch (Exception e) {
            Toast.makeText(ListaProdutosActivity.this, "Erro ao carregar produtos!", Toast.LENGTH_SHORT).show();
            Log.e("ListaProdutosActivity", "Erro ao carregar produtos", e);
        }

    }
}